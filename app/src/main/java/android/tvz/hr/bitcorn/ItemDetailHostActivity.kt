package android.tvz.hr.bitcorn

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.tvz.hr.bitcorn.databinding.ActivityItemDetailBinding
import android.tvz.hr.bitcorn.db.Coin
import android.tvz.hr.bitcorn.db.DatabaseHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class ItemDetailHostActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val databaseHelper = DatabaseHelper(this)
        // Delete previous state
        databaseHelper.onUpgrade(databaseHelper.writableDatabase, 1, 2)

        val coins = getCoinsFromJSON(applicationContext)
        println(coins)

        coins.forEach { coin -> databaseHelper.createCoin(coin) }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_item_detail) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_item_detail)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun getCoinsFromJSON(context: Context): List<Coin> {
        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("coins.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            println("Error reading coins from json")
        }

        val listCointType = object : TypeToken<List<Coin>>() {}.type
        return Gson().fromJson(jsonString, listCointType)
    }
}