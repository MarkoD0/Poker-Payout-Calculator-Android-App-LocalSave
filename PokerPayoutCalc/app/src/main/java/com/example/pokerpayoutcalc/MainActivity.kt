package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import java.util.ArrayList // Explicitly import ArrayList

// MainActivity je ulazna tacka za glavni ekran aplikacije.
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewPlayers: RecyclerView
    private lateinit var buttonAddPlayer: Button
    private lateinit var textViewTitle: TextView
    private lateinit var buttonEndGame: Button

    private lateinit var playerRepository: PlayerRepository

    // Lista sadrzi ListItem objekte (moze biti i Player i House)
    private val combinedList = mutableListOf<ListItem>()
    private lateinit var playerAdapter: PlayerAdapter

    // Instanca HOUSE objekta. Ovo ce uvek referencirati na House objekat koji se nalazi u combinedList
    private lateinit var house: House

    // ActivityResultLauncher za RebuyActivity.
    private val rebuyActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, house.balance) ?: house.balance

            updatedPlayer?.let { player ->
                // Apdejtuj igrace
                val index = combinedList.indexOfFirst { it is Player && it.name == player.name }
                if (index != -1) {
                    combinedList[index] = player
                }
                // Apdejtuj balance HOUS-a
                house.balance = updatedHouseBalance

                // Re-sortiraj listu
                sortAndNotifyDataSetChanged()

                saveAllData()
                Toast.makeText(this, "${player.name} data updated and saved!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Rebuy cancelled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLauncher za AddPlayerActivity.
    private val addPlayerActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_EXTRA_NEW_PLAYER, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_EXTRA_NEW_PLAYER) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_OUT, house.balance) ?: house.balance

            newPlayer?.let { player ->
                combinedList.add(player)

                // Apdejtuj balance HOUS-a
                house.balance = updatedHouseBalance

                // Re-sortiraj listu i obavesti
                sortAndNotifyDataSetChanged()
                // idi do novog igraca
                recyclerViewPlayers.scrollToPosition(combinedList.indexOf(player))

                updatePlayerCount()
                saveAllData()
                Toast.makeText(this, "${player.name} added and saved!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Add Player cancelled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLauncher za PlayerInfoActivity.
    private val playerInfoActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_INFO, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_INFO) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_INFO, house.balance) ?: house.balance

            updatedPlayer?.let { player ->
                // Apdejtuj igrace
                val index = combinedList.indexOfFirst { it is Player && it.name == player.name }
                if (index != -1) {
                    combinedList[index] = player // Update the player object in the list
                }
                // Apdejtuj balance HOUS-a
                house.balance = updatedHouseBalance

                // Re-sortiraj listu
                sortAndNotifyDataSetChanged()

                saveAllData()
                Toast.makeText(this, "${player.name} buyins updated and saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ActivityResultLauncher za CashoutActivity.
    private val cashoutActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_CASHOUT, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_CASHOUT) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_CASHOUT, house.balance) ?: house.balance
            val cashoutMessage = data?.getStringExtra("cashout_message") ?: "Cashout completed."

            updatedPlayer?.let { player ->
                // Apdejtuj listu igraca
                val index = combinedList.indexOfFirst { it is Player && it.name == player.name }
                if (index != -1) {
                    combinedList[index] = player
                }
                // Apdejtuj balance HOUS-a
                house.balance = updatedHouseBalance

                // Re-sortiraj listu
                sortAndNotifyDataSetChanged()

                saveAllData() // Save all data after cashout
                Toast.makeText(this, "Cashout successful!", Toast.LENGTH_SHORT).show()

                // Pokreni CashoutResultActivity
                val intent = Intent(this, CashoutResultActivity::class.java).apply {
                    putExtra("cashout_message", cashoutMessage)
                }
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "Cashout cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    // ActivityResultLauncher za EndGameActivity
    private val endGameActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val finalPlayers: MutableList<Player>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableArrayListExtra(RESULT_ALL_PLAYERS_FINAL, Player::class.java)?.toMutableList()
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableArrayListExtra<Player>(RESULT_ALL_PLAYERS_FINAL)?.toMutableList()
            }
            val finalHouseBalance = data?.getIntExtra(RESULT_HOUSE_BALANCE_FINAL, house.balance) ?: house.balance
            val isGameEnded = data?.getBooleanExtra(RESULT_GAME_ENDED_FLAG, false) ?: false

            if (finalPlayers != null) {
                // Update the combinedList with the final player states from EndGameActivity
                // First, clear existing players and then add the updated ones
                combinedList.removeAll { it is Player }
                combinedList.addAll(finalPlayers)
                house.balance = finalHouseBalance

                // Save the updated state and the game ended flag
                playerRepository.savePlayersAndHouseBalance(finalPlayers, finalHouseBalance)
                playerRepository.saveIsGameEnded(isGameEnded)

                sortAndNotifyDataSetChanged()
                Toast.makeText(this, "Game ended and state saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "End game completed, but no data returned.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "End game cancelled.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicijalizacija UI elemenata
        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers)
        buttonAddPlayer = findViewById(R.id.buttonAddPlayer)
        textViewTitle = findViewById(R.id.textViewTitle)
        buttonEndGame = findViewById(R.id.buttonEndGame)

        // Inicijalizacija PlayerRepository
        playerRepository = PlayerRepository(this)

        // Ucitaj HOUSE
        val loadedHouseBalance = playerRepository.loadHouseBalance()
        house = House(balance = loadedHouseBalance)

        // HOUSE je uvek na pocetku
        combinedList.add(house)

        // Ucitaj igrace
        val loadedPlayers = playerRepository.loadPlayers()

        if (loadedPlayers.isEmpty()) {
            // Sacuvaj inicajlne podatke
            saveAllData()
            Toast.makeText(this, "Initial data saved!", Toast.LENGTH_SHORT).show()
        } else {
            // Ili ucitaj podatke
            combinedList.addAll(loadedPlayers)
            Toast.makeText(this, "Data loaded!", Toast.LENGTH_SHORT).show()
        }

        sortAndNotifyDataSetChanged()

        // *** IMPORTANT: REMOVED THE AUTO-REDIRECTION LOGIC HERE ***
        // The game will now always start on MainActivity.
        // The user must explicitly click the "End Game" button to go to EndGameActivity.


        // postavi RecyclerView za LinearLayoutManager
        recyclerViewPlayers.layoutManager = LinearLayoutManager(this)
        // Inicijalizacija PlayerAdapter
        playerAdapter = PlayerAdapter(
            combinedList,
            onRebuyClick = { player ->
                val intent = Intent(this, RebuyActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER, player)
                    putExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, house.balance)
                }
                rebuyActivityResultLauncher.launch(intent)
            },
            onInfoClick = { player ->
                val intent = Intent(this, PlayerInfoActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER_INFO, player)
                    putExtra(EXTRA_HOUSE_BALANCE_INFO, house.balance)
                }
                playerInfoActivityResultLauncher.launch(intent)
            },
            onCashoutClick = { player ->
                val intent = Intent(this, CashoutActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER_CASHOUT, player)
                    putExtra(EXTRA_HOUSE_BALANCE_CASHOUT_IN, house.balance)
                }
                cashoutActivityResultLauncher.launch(intent)
            }
        )
        recyclerViewPlayers.adapter = playerAdapter

        updatePlayerCount()

        // listener za buttonAddPlayer
        buttonAddPlayer.setOnClickListener {
            val intent = Intent(this, AddPlayerActivity::class.java).apply {
                putExtra(EXTRA_HOUSE_BALANCE_IN, house.balance)
            }
            addPlayerActivityResultLauncher.launch(intent)
        }

        // listener za buttonEndGame
        buttonEndGame.setOnClickListener {
            handleEndGameButtonClick()
        }
    }

    // refreshuj UI
    override fun onResume() {
        super.onResume()
        // Ensure the game state is reloaded and UI updated when returning to MainActivity
        // This is important if EndGameActivity modified the data but MainActivity didn't get the result (e.g., due to process death)
        val loadedPlayers = playerRepository.loadPlayers()
        val loadedHouseBalance = playerRepository.loadHouseBalance()

        // Update combinedList and house object
        combinedList.clear()
        combinedList.add(House(balance = loadedHouseBalance)) // Re-add house
        combinedList.addAll(loadedPlayers)

        house = combinedList.first { it is House } as House // Ensure 'house' reference is updated

        sortAndNotifyDataSetChanged()
        updatePlayerCount()
    }

    // updejtuj broj igraca
    private fun updatePlayerCount() {
        val playerCount = combinedList.count { it is Player && it.isPlaying }
        textViewTitle.text = "Current Players: $playerCount"
    }

    // sacuvaj podatke
    private fun saveAllData() {
        val playersToSave = combinedList.filterIsInstance<Player>()
        playerRepository.savePlayersAndHouseBalance(playersToSave, house.balance)
        // Ensure isGameEnded is false when saving from MainActivity during normal play
        playerRepository.saveIsGameEnded(false)
    }

    // sortiraj
    private fun sortAndNotifyDataSetChanged() {
        val currentHouseItem = combinedList.first { it is House } as House

        val players = combinedList.filterIsInstance<Player>()

        val playingPlayers = players.filter { it.isPlaying }
            .sortedWith(compareByDescending<Player> { it.isVip }.thenBy { it.name })

        val nonPlayingPlayers = players.filter { !it.isPlaying }.sortedBy { it.name }

        combinedList.clear()
        combinedList.add(currentHouseItem)
        combinedList.addAll(playingPlayers)
        combinedList.addAll(nonPlayingPlayers)

        house = currentHouseItem

        if (::playerAdapter.isInitialized) {
            playerAdapter.notifyDataSetChanged()
        }
    }

    // EndGame dugme
    private fun handleEndGameButtonClick() {
        val allPlayers = combinedList.filterIsInstance<Player>()
        val anyPlayerStillPlaying = allPlayers.any { it.isPlaying }

        if (anyPlayerStillPlaying) {
            Toast.makeText(this, "All players must be cashed out before ending the game!", Toast.LENGTH_LONG).show()
        } else {
            // Reset the isGameEnded flag to false before launching EndGameActivity
            // This ensures that if the user backs out, the game isn't marked as ended prematurely.
            playerRepository.saveIsGameEnded(false)

            val intent = Intent(this, EndGameActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_ALL_PLAYERS_END_GAME, ArrayList(allPlayers))
                putExtra(EXTRA_HOUSE_BALANCE_END_GAME, house.balance)
            }
            endGameActivityResultLauncher.launch(intent)
        }
    }
}
