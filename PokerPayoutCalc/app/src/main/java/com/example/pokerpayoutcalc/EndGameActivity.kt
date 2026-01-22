package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import java.io.OutputStream

// Prosledjivanje podataka
const val EXTRA_ALL_PLAYERS_END_GAME = "com.example.pokerpayoutcalc.ALL_PLAYERS_END_GAME"
const val EXTRA_HOUSE_BALANCE_END_GAME = "com.example.pokerpayoutcalc.HOUSE_BALANCE_END_GAME"

// New extras for returning data to MainActivity
const val RESULT_ALL_PLAYERS_FINAL = "com.example.pokerpayoutcalc.ALL_PLAYERS_FINAL"
const val RESULT_HOUSE_BALANCE_FINAL = "com.example.pokerpayoutcalc.HOUSE_BALANCE_FINAL"
const val RESULT_GAME_ENDED_FLAG = "com.example.pokerpayoutcalc.GAME_ENDED_FLAG"


class EndGameActivity : AppCompatActivity() {

    private lateinit var textViewEndGameTitle: TextView
    private lateinit var endGameResultsLayout: LinearLayout
    private lateinit var textViewResultsPlaceholder: TextView
    private lateinit var buttonStartNewGame: Button

    private var mutableAllPlayers: MutableList<Player> = mutableListOf()
    private var mutableHouseBalance: Int = 0

    private lateinit var playerRepository: PlayerRepository

