package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


@Entity(tableName = "recentURLs")
data class UsedURL(
    @PrimaryKey(autoGenerate = true)
    var index: Int,
    @ColumnInfo(name = "id")
    var id: Int,
    @ColumnInfo(name = "url")
    var url: String
)

@Dao
interface RecentUrlsDao {
    @Insert
    suspend fun insert(row: UsedURL)

    @Delete
    suspend fun delete(row: UsedURL)

    @Query("DELETE FROM recentURLs")
    suspend fun clearTable()

    @Query("DELETE FROM recentURLs")
    fun clearTableUnsuspended()

    @Query("SELECT url FROM recentURLs WHERE id = :key")
    fun getByID(key: Int): String?

}

@Database(entities = [UsedURL::class], version = 1, exportSchema = false)
abstract class RecentUrlsDatabase : RoomDatabase() {
    abstract val recentUrlsDao: RecentUrlsDao

    companion object {
        @Volatile
        private var INSTANCE: RecentUrlsDatabase? = null
        fun getInstance(context: Context): RecentUrlsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = newDatabase(context)
                }
                INSTANCE = instance
                return instance
            }
        }
    }
}

fun newDatabase(context: Context): RecentUrlsDatabase {
    return Room.databaseBuilder(
        context.applicationContext, RecentUrlsDatabase::class.java,
        "recent_urls_database"
    ).allowMainThreadQueries().build()
}

//IApi interface
interface IApi {
    @GET("/screenshot")
    fun getImg(): Call<ResponseBody>
    @POST("/api/Command")
    fun postJson(@Body command: Command): Call<Void>
}

val gson: Gson = GsonBuilder().setLenient().create()
var retrofit:Retrofit?=null
var api:IApi?=null

