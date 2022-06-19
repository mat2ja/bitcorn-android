package android.tvz.hr.bitcorn.db

data class Coin(
    var id: String,
    var symbol: String,
    var name: String,
    var image: String,
    var current_price: Double,
    var market_cap: Number,
    var price_change_24h: Double,
    var ath: Double
) {
    override fun toString(): String {
        return "$name ($symbol)"
    }

}