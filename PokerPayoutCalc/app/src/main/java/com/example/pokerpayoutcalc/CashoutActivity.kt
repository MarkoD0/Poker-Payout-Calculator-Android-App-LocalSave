package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

// Prosledjivanje podataka
const val EXTRA_PLAYER_CASHOUT = "com.example.pokerpayoutcalc.PLAYER_CASHOUT"
const val EXTRA_HOUSE_BALANCE_CASHOUT_IN = "com.example.pokerpayoutcalc.HOUSE_BALANCE_CASHOUT_IN"
const val RESULT_PLAYER_UPDATED_FROM_CASHOUT = "com.example.pokerpayoutcalc.PLAYER_UPDATED_FROM_CASHOUT"
const val RESULT_HOUSE_BALANCE_UPDATED_FROM_CASHOUT = "com.example.pokerpayoutcalc.HOUSE_BALANCE_UPDATED_FROM_CASHOUT"


class CashoutActivity : AppCompatActivity() {

    private lateinit var textViewCashoutPlayerName: TextView
    private lateinit var textViewCashoutAllBuyins: TextView
    private lateinit var radioGroupCashoutType: RadioGroup
    private lateinit var radioButtonFromLoans: RadioButton
    private lateinit var radioButtonFromCash: RadioButton
    private lateinit var editTextCashoutAmount: EditText
    private lateinit var buttonConfirmCashout: Button
    private lateinit var buttonCashoutBack: Button

    private var currentPlayer: Player? = null
    private var currentHouseBalance: Int = 0

