package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Prosledjivanje podataka
const val EXTRA_HOUSE_BALANCE_IN = "com.example.pokerpayoutcalc.HOUSE_BALANCE_IN"
const val RESULT_EXTRA_NEW_PLAYER = "com.example.pokerpayoutcalc.NEW_PLAYER"
const val RESULT_EXTRA_HOUSE_BALANCE_OUT = "com.example.pokerpayoutcalc.HOUSE_BALANCE_OUT"


class AddPlayerActivity : AppCompatActivity() {

    private lateinit var editTextPlayerName: EditText
    private lateinit var checkBoxVip: CheckBox
    private lateinit var editTextInitialBuyinAmount: EditText
    private lateinit var radioGroupBuyinType: RadioGroup
    private lateinit var radioButtonLoan: RadioButton
    private lateinit var radioButtonCash: RadioButton
    private lateinit var buttonConfirmAddPlayer: Button
    private lateinit var buttonAddPlayerBack: Button

    private var currentHouseBalance: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_player)

        // inicijalizacija vjuova
        editTextPlayerName = findViewById(R.id.editTextPlayerName)
        checkBoxVip = findViewById(R.id.checkBoxVip)
        editTextInitialBuyinAmount = findViewById(R.id.editTextInitialBuyinAmount)
        radioGroupBuyinType = findViewById(R.id.radioGroupBuyinType)
        radioButtonLoan = findViewById(R.id.radioButtonLoan)
        radioButtonCash = findViewById(R.id.radioButtonCash)
        buttonConfirmAddPlayer = findViewById(R.id.buttonConfirmAddPlayer)
        buttonAddPlayerBack = findViewById(R.id.buttonAddPlayerBack)

        currentHouseBalance = intent.getIntExtra(EXTRA_HOUSE_BALANCE_IN, 0)

        buttonConfirmAddPlayer.setOnClickListener {
            handleConfirmAddPlayer()
        }

        buttonAddPlayerBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // logika
    private fun handleConfirmAddPlayer() {
        val playerName = editTextPlayerName.text.toString().trim()
        val isVip = checkBoxVip.isChecked
        val initialBuyinAmountString = editTextInitialBuyinAmount.text.toString()

        if (playerName.isBlank()) {
            Toast.makeText(this, "Please enter a player name.", Toast.LENGTH_SHORT).show()
            return
        }

        if (initialBuyinAmountString.isBlank()) {
            Toast.makeText(this, "Please enter an initial buy-in amount.", Toast.LENGTH_SHORT).show()
            return
        }

        val initialBuyinAmount = initialBuyinAmountString.toIntOrNull()
        if (initialBuyinAmount == null || initialBuyinAmount <= 0) {
            Toast.makeText(this, "Please enter a valid positive number for initial buy-in.", Toast.LENGTH_SHORT).show()
            return
        }

        val newPlayer = Player(
            name = playerName,
            isVip = isVip,
            balance = 0,
            sumCashBuyin = 0,
            cashBuyins = mutableListOf(),
            sumLoanBuyin = 0,
            loanBuyins = mutableListOf(),
            isPlaying = true
        )

        when (radioGroupBuyinType.checkedRadioButtonId) {
            R.id.radioButtonLoan -> {
                newPlayer.loanBuyins.add(initialBuyinAmount)
                newPlayer.sumLoanBuyin += initialBuyinAmount
                Toast.makeText(this, "Added ${playerName} with ${initialBuyinAmount} (Loan)", Toast.LENGTH_SHORT).show()
            }
            R.id.radioButtonCash -> {
                newPlayer.cashBuyins.add(initialBuyinAmount)
                newPlayer.sumCashBuyin += initialBuyinAmount
                currentHouseBalance += initialBuyinAmount
                Toast.makeText(this, "Added ${playerName} with ${initialBuyinAmount} (Cash)", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Please select loan or cash for initial buy-in.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // apdejtuj i sacuvaj
        val resultIntent = Intent().apply {
            putExtra(RESULT_EXTRA_NEW_PLAYER, newPlayer)
            putExtra(RESULT_EXTRA_HOUSE_BALANCE_OUT, currentHouseBalance)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
