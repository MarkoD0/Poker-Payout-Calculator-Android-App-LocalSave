package com.example.pokerpayoutcalc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ListItem {
    val name: String
}

@Parcelize
data class Player(
    override val name: String,
    var balance: Int = 0,
    var sumCashBuyin: Int = 0,
    val cashBuyins: MutableList<Int> = mutableListOf(),
    var sumLoanBuyin: Int = 0,
    val loanBuyins: MutableList<Int> = mutableListOf(),
    var isVip: Boolean = false,
    var isPlaying: Boolean = true
) : ListItem, Parcelable


@Parcelize
data class House(
    override val name: String = "HOUSE",
    var balance: Int = 0
) : ListItem, Parcelable
