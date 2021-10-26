package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.Json
import kotlinx.android.synthetic.main.content_simulatorpage.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Thread.sleep

data class Command(@Json(name = "Aileron") val aileron: Double,
                   @Json(name = "Rudder") var rudder: Double,
                   @Json(name = "Elevator")  var elevator: Double,
                   @Json(name = "Throttle") var throttle: Double)

class SimulatorPage : AppCompatActivity() {
    private var stop : Boolean = false
    private var url :String ?=null
    private var rudderText:TextView ?=null
    private var throttleText: TextView ?=null
    private var aileronText:TextView ?=null
    private var elevatorText: TextView ?=null
    private var imageView: ImageView?=null
    private var toastMessage : Toast?=null

    //Override function for on progress of rudder
    fun newOnProgressChangedRudder(progress: Int){
        rudderText?.apply {
            val tempText :String = if (progress >= 50) {
                ((progress.toFloat() - 50) / 50.0).toString()
            } else {
                ((50 - progress.toFloat()) / -50.0).toString()
            }
            if(checkChange(tempText,1) ==1){
                CoroutineScope(Dispatchers.IO).launch {sendJson()}
            }
            text = tempText
        }
    }

    //Override function for on progress of throttle
    fun newOnProgressChangedThrottle(progress: Int){
        throttleText?.apply {
            val tempText :String= (progress.toFloat() / 100.0).toString()
            if(checkChange(tempText,2) ==1){
                CoroutineScope(Dispatchers.IO).launch {sendJson()}
            }
            text = tempText
        }
    }

    //Change Listeners of rudder and throttle
    fun changeListeners(){
        val rudder = findViewById<SeekBar>(R.id.rudderBar)
        val throttle = findViewById<SeekBar>(R.id.ThrottleBar)
        rudder.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // You can have your own calculation for progress
                newOnProgressChangedRudder(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        throttle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // You can have your own calculation for progress
                newOnProgressChangedThrottle(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    //onCreate Function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val js = JoyStick(this, this)
        js.initSim(this)
        setContentView(R.layout.activity_simulatorpage)
        CoroutineScope(Dispatchers.IO).launch {init()}
        changeListeners()
        val textView: TextView = findViewById(R.id.showUrl)
        url = intent.getStringExtra("url")?.toString()
        textView.text = url
        imageView=findViewById(R.id.imageView2)
        val data = intent.getByteArrayExtra("FlightImage")
        val v =data?.size?.let{BitmapFactory.decodeByteArray(data,0,it)}
        runOnUiThread{imageView?.setImageBitmap(v)}
    }

    //onStop Function
    override fun onStop() {
        super.onStop()
        stop = true
        try {
            toastMessage?.cancel()
        }
        catch(exception:java.lang.Exception){

        }
    }

    //onStart Function
    override fun onStart() {
        super.onStart()
        stop = false
        displayImage()
    }

    //Function for getting image from server
    private fun displayImage(){
        Thread{
            while (!stop){
                CoroutineScope(Dispatchers.IO).launch {getImage()}
                sleep(500)
            }
        }.start()
    }

    //Initialization function
    private fun init(){
        aileronText = findViewById(R.id.AileronText)
        elevatorText = findViewById(R.id.ElevatorText)
        rudderText = findViewById(R.id.rudderText)
        throttleText = findViewById(R.id.ThrottleText)
    }

    //Function in charge of printing toasts
    fun errorMessage(message:String){
        runOnUiThread {
            try{
                toastMessage?.cancel()
                toastMessage=Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_LONG
                )
                toastMessage?.show()
            }
            catch (exception:Exception){}

        }
    }

    //override function for onResponse of post
    fun overrideOnResponsePost(response: Response<Void>){
        when {
            response.isSuccessful -> {
                return
            }
            response.code() == 404 -> {
                errorMessage("Oops! Something Is Wrong, Please Try Reconnecting")
            }
            response.code()== 500 -> {
                errorMessage("Oops! the simulator disconnected, Please Try Reconnecting")
            }
            response.code()== 501 -> {
                errorMessage("Oops! Something Is Wrong, can't set data in simulator," +
                        " Please Try Reconnecting")
            }
            else -> {
                errorMessage("Could Not Set Values")
            }
        }
    }

    //Function for sending Json
    fun sendJson() {
        val command = Command(rudder = rudderText?.text.toString().toDouble(),
            throttle = throttleText?.text.toString().toDouble(),
            elevator = elevatorText?.text.toString().toDouble(),
            aileron = aileronText?.text.toString().toDouble())
        api!!.postJson(command).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                overrideOnResponsePost(response)
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                errorMessage("Could Not Set Values")
            }
        })
    }

    //override Funcion for onResponse of GET
    fun overrideOnResponseGet(response: Response<ResponseBody>){
        if(!response.isSuccessful && !stop) {
            errorMessage("Error code "+response.code().toString()+", Please Try Reconnecting")
        }
        else if (!response.isSuccessful && stop){
            toastMessage?.cancel()
        }
        else{
            val data = response.body()!!.bytes()
            val v = data.size.let { BitmapFactory.decodeByteArray(data, 0, it) }
            runOnUiThread { imageView2.setImageBitmap(v) }
        }
    }

    //Funcion in charge of getting image from server
    private fun getImage() {
        api!!.getImg().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                overrideOnResponseGet(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                errorMessage("Unable to connect with given IP/PORT, Please Try Reconnecting")
            }
        })

    }

    //Function in charge of checking if change in values is large enough to warrant post command
    fun checkChange(newdate:String, flag: Int): Int {
        val newInt : Double = newdate.toDouble()
        var lastInt = 0.0
        val temp :Double
        when(flag) {
            1 -> lastInt = rudderText?.text.toString().toDouble()
            2 -> lastInt = throttleText?.text.toString().toDouble()
            3-> lastInt = elevatorText?.text.toString().toDouble()
            4 -> lastInt = aileronText?.text.toString().toDouble()
        }
        if(flag == 2){
            temp = lastInt - newInt
            when {
                temp >0.01 -> return 1
                temp<-0.01 -> return 1
                else -> return 0
            }
        }else{
            if(lastInt>newInt){
                temp = lastInt - newInt
                if(temp > 0.02) return 1
                else return 0
            }else {
                temp = newInt - lastInt
                if(temp >0.02) return 1
                else return 0
            }
        }
    }
}