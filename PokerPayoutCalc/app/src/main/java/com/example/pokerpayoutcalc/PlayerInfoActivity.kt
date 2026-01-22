package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

// Prosledjivanje podataka
const val EXTRA_PLAYER_INFO = "com.example.pokerpayoutcalc.PLAYER_INFO"
const val EXTRA_HOUSE_BALANCE_INFO = "com.example.pokerpayoutcalc.HOUSE_BALANCE_INFO"

const val RESULT_PLAYER_UPDATED_FROM_INFO = "com.example.pokerpayoutcalc.PLAYER_UPDATED_FROM_INFO"
const val RESULT_HOUSE_BALANCE_UPDATED_FROM_INFO = "com.example.pokerpayoutcalc.HOUSE_BALANCE_UPDATED_FROM_INFO"


class PlayerInfoActivity : AppCompatActivity() {

    private lateinit var textViewInfoPlayerName: TextView
    private lateinit var textViewInfoAllBuyins: TextView
    private lateinit var textViewAllCashBuyinsValue: TextView
    private lateinit var textViewListCashBuyinsValue: TextView
    private lateinit var textViewAllLoanBuyinsValue: TextView
    private lateinit var textViewListLoanBuyinsValue: TextView
    private lateinit var buttonEditDeleteBuyins: Button
    private lateinit var buttonInfoBack: Button

    private var currentPlayer: Player? = null
    private var currentHouseBalance: Int = 0

    // da li smo neto menjali
    private var hasModifiedPlayerOrHouse = false

    // ActivityResultLauncher za EditBuyinsActivity
    private val editBuyinsActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED_FROM_EDIT, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED_FROM_EDIT) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED_FROM_EDIT, currentHouseBalance) ?: currentHouseBalance

            updatedPlayer?.let { player ->
                currentPlayer = player
                currentHouseBalance = updatedHouseBalance
                updatePlayerInfoUI(player)
                hasModifiedPlayerOrHouse = true
                Toast.makeText(this, "Buyins updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_info)

        // inicijalizacija views
        textViewInfoPlayerName = findViewById(R.id.textViewInfoPlayerName)
        textViewInfoAllBuyins = findViewById(R.id.textViewInfoAllBuyins)
        textViewAllCashBuyinsValue = findViewById(R.id.textViewAllCashBuyinsValue)
        textViewListCashBuyinsValue = findViewById(R.id.textViewListCashBuyinsValue)
        textViewAllLoanBuyinsValue = findViewById(R.id.textViewAllLoanBuyinsValue)
        textViewListLoanBuyinsValue = findViewById(R.id.textViewListLoanBuyinsValue)
        buttonEditDeleteBuyins = findViewById(R.id.buttonEditDeleteBuyins)
        buttonInfoBack = findViewById(R.id.buttonInfoBack)

        // dohvati balances
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_INFO, Player::class.java)
        } else {
            @Suppress("DEPRECATION")
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_INFO) as? Player
        }
        currentHouseBalance = intent.getIntExtra(EXTRA_HOUSE_BALANCE_INFO, 0)

        // prikazi info
        currentPlayer?.let { player ->
            updatePlayerInfoUI(player)
        } ?: run {
            Toast.makeText(this, "Error: Player data not found!", Toast.LENGTH_LONG).show()
            finish()
        }

        // listener za Edit/Delete Buyins dugme
        buttonEditDeleteBuyins.setOnClickListener {
            currentPlayer?.let { player ->
                val intent = Intent(this, EditBuyinsActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER_EDIT_BUYINS, player)
                    putExtra(EXTRA_HOUSE_BALANCE_IN_EDIT, currentHouseBalance) // Pass current house balance
                }
                editBuyinsActivityResultLauncher.launch(intent)
            }
        }

        // listener za the Back
        buttonInfoBack.setOnClickListener {
            setResultAndFinish()
        }
    }

    override fun onBackPressed() {
        setResultAndFinish()
        super.onBackPressed()
    }

    // apdejt UI
    private fun updatePlayerInfoUI(player: Player) {
        textViewInfoPlayerName.text = player.name
        val totalBuyins = player.sumCashBuyin + player.sumLoanBuyin
        textViewInfoAllBuyins.text = String.format(Locale.US, "All Buyins: %d", totalBuyins)

        textViewAllCashBuyinsValue.text = String.format(Locale.US, "%d", player.sumCashBuyin)
        textViewListCashBuyinsValue.text = if (player.cashBuyins.isEmpty()) "-" else player.cashBuyins.joinToString(", ")

        textViewAllLoanBuyinsValue.text = String.format(Locale.US, "%d", player.sumLoanBuyin)
        textViewListLoanBuyinsValue.text = if (player.loanBuyins.isEmpty()) "-" else player.loanBuyins.joinToString(", ")
    }

    private fun setResultAndFinish() {
        if (hasModifiedPlayerOrHouse && currentPlayer != null) {
            val resultIntent = Intent().apply {
                putExtra(RESULT_PLAYER_UPDATED_FROM_INFO, currentPlayer)
                putExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_INFO, currentHouseBalance)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
}
