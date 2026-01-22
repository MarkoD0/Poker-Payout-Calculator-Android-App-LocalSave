package com.example.pokerpayoutcalc

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CashoutResultActivity : AppCompatActivity() {

    private lateinit var textViewCashoutResultMessage: TextView
    private lateinit var buttonReturnToMain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cashout_result)

        textViewCashoutResultMessage = findViewById(R.id.textViewCashoutResultMessage)
        buttonReturnToMain = findViewById(R.id.buttonReturnToMain)

        // poruka
        val message = intent.getStringExtra("cashout_message") ?: "Cashout operation completed."
        textViewCashoutResultMessage.text = message

        buttonReturnToMain.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
