package me.xcyoung.js.engine

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import app.cash.quickjs.QuickJs
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val quickJs: QuickJs by lazy {
        val quickJs = QuickJs.create()
        val testJs = assetsToStr()
        quickJs.set("deviceInfo", DeviceInfoInterface::class.java, DeviceInfo())
        quickJs.set("database", DatabaseInterface::class.java, Database(this.applicationContext))
        quickJs.evaluate(testJs)
        quickJs
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn).setOnClickListener {
            Thread {
                copyDatabase()
                val test = quickJs.get("test", TestInterface::class.java)
                val userJson = test.testFetchOne()
                Log.d(this.javaClass.simpleName, userJson)
            }.start()
        }
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

    private fun copyDatabase() {
        val dbOutput = File(applicationContext.getExternalFilesDir(null), "test.db")
        if (dbOutput.exists()) {
            return
        } else {
            dbOutput.createNewFile()
        }

        val inputStream = assets.open("test.db")

        val fileOutputStream = FileOutputStream(dbOutput)
        val data = ByteArray(4096)
        var count = inputStream.read(data, 0, 4096)
        while (count != -1) {
            fileOutputStream.write(data, 0, count)
            count = inputStream.read(data, 0, 4096)
        }
        inputStream.close()
        fileOutputStream.close()
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

    class Database(context: Context): DatabaseInterface {
        private val db: SupportSQLiteDatabase

        init {
            val factory = FrameworkSQLiteOpenHelperFactory()
            val builder =
                SupportSQLiteOpenHelper.Configuration.builder(context)
            val dbOutput = File(context.getExternalFilesDir(null), "test.db")
            builder.name(dbOutput.absolutePath)
            builder.callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {

                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {

                }
            })
            val helper = factory.create(builder.build())
            this.db = helper.readableDatabase
        }

        @SuppressLint("Range")
        override fun fetchOne(sql: String): String {
            try {
                val cursor = db.query(sql, emptyArray())
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

    interface TestInterface {
        fun testFetchOne(): String
    }
}