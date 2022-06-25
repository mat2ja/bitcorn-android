package android.tvz.hr.bitcorn.db

import java.util.*

data class Coin(
    var id: String,
    var symbol: String,
    var name: String,
    var image: String,
    val market_cap_rank: Int,
    var current_price: Double,
    var market_cap: Double,
    var price_change_24h: Double,
    val price_change_percentage_24h: Double,
    val circulating_supply: Double,
    val total_supply: Double,
    var max_supply: Double,
    var ath: Double,
) {
    override fun toString(): String {
        return "$name ($symbol)"
    }

}