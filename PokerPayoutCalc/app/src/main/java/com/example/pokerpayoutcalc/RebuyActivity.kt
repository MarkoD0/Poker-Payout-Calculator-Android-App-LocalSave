package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Prosledjivanje podataka
const val EXTRA_PLAYER = "com.example.pokerpayoutcalc.PLAYER"
const val RESULT_EXTRA_PLAYER_UPDATED = "com.example.pokerpayoutcalc.PLAYER_UPDATED"
const val RESULT_EXTRA_HOUSE_BALANCE_UPDATED = "com.example.pokerpayoutcalc.HOUSE_BALANCE_UPDATED"

class RebuyActivity : AppCompatActivity() {

    private lateinit var textViewPlayerName: TextView
    private lateinit var textViewAllBuyins: TextView
    private lateinit var editTextRebuyAmount: EditText
    private lateinit var radioGroupRebuyType: RadioGroup
    private lateinit var radioButtonLoan: RadioButton
    private lateinit var radioButtonCash: RadioButton
    private lateinit var buttonConfirmRebuy: Button
    private lateinit var buttonRebuyBack: Button

    // Igrac kog menjamo
    private var currentPlayer: Player? = null
    // HOSUE balance
    private var currentHouseBalance: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rebuy)

        // Inicijalizacija vjua
        textViewPlayerName = findViewById(R.id.textViewRebuyPlayerName)
        textViewAllBuyins = findViewById(R.id.textViewRebuyAllBuyins)
        editTextRebuyAmount = findViewById(R.id.editTextRebuyAmount)
        radioGroupRebuyType = findViewById(R.id.radioGroupRebuyType)
        radioButtonLoan = findViewById(R.id.radioButtonLoan)
        radioButtonCash = findViewById(R.id.radioButtonCash)
        buttonConfirmRebuy = findViewById(R.id.buttonConfirmRebuy)
        buttonRebuyBack = findViewById(R.id.buttonRebuyBack)

        // dohvatamo podatke
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER, Player::class.java)
        } else {
            @Suppress("DEPRECATION") // Suppress deprecation warning for older API versions
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER) as? Player
        }
        currentHouseBalance = intent.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, 0)

        // prikaz informacija
        currentPlayer?.let { player ->
            textViewPlayerName.text = player.name
            textViewAllBuyins.text = "All Buyins: ${player.sumCashBuyin + player.sumLoanBuyin}"
        } ?: run {
            Toast.makeText(this, "Error: Player data not found!", Toast.LENGTH_LONG).show()
            finish() // Close activity if player data is missing
        }

        // listener buttonConfirmRebuy
        buttonConfirmRebuy.setOnClickListener {
            handleConfirmRebuy()
        }

        // listener buttonRebuyBack
        buttonRebuyBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // logika
    private fun handleConfirmRebuy() {
        val rebuyAmountString = editTextRebuyAmount.text.toString()
        if (rebuyAmountString.isBlank()) {
            Toast.makeText(this, "Please enter a rebuy amount.", Toast.LENGTH_SHORT).show()
            return
        }

        val rebuyAmount = rebuyAmountString.toIntOrNull()
        if (rebuyAmount == null || rebuyAmount <= 0) {
            Toast.makeText(this, "Please enter a valid positive number for rebuy.", Toast.LENGTH_SHORT).show()
            return
        }

        currentPlayer?.let { player ->
            when (radioGroupRebuyType.checkedRadioButtonId) {
                R.id.radioButtonLoan -> {
                    player.loanBuyins.add(rebuyAmount)
                    player.sumLoanBuyin += rebuyAmount
                    Toast.makeText(this, "${player.name} rebought ${rebuyAmount} (Loan)", Toast.LENGTH_SHORT).show()
                }
                R.id.radioButtonCash -> {
                    player.cashBuyins.add(rebuyAmount)
                    player.sumCashBuyin += rebuyAmount
                    currentHouseBalance += rebuyAmount
                    Toast.makeText(this, "${player.name} rebought ${rebuyAmount} (Cash)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Please select loan or cash.", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            val resultIntent = Intent().apply {
                putExtra(RESULT_EXTRA_PLAYER_UPDATED, player)
                putExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, currentHouseBalance)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
