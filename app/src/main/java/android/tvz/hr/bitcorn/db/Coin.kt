package android.tvz.hr.bitcorn.db

data class Coin(
   var id: String,
    var symbol: String,
    var name: String
) {
    override fun toString(): String {
        return "$name ($symbol)"
    }

}