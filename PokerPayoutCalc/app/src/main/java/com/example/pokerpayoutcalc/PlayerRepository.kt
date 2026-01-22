package com.example.pokerpayoutcalc

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// PlayerRepository za cuvanje i ucitavanje podataka
class PlayerRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val PLAYERS_KEY = "players_list_json"
    private val HOUSE_BALANCE_KEY = "house_balance"
    private val KEY_IS_GAME_ENDED = "is_game_ended"

    // cuva listu igraca i HOUSE u SharedPreferences
    fun savePlayersAndHouseBalance(players: List<Player>, houseBalance: Int) {
        val json = gson.toJson(players)
        sharedPreferences.edit().putString(PLAYERS_KEY, json).apply()
        sharedPreferences.edit().putInt(HOUSE_BALANCE_KEY, houseBalance).apply()
    }

    // ucitava igrace iz SharedPreferences
    fun loadPlayers(): MutableList<Player> {
        val json = sharedPreferences.getString(PLAYERS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Player>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // ucitava HOUSE iz SharedPreferences
    fun loadHouseBalance(): Int {
        return sharedPreferences.getInt(HOUSE_BALANCE_KEY, 0) // Default to 0 if not found
    }

    // cuva game ended flag
    fun saveIsGameEnded(isEnded: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_GAME_ENDED, isEnded).apply()
    }

    // ucitava game ended flag
    fun loadIsGameEnded(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_GAME_ENDED, false) // Default to false if not found
    }

    // sve brise
    fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.remove(PLAYERS_KEY) // Remove the players list
        editor.putInt(HOUSE_BALANCE_KEY, 0) // Reset house balance to 0
        editor.putBoolean(KEY_IS_GAME_ENDED, false) // Reset game ended flag
        editor.apply() // Apply the changes
    }
}
