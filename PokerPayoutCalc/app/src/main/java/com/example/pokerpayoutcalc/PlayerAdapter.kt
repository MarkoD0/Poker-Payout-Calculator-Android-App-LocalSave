package com.example.pokerpayoutcalc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// tipovi RecyclerView itema
const val ITEM_VIEW_TYPE_PLAYER = 0
const val ITEM_VIEW_TYPE_HOUSE = 1

// PlayerAdapter za stavljanje ListItem u RecyclerView.
class PlayerAdapter(
    private val items: List<ListItem>,
    private val onRebuyClick: (Player) -> Unit,
    private val onInfoClick: (Player) -> Unit,
    private val onCashoutClick: (Player) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerItemLayout: ConstraintLayout = itemView.findViewById(R.id.playerItemLayout)
        val playerName: TextView = itemView.findViewById(R.id.textViewPlayerName)
        val playerAllBuyins: TextView = itemView.findViewById(R.id.textViewPlayerAllBuyins)
        val buttonInfo: Button = itemView.findViewById(R.id.buttonInfo)
        val buttonCashout: Button = itemView.findViewById(R.id.buttonCashout)
        val buttonRebuy: Button = itemView.findViewById(R.id.buttonRebuy)
        val playerActionButtonsLayout: ViewGroup = itemView.findViewById(R.id.playerActionButtonsLayout)
    }

    class HouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val houseName: TextView = itemView.findViewById(R.id.textViewHouseName)
        val houseBalance: TextView = itemView.findViewById(R.id.textViewHouseBalance)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Player -> ITEM_VIEW_TYPE_PLAYER
            is House -> ITEM_VIEW_TYPE_HOUSE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_PLAYER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.player_list_item, parent, false)
                PlayerViewHolder(view)
            }
            ITEM_VIEW_TYPE_HOUSE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.house_list_item, parent, false)
                HouseViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // prikazi podatke
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_PLAYER -> {
                val player = items[position] as Player
                val playerHolder = holder as PlayerViewHolder
                playerHolder.playerName.text = player.name
                playerHolder.playerAllBuyins.text = String.format(Locale.US, "All buyins: %d", player.sumCashBuyin + player.sumLoanBuyin)

                val context = holder.itemView.context
                if (player.isPlaying) {
                    if (player.isVip) {
                        playerHolder.playerItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.player_vip_playing_bg))
                    } else {
                        playerHolder.playerItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.player_non_vip_playing_bg))
                    }
                    playerHolder.playerActionButtonsLayout.visibility = View.VISIBLE
                    playerHolder.buttonInfo.isEnabled = true
                    playerHolder.buttonCashout.isEnabled = true
                    playerHolder.buttonRebuy.isEnabled = true
                } else {
                    playerHolder.playerItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.player_not_playing_bg))
                    playerHolder.playerActionButtonsLayout.visibility = View.GONE
                }

                // listeneri za dugmice
                playerHolder.buttonRebuy.setOnClickListener {
                    onRebuyClick(player)
                }
                playerHolder.buttonInfo.setOnClickListener {
                    onInfoClick(player)
                }
                playerHolder.buttonCashout.setOnClickListener {
                    onCashoutClick(player)
                }
            }
            ITEM_VIEW_TYPE_HOUSE -> {
                val house = items[position] as House
                val houseHolder = holder as HouseViewHolder
                houseHolder.houseName.text = house.name
                houseHolder.houseBalance.text = String.format(Locale.US, "Balance: %d", house.balance)
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }
        }
    }

    // vrati podatke
    override fun getItemCount(): Int {
        return items.size
    }
}
