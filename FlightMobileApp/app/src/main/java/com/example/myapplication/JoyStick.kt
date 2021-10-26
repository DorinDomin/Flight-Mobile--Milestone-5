package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt


class JoyStick : SurfaceView, OnTouchListener, SurfaceHolder.Callback {
    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private var baseRadius: Float = 0F
    private var hatRadius: Float = 0F
    private var borderRadius: Float = 0F
    private lateinit var joystickCallback: JoyStickListener
    private lateinit var con: Context
    private lateinit var simupage: SimulatorPage
    private lateinit var b: Bitmap
    private var flag: Boolean = false
    private var updatedX: Float = 0F
    private var updatedY: Float = 0F
    private lateinit var aileronString: String
    private lateinit var elevatorString: String
    private lateinit var myCanvas: Canvas
    private var colors: Paint = Paint()
    private val tmp = colors.style
    // init
    fun initSim(sim: SimulatorPage) {
        simupage = sim
    }
    // constructor
    constructor(context: Context?, sim: SimulatorPage?) : super(context) {
        // set context for use
        if (context != null) {
            con = context
        }
        if (sim != null) {
            simupage = sim
        }
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoyStickListener) {
            joystickCallback = context
        }
    }
    // constructor
    constructor(context: Context?) : super(context) {
        // set context for use
        if (context != null) {
            con = context
        }
        simupage = context as SimulatorPage
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoyStickListener) {
            joystickCallback = context
        }
    }
    // constructor
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        // set context for use
        if (context != null) {
            con = context
        }
        simupage = context as SimulatorPage
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoyStickListener) {
            joystickCallback = context
        }
    }
    // constructor
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // set context for use
        if (context != null) {
            con = context
        }
        simupage = context as SimulatorPage
        holder.addCallback(this)
        setOnTouchListener(this)
        if (context is JoyStickListener) {
            joystickCallback = context
        }
    }
    // set vals for drawing
    private fun setUpDimensions() {
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
        baseRadius = 443F
        hatRadius = 190F
        borderRadius = baseRadius - (hatRadius)
        b = BitmapFactory.decodeResource(resources, R.drawable.roundjoystick)
    }
    // draw the circles of the joyStick
    private fun drawBaseCircles(){
        colors.style = Paint.Style.STROKE
        colors.strokeWidth = 10F
        myCanvas.drawColor(Color.CYAN, PorterDuff.Mode.CLEAR) // base circle
        colors.setARGB(255, 38, 38, 38)
        myCanvas.drawCircle(centerX, centerY, baseRadius, colors)
        colors.setARGB(255, 38, 38, 38)
        colors.style = Paint.Style.STROKE
        colors.strokeWidth = 8F
        myCanvas.drawCircle(447F, 451F, borderRadius, colors)
    }
    // draw the arrows of the joyStick
    private fun drawArrows(){
        colors.style = tmp
        colors.setARGB(255, 204, 204, 204)
        val arrow: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.right)
        myCanvas.drawBitmap(
            arrow, centerX + baseRadius - 5 * (hatRadius / 6),
            centerY - (hatRadius / 3), colors
        )
        val leftArrow: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.left)
        myCanvas.drawBitmap(
            leftArrow, centerX - baseRadius + 1 * (hatRadius / 6),
            centerY - (hatRadius / 3), colors
        )
        val upArrow: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.up)
        myCanvas.drawBitmap(
            upArrow, centerX - 2 * (hatRadius / 6),
            centerY - 10 * (baseRadius / 11), colors
        )
        val downArrow: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.down)
        myCanvas.drawBitmap(
            downArrow, centerX - 2 * (hatRadius / 6),
            centerY + 6 * (baseRadius / 10), colors
        )
    }
    // draw joyStick
    private fun drawJoyStick(newX: Float, newY: Float) {
        if (holder.surface.isValid) {
            myCanvas = this.holder.lockCanvas()
            colors = Paint()
            // draw circles
            drawBaseCircles()
            // draw arrows
            drawArrows()
            // draw joystick
            colors.setShadowLayer(80F, 0F, 0F, Color.BLUE)
            colors.setARGB(255, 204, 204, 204)
            myCanvas.drawCircle(newX - 8, newY - 4, hatRadius, colors)
            myCanvas.drawBitmap(
                b, newX - (baseRadius - hatRadius) + 30,
                newY - (baseRadius - hatRadius) + 30, colors
            )
            holder.unlockCanvasAndPost(myCanvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        setUpDimensions()
        drawJoyStick(centerX, centerY)
        val sfvTrack = findViewById<View>(R.id.joystick) as SurfaceView
        sfvTrack.setZOrderOnTop(true)
        val sfhTrackHolder = sfvTrack.holder
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {}
    // set touch event for joyStick
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v != null && v == this && event != null) {
            val displacement: Float = sqrt(
                ((event.x - centerX).pow(2)) + (event.y - centerY).pow(2)
            ) // calculate path length
            if (event.action != MotionEvent.ACTION_UP) {
                if (displacement <= borderRadius) { // inner circle
                    flag = true
                    updatedX = event.x
                    updatedY = event.y
                    drawJoyStick(event.x, event.y)
                } else {// outside the inner circle
                    if (!flag) { // move only if pressed on joystick
                        return true
                    }
                    calConstrainedParams(event, displacement)// calculate relative new placement
                }
            } else {
                flag = false
                drawJoyStick(centerX, centerY)
                updatedX = centerX
                updatedY = centerY
            }

            aileronString = calParamText(updatedX, updatedY, 1)// calculate aileron
            elevatorString = calParamText(updatedX, updatedY, 2)// calculate elevator
            updateParamText(aileronString, elevatorString)
        }
        return true
    }
    // calculate new placement for joyStick
    private fun calConstrainedParams(event: MotionEvent?, displacement: Float) {
        if (event != null) {
            val ratio: Float = (borderRadius) / displacement
            val constrainedX = centerX + (event.x - centerX) * ratio//new x
            val constrainedY = centerY + (event.y - centerY) * ratio// new y
            drawJoyStick(constrainedX, constrainedY)
            updatedX = constrainedX
            updatedY = constrainedY
        }
    }
    // aileron & elevator new text-vals
    private fun calParamText(x: Float, y: Float, index: Int): String {
        if (x != centerX && y != centerY) {
            if (index == 1) {
                // case AlirString
                return String.format("%.2f", ((((x - centerX) / borderRadius))))
            }
            // case EleString
            return String.format("%.2f", (((((y - centerY) / (borderRadius)) * -1.0))))
        } else {
            return "0"
        }
    }
    // update text for elevator & aileron
    private fun updateParamText(ail: String, eli: String) {
        if ((simupage.checkChange(ail, 4)) == 1 ||
            (simupage.checkChange(eli, 3)) == 1
        ) {
            CoroutineScope(Dispatchers.IO).launch { simupage.sendJson() }
        }
        (con as Activity).findViewById<TextView>(R.id.AileronText)?.apply {
            text =
                ail
        }
        (con as Activity).findViewById<TextView>(R.id.ElevatorText)?.apply {
            text =
                eli
        }

    }

    interface JoyStickListener
}