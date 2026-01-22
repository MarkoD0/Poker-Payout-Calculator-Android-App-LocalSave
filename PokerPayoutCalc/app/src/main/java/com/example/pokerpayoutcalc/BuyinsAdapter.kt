package com.example.pokerpayoutcalc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast // For placeholder Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// Adapter for displaying a list of integer buy-in amounts.
// It includes a delete button for each item (functionality to be added later).
class BuyinsAdapter(
    private val buyinList: List<Int>, // The list of buy-in amounts (cash or loan)
    private val buyinType: String,    // "Cash" or "Loan" for context in toasts/logs
    private val onDeleteClick: (Int, Int) -> Unit // Callback: (position, value)
) : RecyclerView.Adapter<BuyinsAdapter.BuyinViewHolder>() {

    // ViewHolder for an individual buy-in item.
    class BuyinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewBuyinValue: TextView = itemView.findViewById(R.id.textViewBuyinValue)
        val buttonDeleteBuyin: Button = itemView.findViewById(R.id.buttonDeleteBuyin)
    }

    // Called when RecyclerView needs a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_buyin_list_item, parent, false)
        return BuyinViewHolder(view)
    }

    // Called to bind data to an existing ViewHolder.
    override fun onBindViewHolder(holder: BuyinViewHolder, position: Int) {
        val buyinValue = buyinList[position]
        holder.textViewBuyinValue.text = String.format(Locale.US, "%d", buyinValue)

        // Set click listener for the delete button.
        // For now, it just shows a Toast. Actual deletion logic will come later.
        holder.buttonDeleteBuyin.setOnClickListener {
            // Call the lambda to notify the activity about the deletion attempt
            onDeleteClick(position, buyinValue)
        }
    }

    // Returns the total number of items in the list.
    override fun getItemCount(): Int {
        return buyinList.size
    }
}
