package android.tvz.hr.bitcorn.retrofit

import android.tvz.hr.bitcorn.db.Coin
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface CoinServiceInterface {
    @GET("/coins")
    fun fetchCoins(): Call<MutableList<Coin>>

    @GET("/coins/{id}")
    fun fetchCoin(@Path("id") todoId: String): Call<Coin>
}