class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        textView = findViewById(R.id.editTextUrl)
    }

    //Handles displaying error message
    fun errorMessage(){
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                "Unable to connect with given IP/PORT",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Http request to get image from server
    private fun getImage(selectedURL: String) {
        try {
            retrofit = Retrofit.Builder().baseUrl(selectedURL).client(OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
            api = retrofit!!.create(IApi::class.java)
        } catch (e: java.lang.Exception) {
            errorMessage()
            return
        }
        api!!.getImg().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (!response.isSuccessful) {
                    errorMessage()
                    return
                }
                val intent = Intent(this@MainActivity, SimulatorPage::class.java)
                    .apply { putExtra("url", selectedURL).
                    putExtra("FlightImage", response.body()!!.bytes())
                }
                startActivity(intent)
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                errorMessage()
            }
        })
    }

    //Changes button visibility if it should not be visible
    private fun buttonClickabilityNo(){
        if (button1.text == "") {
            button1.isEnabled = false
            button1.isClickable = false
            button1.alpha = 0.5F
        }
        if (button2.text == "") {
            button2.isEnabled = false
            button2.isClickable = false
            button2.alpha = 0.5F
        }
        if (button3.text == "") {
            button3.isEnabled = false
            button3.isClickable = false
            button3.alpha = 0.5F
        }
        if (button4.text == "") {
            button4.isEnabled = false
            button4.isClickable = false
            button4.alpha = 0.5F
        }
        if (button5.text == "") {
            button5.isEnabled = false
            button5.isClickable = false
            button5.alpha = 0.5F
        }
    }

    //Changes button visibility if it should be visible
    private fun buttonClickabilityYes(){
        if (button1.text != "") {
            button1.isEnabled = true
            button1.isClickable = true
            button1.alpha = 1F
        }
        if (button2.text != "") {
            button2.isEnabled = true
            button2.isClickable = true
            button2.alpha = 1F
        }
        if (button3.text != "") {
            button3.isEnabled = true
            button3.isClickable = true
            button3.alpha = 1F
        }
        if (button4.text != "") {
            button4.isEnabled = true
            button4.isClickable = true
            button4.alpha = 1F
        }
        if (button5.text != "") {
            button5.isEnabled = true
            button5.isClickable = true
            button5.alpha = 1F
        }
    }
    private fun initialize() {
        val db = RecentUrlsDatabase.getInstance(this)
        //db.recentUrlsDao.clearTableUnsuspended()
        button5.text = (db.recentUrlsDao.getByID(5))
        button4.text = (db.recentUrlsDao.getByID(4))
        button3.text = (db.recentUrlsDao.getByID(3))
        button2.text = (db.recentUrlsDao.getByID(2))
        button1.text = (db.recentUrlsDao.getByID(1))
        buttonClickabilityNo()
    }

    //Handles lru in case of url that doesn't exist in lru
    private fun newUrl(URL : Array<String?>, pick: String){
        button5.text = URL[3]
        button4.text = URL[2]
        button3.text = URL[1]
        button2.text = URL[0]
        button1.text = pick
    }

    //Handles lru in case of url that exists in lru
    private fun oldUrl(URL : Array<String?>, pick: String, indexSame: Int){
        when (indexSame) {
            0 -> {
                button1.text = pick
            }
            1 -> {
                button2.text = URL[0]
                button1.text = pick
            }
            2 -> {
                button3.text = URL[1]
                button2.text = URL[0]
                button1.text = pick
            }
            3 -> {
                button4.text = URL[2]
                button3.text = URL[1]
                button2.text = URL[0]
                button1.text = pick
            }
            4 -> {
                button5.text = URL[3]
                button4.text = URL[2]
                button3.text = URL[1]
                button2.text = URL[0]
                button1.text = pick
            }
        }
    }

    //Handles LRU of urls
    private fun organizeConnect(pick: String) {
        //Gets Urls of buttons
        val listOfUrls: Array<String?> = arrayOfNulls(5)
        listOfUrls[0] = (button1.text.toString())
        listOfUrls[1] = (button2.text.toString())
        listOfUrls[2] = (button3.text.toString())
        listOfUrls[3] = (button4.text.toString())
        listOfUrls[4] = (button5.text.toString())

        var flag = false
        var indexSame = 0
        for ((index, url) in listOfUrls.withIndex()) {
            if (url == pick) {
                flag = true
                indexSame = index
            }
        }
        //If url is not in lru
        if (!flag) {
            newUrl(listOfUrls, pick)
        //Otherwise it in lru
        } else {
            oldUrl(listOfUrls,pick,indexSame)
        }
        //Updates buttons accordingly
        buttonClickabilityNo()
        buttonClickabilityYes()
    }

    //Saves database
    private suspend fun saveDataBase() {
        val db = RecentUrlsDatabase.getInstance(this)
        try {
            db.recentUrlsDao.clearTable()
            db.recentUrlsDao.insert(UsedURL(10, 1, button1.text.toString()))
            db.recentUrlsDao.insert(UsedURL(11, 2, button2.text.toString()))
            db.recentUrlsDao.insert(UsedURL(12, 3, button3.text.toString()))
            db.recentUrlsDao.insert(UsedURL(13, 4, button4.text.toString()))
            db.recentUrlsDao.insert(UsedURL(14, 5, button5.text.toString()))
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "Error Saving To Database",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Changes textview when clicking on url button
    fun changeUrlFromButton(view: View) {
        val button: Button = view as Button
        textView?.text = button.text
    }

    //Handles the clicking of Connect Button
    fun connect(view:View) {

        val url: String = textView?.text.toString()

        //Checks if textbox is empty
        if (url == "") {
            textView?.error = "Please Enter URL"
            textView?.requestFocus()

        //Otherwise
        } else {
            textView?.error = null
            try{
                CoroutineScope(Dispatchers.IO).launch {withTimeout(10000L){getImage(url)} }
            }
            catch (exception : Exception){
                Toast.makeText(applicationContext, "TimeOut, Please Try Again",
                    Toast.LENGTH_LONG).show()
            }
            organizeConnect(url)
            try {
                CoroutineScope(Dispatchers.IO).launch {withTimeout(10000L){saveDataBase()}}
            }
            catch (exception : Exception){
                Toast.makeText(applicationContext, "TimeOut, Please Try Again",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}