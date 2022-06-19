package android.tvz.hr.bitcorn.retrofit

import android.tvz.hr.bitcorn.db.Coin
import retrofit2.Call
import retrofit2.http.GET

interface CoinServiceInterface {
    @GET("/coins")
    fun fetchCoins(): Call<MutableList<Coin>>
}