    // omda se cuva
    private lateinit var playerRepository: PlayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cashout)

        // inicijalizacija vjuova
        textViewCashoutPlayerName = findViewById(R.id.textViewCashoutPlayerName)
        textViewCashoutAllBuyins = findViewById(R.id.textViewCashoutAllBuyins)
        radioGroupCashoutType = findViewById(R.id.radioGroupCashoutType)
        radioButtonFromLoans = findViewById(R.id.radioButtonFromLoans)
        radioButtonFromCash = findViewById(R.id.radioButtonFromCash)
        editTextCashoutAmount = findViewById(R.id.editTextCashoutAmount)
        buttonConfirmCashout = findViewById(R.id.buttonConfirmCashout)
        buttonCashoutBack = findViewById(R.id.buttonCashoutBack)

        playerRepository = PlayerRepository(this)

        // dohvati podatke
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_CASHOUT, Player::class.java)
        } else {
            @Suppress("DEPRECATION")
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_CASHOUT) as? Player
        }
        currentHouseBalance = intent.getIntExtra(EXTRA_HOUSE_BALANCE_CASHOUT_IN, 0)

        // prikazi info
        currentPlayer?.let { player ->
            textViewCashoutPlayerName.text = player.name
            textViewCashoutAllBuyins.text = String.format(Locale.US, "All Buyins: %d", player.sumCashBuyin + player.sumLoanBuyin)

            if (player.isVip) {
                radioButtonFromCash.isChecked = true
            } else {
                radioButtonFromLoans.isChecked = true
            }

        } ?: run {
            Toast.makeText(this, "Error: Player data not found!", Toast.LENGTH_LONG).show()
            finish()
        }

        // listeneri
        editTextCashoutAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateConfirmButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        radioGroupCashoutType.setOnCheckedChangeListener { _, _ ->
            updateConfirmButtonState()
        }

        updateConfirmButtonState()


        buttonConfirmCashout.setOnClickListener {
            handleConfirmCashout()
        }

        buttonCashoutBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // cashout dugme
    private fun updateConfirmButtonState() {
        val cashoutAmount = editTextCashoutAmount.text.toString().toIntOrNull() ?: 0
        val isFromCashSelected = radioGroupCashoutType.checkedRadioButtonId == R.id.radioButtonFromCash
        val playerLoanBuyinSum = currentPlayer?.sumLoanBuyin ?: 0

        val netPayout = cashoutAmount - playerLoanBuyinSum

        buttonConfirmCashout.isEnabled = !(isFromCashSelected && netPayout > currentHouseBalance)

        if (isFromCashSelected && netPayout > currentHouseBalance) {
            Toast.makeText(this, "House balance not sufficient for this cashout!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleConfirmCashout() {
        val cashoutAmount = editTextCashoutAmount.text.toString().toIntOrNull() ?: 0

        currentPlayer?.let { player ->
            val isFromLoansSelected = radioGroupCashoutType.checkedRadioButtonId == R.id.radioButtonFromLoans
            val isFromCashSelected = radioGroupCashoutType.checkedRadioButtonId == R.id.radioButtonFromCash

            val playerLoanBuyinSum = player.sumLoanBuyin

            val netPayoutForCashPayment = cashoutAmount - playerLoanBuyinSum

            if (isFromCashSelected && netPayoutForCashPayment > currentHouseBalance) {
                Toast.makeText(this, "House balance not sufficient for this cashout!", Toast.LENGTH_SHORT).show()
                return
            }

            // dijalog da se potvrdi
            showConfirmationDialog("Confirm Cashout?", "Are you sure you want to confirm this cashout of $cashoutAmount?", {
                player.isPlaying = false

                var finalMessage = ""

                if (isFromLoansSelected) {
                    player.balance = cashoutAmount - playerLoanBuyinSum

                    if (player.balance < 0) {
                        val loanRemaining = -player.balance
                        finalMessage = String.format(
                            Locale.US,
                            "When the game finishes, payouts and loan settlements will be automatically calculated for each player. \n Player %s loan is %d",
                            player.name,
                            loanRemaining
                        )
                    } else if (player.balance == 0) {
                        finalMessage = String.format(
                            Locale.US,
                            "Player %s balance is 0. There are no loans or payouts to settle",
                            player.name
                        )
                    } else { // player.balance > 0
                        finalMessage = String.format(
                            Locale.US,
                            "When the game finishes, payouts and loan settlements will be automatically calculated for each player. \n Player %s has earned %d",
                            player.name,
                            player.balance
                        )
                    }
                } else if (isFromCashSelected) {
                    val netDifference = cashoutAmount - playerLoanBuyinSum

                    if (netDifference < 0) {
                        player.balance = netDifference
                        val loanRemaining = -netDifference
                        finalMessage = String.format(
                            Locale.US,
                            "When the game finishes, payouts and loan settlements will be automatically calculated for each player. \n Player %s loan is %d",
                            player.name,
                            loanRemaining
                        )
                    } else if (netDifference == 0) {
                        // Player breaks even
                        player.balance = 0
                        finalMessage = String.format(
                            Locale.US,
                            "Player %s balance is 0. There are no loans or payouts to settle",
                            player.name
                        )
                    } else {
                        player.balance = 0
                        currentHouseBalance -= netDifference
                        finalMessage = String.format(
                            Locale.US,
                            "Player %s receives %d in cash as payout.",
                            player.name,
                            netDifference
                        )
                    }
                }

                saveAllPlayerDataFromCashoutActivity()
                Toast.makeText(this, "Cashout processed and saved!", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent().apply {
                    putExtra(RESULT_PLAYER_UPDATED_FROM_CASHOUT, player)
                    putExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_CASHOUT, currentHouseBalance)
                    putExtra("cashout_message", finalMessage)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            })
        }
    }

    // konirm dijalog
    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("YES") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("BACK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // sacuvaj sve
    private fun saveAllPlayerDataFromCashoutActivity() {
        currentPlayer?.let { player ->
            val allPlayers = playerRepository.loadPlayers()
            val indexToUpdate = allPlayers.indexOfFirst { it.name == player.name }
            if (indexToUpdate != -1) {
                allPlayers[indexToUpdate] = player
            } else {
                Toast.makeText(this, "Warning: Player not found in full list for re-save after cashout.", Toast.LENGTH_SHORT).show()
            }
            playerRepository.savePlayersAndHouseBalance(allPlayers, currentHouseBalance)
        }
    }
}
