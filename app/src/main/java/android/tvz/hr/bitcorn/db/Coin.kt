package android.tvz.hr.bitcorn.db

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Coin(
    @SerializedName("id") @Expose var id: String,
    @SerializedName("symbol") @Expose var symbol: String,
    @SerializedName("name") @Expose var name: String
) {
    override fun toString(): String {
        return "$name ($symbol)"
    }

}