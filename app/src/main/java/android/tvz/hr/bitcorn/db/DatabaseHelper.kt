package android.tvz.hr.bitcorn.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(

    context,
    DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "coin_db"
        const val TABLE_COINS = "coins"

        const val KEY_ID = "id"
        const val KEY_SYMBOL = "symbol"
        const val KEY_NAME = "name"

        const val CREATE_TABLE_COINS = ("CREATE TABLE "
                + TABLE_COINS + "(" + KEY_ID + " STRING PRIMARY KEY," + KEY_SYMBOL
                + " STRING," + KEY_NAME
                + " STRING" + ")")
    }

    @SuppressLint("SQLiteString")
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_COINS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_COINS")

        onCreate(db)
    }

    fun getCoins(): MutableList<Coin> {
        val coins: MutableList<Coin> = ArrayList<Coin>()
        val selectQuery = "SELECT * FROM $TABLE_COINS;"
        val db = this.readableDatabase
        val c = db.rawQuery(selectQuery, null)

        if (c.moveToFirst()) {
            do {
                coins.add(getCoinRow(c))
            } while (c.moveToNext())
        }
        return coins
    }


    @SuppressLint("Range")
    fun getCoinById(id: String?): Coin? {
        val db = this.readableDatabase
        val selectQuery = ("SELECT * FROM " + TABLE_COINS + " WHERE "
                + KEY_ID + " = " + id)
        val c = db.rawQuery(selectQuery, null)
        c?.moveToFirst()
        return getCoinRow(c)
    }

    @SuppressLint("Range")
    fun getCoinRow(c: Cursor): Coin {
        return Coin(
            c.getString(c.getColumnIndex(KEY_ID)),
            c.getString(c.getColumnIndex(KEY_SYMBOL)),
            c.getString(c.getColumnIndex(KEY_NAME)),
        )
    }

    fun createCoin(item: Coin): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_ID, item.id)
        values.put(KEY_SYMBOL, item.symbol)
        values.put(KEY_NAME, item.name)
        return db.insert(TABLE_COINS, null, values)
    }

}