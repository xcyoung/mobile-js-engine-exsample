package me.xcyoung.js.engine

import android.annotation.SuppressLint
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.cash.quickjs.QuickJs
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val quickJs: QuickJs by lazy {
        val quickJs = QuickJs.create()
        val testJs = assetsToStr()
        quickJs.set("deviceInfo", DeviceInfoInterface::class.java, DeviceInfo())
        quickJs.set("database", DatabaseInterface::class.java, Database())
        quickJs.evaluate(testJs)
        quickJs
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun assetsToStr(): String {
        val inputStream = assets.open("test.js")

        val byteArrayOutputStream = ByteArrayOutputStream()
        val data = ByteArray(4096)
        var count = inputStream.read(data, 0, 4096)
        while (count != -1) {
            byteArrayOutputStream.write(data, 0, count)
            count = inputStream.read(data, 0, 4096)
        }

        val str = String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"))
        inputStream.close()
        byteArrayOutputStream.close()
        return str
    }

    override fun onDestroy() {
        super.onDestroy()
        quickJs.close()
    }

    interface DeviceInfoInterface {
        fun platform(): String
    }

    class DeviceInfo: DeviceInfoInterface {
        override fun platform(): String = "Android"
    }

    interface DatabaseInterface {
        fun fetchOne(sql: String): String
    }

    class Database: DatabaseInterface {
        private val databaseHelper = DatabaseHelper(App.application)
        @SuppressLint("Range")
        override fun fetchOne(sql: String): String {
            val db = databaseHelper.readableDatabase
            try {
                val cursor = db.rawQuery(sql, emptyArray())
                if (cursor.count == 0) {
                    if (!cursor.isClosed) cursor.close()
                    return "{}"
                }

                cursor.moveToFirst()
                val map = hashMapOf<String, Any>()
                cursor.columnNames.forEach {
                    when (cursor.getType(cursor.getColumnIndex(it))) {
                        Cursor.FIELD_TYPE_INTEGER -> map[it] =
                            cursor.getInt(cursor.getColumnIndex(it))
                        Cursor.FIELD_TYPE_STRING -> map[it] =
                            cursor.getString(cursor.getColumnIndex(it))
                        Cursor.FIELD_TYPE_FLOAT -> map[it] =
                            cursor.getFloat(cursor.getColumnIndex(it))
                        else -> {

                        }
                    }
                }
                if (!cursor.isClosed) cursor.close()
                db.close()
                val gson = Gson()
                return gson.toJson(map)
            } catch (e: Exception) {
                return "{}"
            }
        }
    }
}