    private val transactionRecords = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_game)

        textViewEndGameTitle = findViewById(R.id.textViewEndGameTitle)
        endGameResultsLayout = findViewById(R.id.endGameResultsLayout)
        textViewResultsPlaceholder = findViewById(R.id.textViewResultsPlaceholder)
        buttonStartNewGame = findViewById(R.id.buttonStartNewGame)

        // inicijalizacija PlayerRepository
        playerRepository = PlayerRepository(this)

        // dohvati balanse
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableAllPlayers = intent.getParcelableArrayListExtra(EXTRA_ALL_PLAYERS_END_GAME, Player::class.java)?.toMutableList() ?: mutableListOf()
        } else {
            @Suppress("DEPRECATION")
            mutableAllPlayers = intent.getParcelableArrayListExtra<Player>(EXTRA_ALL_PLAYERS_END_GAME)?.toMutableList() ?: mutableListOf()
        }
        mutableHouseBalance = intent.getIntExtra(EXTRA_HOUSE_BALANCE_END_GAME, 0)

        textViewEndGameTitle.text = "Game Results"

        // logika - calculations are done every time onCreate is called
        performEndGameCalculations()

        // listener
        buttonStartNewGame.setOnClickListener {
            showStartNewGameConfirmationDialog()
        }
    }

    override fun onBackPressed() {
        // Prepare data to send back to MainActivity
        val resultIntent = Intent().apply {
            putParcelableArrayListExtra(RESULT_ALL_PLAYERS_FINAL, ArrayList(mutableAllPlayers))
            putExtra(RESULT_HOUSE_BALANCE_FINAL, mutableHouseBalance)
            putExtra(RESULT_GAME_ENDED_FLAG, true) // Indicate that the game has ended
        }
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    // Funkcija za cuvanje rezultata kao slike
    fun saveLongTextToGallery(context: Context, viewToCapture: View, fileName: String) {
        try {
            // 1. Create bitmap matching the full size of the content
            val bitmap = Bitmap.createBitmap(viewToCapture.width, viewToCapture.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 2. Draw the view (the LinearLayout with all the text) onto the bitmap
            viewToCapture.draw(canvas)

            // 3. MediaStore setup to save to Gallery
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GameResults")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, values, null, null)
                }
                Toast.makeText(context, "Results saved to Gallery!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performEndGameCalculations() {
        // Clear previous transaction records before new calculations
        transactionRecords.clear()

        val resultsBuilder = StringBuilder()

        // --- 1: kreiraj lsite sa pozitivnim i negativnim balansima ---
        val currentPositiveBalancePlayers = mutableAllPlayers
            .filter { it.balance > 0 }
            .sortedByDescending { it.balance }
            .toMutableList()

        val currentNegativeBalancePlayers = mutableAllPlayers
            .filter { it.balance < 0 }
            .sortedBy { it.balance }
            .toMutableList()

        /*resultsBuilder.append("--- Step 1: Initial Sorted Players ---\n\n")
        if (currentPositiveBalancePlayers.isEmpty()) {
            resultsBuilder.append("Players with Positive Balance (Highest to Lowest):\nNone\n")
        } else {
            resultsBuilder.append("Players with Positive Balance (Highest to Lowest):\n")
            currentPositiveBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nPlayers with Negative Balance (Lowest to Highest):\n")
        if (currentNegativeBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentNegativeBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nInitial House Balance: ${mutableHouseBalance}\n")
        resultsBuilder.append("\n-------------------------------------\n")*/


        // --- 2: HOUSE puni vip balanse ---
        /*resultsBuilder.append("\n--- Step 2: House Payouts to VIPs ---\n\n")*/
        val vipPlayersToPay = currentPositiveBalancePlayers
            .filter { it.isVip }
            .sortedByDescending { it.balance }

        if (vipPlayersToPay.isEmpty() || mutableHouseBalance <= 0) {
            /*resultsBuilder.append("No VIP players with positive balance or house has no money to pay.\n")*/
        } else {
            val playersProcessedInStep2 = mutableListOf<Player>()

            for (vipPlayer in vipPlayersToPay) {
                val playerInMasterList = mutableAllPlayers.find { it.name == vipPlayer.name }

                if (playerInMasterList != null && playerInMasterList.balance > 0 && mutableHouseBalance > 0) {
                    val amountPlayerIsOwed = playerInMasterList.balance

                    if (mutableHouseBalance >= amountPlayerIsOwed) {
                        mutableHouseBalance -= amountPlayerIsOwed
                        playerInMasterList.balance = 0
                        transactionRecords.add(
                            String.format(
                                Locale.US,
                                "HOUSE  -->  %s  :  %d",
                                playerInMasterList.name,
                                amountPlayerIsOwed
                            )
                        )
                        /*resultsBuilder.append(String.format(Locale.US, "%s received %d from HOUSE. New Balance: 0\n", playerInMasterList.name, amountPlayerIsOwed))*/
                    } else {
                        val amountPaidByHouse = mutableHouseBalance
                        playerInMasterList.balance -= amountPaidByHouse
                        mutableHouseBalance = 0
                        transactionRecords.add(
                            String.format(
                                Locale.US,
                                "HOUSE  -->  %s  :  %d",
                                playerInMasterList.name,
                                amountPaidByHouse
                            )
                        )
                        /*resultsBuilder.append(String.format(Locale.US, "%s received %d from HOUSE. New Balance: %d\n", playerInMasterList.name, amountPaidByHouse, playerInMasterList.balance))*/
                    }
                    playersProcessedInStep2.add(playerInMasterList)
                }
                if (mutableHouseBalance <= 0) {
                    /*resultsBuilder.append("HOUSE balance depleted. No more payouts from HOUSE.\n")*/
                    break
                }
            }

            /*resultsBuilder.append("\nTransactions:\n")
            if (transactionRecords.isEmpty()) {
                resultsBuilder.append("No transactions in this step.\n")
            } else {
                transactionRecords.forEach { resultsBuilder.append("$it\n") }
            }*/
        }
        /*resultsBuilder.append("\nHouse Balance after Step 2: ${mutableHouseBalance}\n")
        resultsBuilder.append("\n-------------------------------------\n")*/

        // --- 3: izbaci sa 0 balansom ---
        /*resultsBuilder.append("\n--- Step 3: Removing Zero Balance Players from Positive List ---\n\n")*/

        val playersRemovedInStep3 = mutableListOf<Player>()
        val positiveIteratorStep3 = currentPositiveBalancePlayers.iterator()
        while (positiveIteratorStep3.hasNext()) {
            val player = positiveIteratorStep3.next()
            val playerInMasterList = mutableAllPlayers.find { it.name == player.name }
            if (playerInMasterList != null && playerInMasterList.balance == 0) {
                playersRemovedInStep3.add(playerInMasterList)
                positiveIteratorStep3.remove()
                /*resultsBuilder.append(String.format(Locale.US, "Removed %s from positive list (Balance: 0).\n", playerInMasterList.name))*/
            }
        }

        /*if (playersRemovedInStep3.isEmpty()) {
            resultsBuilder.append("No players with zero balance were found in the positive list to remove.\n")
        }*/

        currentPositiveBalancePlayers.sortByDescending { it.balance }

        /*resultsBuilder.append("\nPositive Balance Players after Step 3:\n")
        if (currentPositiveBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentPositiveBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\n-------------------------------------\n")*/

        // --- 4: negativni pune pozitivnim balanse ---
        /*resultsBuilder.append("\n--- Step 4: Player-to-Player Settlements (Negative paying Positive) - Round 1 ---\n\n")*/

        if (currentNegativeBalancePlayers.isEmpty() || currentPositiveBalancePlayers.isEmpty()) {
            /*resultsBuilder.append("No negative players or no positive players remaining for settlement in Step 4.\n")*/
        } else {
            val negativePlayersToProcessInStep4 = currentNegativeBalancePlayers.toMutableList()

            for (negativePlayerToProcess in negativePlayersToProcessInStep4) {
                val negativePlayerInMasterList = mutableAllPlayers.find { it.name == negativePlayerToProcess.name }

                if (negativePlayerInMasterList != null && negativePlayerInMasterList.balance < 0) {
                    val positivePlayersIterator = currentPositiveBalancePlayers.iterator()
                    while (positivePlayersIterator.hasNext() && negativePlayerInMasterList.balance < 0) {
                        val positivePlayerToProcess = positivePlayersIterator.next()
                        val positivePlayerInMasterList = mutableAllPlayers.find { it.name == positivePlayerToProcess.name }

                        if (positivePlayerInMasterList != null && positivePlayerInMasterList.balance > 0) {
                            val amountNegativePlayerOwes = -negativePlayerInMasterList.balance
                            val amountPositivePlayerIsOwed = positivePlayerInMasterList.balance
                            val transactionAmount: Int

                            if (amountNegativePlayerOwes >= amountPositivePlayerIsOwed) {
                                transactionAmount = amountPositivePlayerIsOwed
                                negativePlayerInMasterList.balance += amountPositivePlayerIsOwed
                                positivePlayerInMasterList.balance = 0
                                positivePlayersIterator.remove()

                                transactionRecords.add(
                                    String.format(
                                        Locale.US,
                                        "%s  -->  %s  :  %d",
                                        negativePlayerInMasterList.name,
                                        positivePlayerInMasterList.name,
                                        transactionAmount
                                    )
                                )
                                /*resultsBuilder.append(String.format(Locale.US, "%s paid %s: %d. New Balances: %s: %d, %s: %d\n",
                                    negativePlayerInMasterList.name, positivePlayerInMasterList.name, transactionAmount,
                                    negativePlayerInMasterList.name, negativePlayerInMasterList.balance,
                                    positivePlayerInMasterList.name, positivePlayerInMasterList.balance))*/
                            }
                        }
                    }
                }
            }

            val positiveIteratorAfterStep4 = currentPositiveBalancePlayers.iterator()
            while (positiveIteratorAfterStep4.hasNext()) {
                val player = positiveIteratorAfterStep4.next()
                val playerInMasterList = mutableAllPlayers.find { it.name == player.name }
                if (playerInMasterList != null && playerInMasterList.balance == 0) {
                    positiveIteratorAfterStep4.remove()
                    /*resultsBuilder.append(String.format(Locale.US, "Removed %s from positive list (Balance: 0) after Step 4.\n", playerInMasterList.name))*/
                }
            }
        }
        /*resultsBuilder.append("\nTransactions after Step 4:\n")
        if (transactionRecords.isEmpty()) {
            resultsBuilder.append("No transactions in this step.\n")
        } else {
            transactionRecords.forEach { resultsBuilder.append("$it\n") }
        }
        resultsBuilder.append("\nPositive Balance Players after Step 4:\n")
        if (currentPositiveBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentPositiveBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nNegative Balance Players after Step 4:\n")
        if (currentNegativeBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentNegativeBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nHouse Balance after Step 4: ${mutableHouseBalance}\n")
        resultsBuilder.append("\n-------------------------------------\n")*/

        // --- 5: izbaci nule ---
        /*resultsBuilder.append("\n--- Step 5: Removing Zero Balance Players from Negative List ---\n\n")*/

        val playersRemovedInStep5 = mutableListOf<Player>()
        val negativeIteratorStep5 = currentNegativeBalancePlayers.iterator()
        while (negativeIteratorStep5.hasNext()) {
            val player = negativeIteratorStep5.next()
            val playerInMasterList = mutableAllPlayers.find { it.name == player.name }
            if (playerInMasterList != null && playerInMasterList.balance == 0) {
                playersRemovedInStep5.add(playerInMasterList)
                negativeIteratorStep5.remove() // Remove from currentNegativeBalancePlayers
                /*resultsBuilder.append(String.format(Locale.US, "Removed %s from negative list (Balance: 0).\n", playerInMasterList.name))*/
            }
        }

        /*if (playersRemovedInStep5.isEmpty()) {
            resultsBuilder.append("No players with zero balance were found in the negative list to remove.\n")
        }*/

        currentNegativeBalancePlayers.sortBy { it.balance }

        /*resultsBuilder.append("\nNegative Balance Players after Step 5:\n")
        if (currentNegativeBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentNegativeBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\n-------------------------------------\n")*/

        // --- 6: negativni pune pozitivne kako god mogu ---
        /*resultsBuilder.append("\n--- Step 6: Player-to-Player Settlements (Negative paying Positive) - Round 2 ---\n\n")*/

        if (currentNegativeBalancePlayers.isEmpty() || currentPositiveBalancePlayers.isEmpty()) {
            /*resultsBuilder.append("No negative players or no positive players remaining for settlement in Step 6.\n")*/
        } else {
            val negativePlayersToProcessInStep6 = currentNegativeBalancePlayers.toMutableList()

            for (negativePlayerToProcess in negativePlayersToProcessInStep6) {
                val negativePlayerInMasterList = mutableAllPlayers.find { it.name == negativePlayerToProcess.name }

                if (negativePlayerInMasterList != null && negativePlayerInMasterList.balance < 0) {
                    val positivePlayersIterator = currentPositiveBalancePlayers.iterator()
                    while (positivePlayersIterator.hasNext() && negativePlayerInMasterList.balance < 0) {
                        val positivePlayerToProcess = positivePlayersIterator.next()
                        val positivePlayerInMasterList = mutableAllPlayers.find { it.name == positivePlayerToProcess.name }

                        if (positivePlayerInMasterList != null && positivePlayerInMasterList.balance > 0) {
                            val amountNegativePlayerOwes = -negativePlayerInMasterList.balance
                            val amountPositivePlayerIsOwed = positivePlayerInMasterList.balance

                            val transactionAmount: Int

                            if (amountNegativePlayerOwes >= amountPositivePlayerIsOwed) {
                                transactionAmount = amountPositivePlayerIsOwed
                                negativePlayerInMasterList.balance += amountPositivePlayerIsOwed
                                positivePlayerInMasterList.balance = 0
                                positivePlayersIterator.remove()
                            } else {
                                transactionAmount = amountNegativePlayerOwes
                                positivePlayerInMasterList.balance -= amountNegativePlayerOwes
                                negativePlayerInMasterList.balance = 0
                            }

                            transactionRecords.add(
                                String.format(
                                    Locale.US,
                                    "%s  -->  %s  :  %d",
                                    negativePlayerInMasterList.name,
                                    positivePlayerInMasterList.name,
                                    transactionAmount
                                )
                            )
                            /*resultsBuilder.append(String.format(Locale.US, "%s paid %s: %d. New Balances: %s: %d, %s: %d\n",
                                negativePlayerInMasterList.name, positivePlayerInMasterList.name, transactionAmount,
                                negativePlayerInMasterList.name, negativePlayerInMasterList.balance,
                                positivePlayerInMasterList.name, positivePlayerInMasterList.balance))*/

                            if (negativePlayerInMasterList.balance == 0) {
                                break
                            }
                        }
                    }
                }
            }

            val positiveIteratorAfterStep6 = currentPositiveBalancePlayers.iterator()
            while (positiveIteratorAfterStep6.hasNext()) {
                val player = positiveIteratorAfterStep6.next()
                val playerInMasterList = mutableAllPlayers.find { it.name == player.name }
                if (playerInMasterList != null && playerInMasterList.balance == 0) {
                    positiveIteratorAfterStep6.remove()
                    /*resultsBuilder.append(String.format(Locale.US, "Removed %s from positive list (Balance: 0) after Step 6.\n", playerInMasterList.name))*/
                }
            }
        }
        /*resultsBuilder.append("\nTransactions after Step 6:\n")
        if (transactionRecords.isEmpty()) {
            resultsBuilder.append("No new transactions in this step.\n")
        } else {
            transactionRecords.forEach { resultsBuilder.append("$it\n") }
        }
        resultsBuilder.append("\nPositive Balance Players after Step 6:\n")
        if (currentPositiveBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentPositiveBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nNegative Balance Players after Step 6:\n")
        if (currentNegativeBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentNegativeBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\nHouse Balance after Step 6: ${mutableHouseBalance}\n")
        resultsBuilder.append("\n---------------------------\n")*/

        // --- 7: izbaci nule ---
        /*resultsBuilder.append("\n--- Step 7: Removing Zero Balance Players from Negative List (After Round 2) ---\n\n")*/

        val playersRemovedInStep7 = mutableListOf<Player>()
        val negativeIteratorStep7 = currentNegativeBalancePlayers.iterator()
        while (negativeIteratorStep7.hasNext()) {
            val player = negativeIteratorStep7.next()
            val playerInMasterList = mutableAllPlayers.find { it.name == player.name }
            if (playerInMasterList != null && playerInMasterList.balance == 0) {
                playersRemovedInStep7.add(playerInMasterList)
                negativeIteratorStep7.remove()
                /*resultsBuilder.append(String.format(Locale.US, "Removed %s from negative list (Balance: 0) after Step 7.\n", playerInMasterList.name))*/
            }
        }

        /*if (playersRemovedInStep7.isEmpty()) {
            resultsBuilder.append("No players with zero balance were found in the negative list to remove.\n")
        }*/

        currentNegativeBalancePlayers.sortBy { it.balance }

        /*resultsBuilder.append("\nNegative Balance Players after Step 7:\n")
        if (currentNegativeBalancePlayers.isEmpty()) {
            resultsBuilder.append("None\n")
        } else {
            currentNegativeBalancePlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }
        resultsBuilder.append("\n-------------------------------------\n")*/


        // --- 8: HOUSE puni ostale pozitivne ---
        /*resultsBuilder.append("\n--- Step 8: House Settles Remaining Balances ---\n\n")*/

        if (currentNegativeBalancePlayers.isNotEmpty()) {
            /*resultsBuilder.append("Negative players remaining. House collects debts.\n")*/
            val negativePlayersToProcessInStep8 = currentNegativeBalancePlayers.toMutableList()
            for (negativePlayer in negativePlayersToProcessInStep8) {
                val playerInMasterList = mutableAllPlayers.find { it.name == negativePlayer.name }
                if (playerInMasterList != null && playerInMasterList.balance < 0) {
                    val amountOwedToHouse = -playerInMasterList.balance // Convert to positive amount
                    mutableHouseBalance += amountOwedToHouse
                    playerInMasterList.balance = 0
                    transactionRecords.add(
                        String.format(
                            Locale.US,
                            "%s  -->  HOUSE  :  %d",
                            playerInMasterList.name,
                            amountOwedToHouse
                        )
                    )
                    /*resultsBuilder.append(String.format(Locale.US, "HOUSE collected %d from %s. New Balance: 0\n", amountOwedToHouse, playerInMasterList.name))*/
                }
            }
            /*resultsBuilder.append("All remaining negative balances settled with HOUSE.\n")*/
        } else if (currentPositiveBalancePlayers.isNotEmpty()) {
            /*resultsBuilder.append("Positive players remaining. House pays out.\n")*/
            val positivePlayersToProcessInStep8 = currentPositiveBalancePlayers.toMutableList()
            for (positivePlayer in positivePlayersToProcessInStep8) {
                val playerInMasterList = mutableAllPlayers.find { it.name == positivePlayer.name }
                if (playerInMasterList != null && playerInMasterList.balance > 0) {
                    val amountToPay = playerInMasterList.balance
                    mutableHouseBalance -= amountToPay
                    playerInMasterList.balance = 0
                    transactionRecords.add(
                        String.format(
                            Locale.US,
                            "HOUSE  -->  %s  :  %d",
                            playerInMasterList.name,
                            amountToPay
                        )
                    )
                    /*resultsBuilder.append(String.format(Locale.US, "HOUSE paid %d to %s. New Balance: 0\n", amountToPay, playerInMasterList.name))*/
                }
            }
            /*resultsBuilder.append("All remaining positive balances settled by HOUSE.\n")*/
        } else {
            /*resultsBuilder.append("No players with outstanding balances remaining for House settlement.\n")*/
        }
        /*resultsBuilder.append("\nHouse Balance after Step 8: ${mutableHouseBalance}\n")
        resultsBuilder.append("\n-------------------------------------\n")*/


        // --- 9: negativni ostali pune kucu ---
        /*resultsBuilder.append("\n--- Step 9: Final Game Summary ---\n\n")*/

        resultsBuilder.append("All Transactions:\n\n")
        if (transactionRecords.isEmpty()) {
            resultsBuilder.append("No transactions recorded during game end.\n")
        } else {
            transactionRecords.forEach { resultsBuilder.append("$it\n") }
        }

        resultsBuilder.append("\n\n\nFinal House Balance: ${mutableHouseBalance}\n\n")
        /*resultsBuilder.append("\nFinal Player Balances (for verification):\n")*/
        /*if (mutableAllPlayers.isEmpty()) {
            resultsBuilder.append("No players in the game.\n")
        } else {
            mutableAllPlayers.forEach {
                resultsBuilder.append(String.format(Locale.US, "%s: %d\n", it.name, it.balance))
            }
        }*/
        resultsBuilder.append("\n\n\n---------------------------\n\n\n")

        // ispis
        resultsBuilder.append("\nTotal Buyins Summary:\n\n")
        var totalAllBuyins = 0
        if (mutableAllPlayers.isEmpty()) {
            resultsBuilder.append("No players in the game to summarize buyins.\n")
        } else {
            mutableAllPlayers.forEach { player ->
                val playerTotalBuyins = player.sumCashBuyin + player.sumLoanBuyin
                resultsBuilder.append(String.format(Locale.US, "%s All Buyins: %d\n", player.name, playerTotalBuyins))
                totalAllBuyins += playerTotalBuyins
            }
        }
        resultsBuilder.append(String.format(Locale.US, "\n\n\nTotal Buyins for all Players: %d\n", totalAllBuyins))
        resultsBuilder.append("\n\n---------------------------\n\n")

        textViewResultsPlaceholder.text = resultsBuilder.toString()

        // cuvanje slike rezultata
        endGameResultsLayout.post {
            saveLongTextToGallery(this, endGameResultsLayout, "Results_${System.currentTimeMillis()}")
        }
    }

    private fun saveCurrentGameState() {
        // This method is now only for saving the current state of players and house balance
        // without affecting the 'isGameEnded' flag.
        playerRepository.savePlayersAndHouseBalance(mutableAllPlayers, mutableHouseBalance)
        Toast.makeText(this, "Game state saved after calculations.", Toast.LENGTH_SHORT).show()
    }

    private fun showStartNewGameConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Start New Game?")
            .setMessage("Are you sure you want to start a new game? All current player data and house balance will be cleared.")
            .setPositiveButton("YES") { dialog, _ ->
                playerRepository.clearAllData()
                Toast.makeText(this, "Starting a new game. Data cleared!", Toast.LENGTH_LONG).show()

                // Set result to indicate a new game should start (and implicitly game ended)
                val resultIntent = Intent().apply {
                    putExtra(RESULT_GAME_ENDED_FLAG, true) // Indicate game ended and new game started
                }
                setResult(Activity.RESULT_OK, resultIntent)

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
