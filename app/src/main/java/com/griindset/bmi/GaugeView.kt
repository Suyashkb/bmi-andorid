package com.griindset.bmi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlin.math.roundToInt

class GaugeView : RelativeLayout {

    private lateinit var mainBg: ImageView
    private lateinit var needle: RelativeLayout
    private lateinit var polygon: ImageView
    private lateinit var labelsLL: LinearLayout
    private lateinit var valueLabel: TextView
    private lateinit var genderLabel:TextView
    private lateinit var statusLabel: TextView

    var outerBezelColor: Int = Color.GRAY
    var innerBezelColor: Int = Color.WHITE
    var insideColor: Int = Color.WHITE
    var needleColor: Int = Color.TRANSPARENT
    var valueColor: Int = Color.WHITE
    var statusColor: Int = R.color.color_primary

    var outerBezelWidth = 2f
    var innerBezelWidth = 5f
    var segmentWidth = 0f
    var needleWidth = 23f
    var segmentColors = intArrayOf(Color.GRAY)

    var totalAngle = 270f
    var rotationAngle = -135f

    var path: Path = Path()
    var paint: Paint = Paint()
    var radiusPathRectF = RectF()
    var w = 0
    var h = 0

    constructor(context: Context?) : super(context) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {

        //create the needle RelativeLayout ViewGroup
        needle = RelativeLayout(context)
        needle.setBackgroundColor(needleColor)

        //create the mainBg ImageView
        mainBg = ImageView(context)
        mainBg.setImageResource(R.drawable.credit_score_meter)
        mainBg.setAdjustViewBounds(true)
        mainBg.setScaleType(ImageView.ScaleType.CENTER_INSIDE)

        //create the polygon ImageView
        polygon = ImageView(context)
        polygon.setImageResource(R.drawable.ic_needle)
        polygon.setAdjustViewBounds(true)
        polygon.setScaleType(ImageView.ScaleType.CENTER_INSIDE)

        //add the mainBg and needle as subviews and polygon as a subview of needle
        addView(mainBg)
        addView(needle)
        needle.addView(polygon)

        //create a Vertical LinearLayout ViewGroup to add the valueLabel and statusLabel as subviews
        labelsLL = LinearLayout(context)
        labelsLL.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        labelsLL.orientation = LinearLayout.VERTICAL
        addView(labelsLL)

        //create the valueLabel TextView and add it as a subview of labelsLL
        valueLabel = TextView(context)
        valueLabel.text = "0"
        valueLabel.setTextColor(valueColor)
        valueLabel.gravity = Gravity.CENTER
        valueLabel.setTypeface(valueLabel.typeface, Typeface.BOLD)
        valueLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        labelsLL.addView(valueLabel)


        statusLabel = TextView(context)
        statusLabel.text = ""
        statusLabel.setTextColor(resources.getColor(R.color.color_primary))
        statusLabel.gravity = Gravity.CENTER
        statusLabel.setTypeface(statusLabel.typeface, Typeface.BOLD)
        statusLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        labelsLL.addView(statusLabel)

        genderLabel = TextView(context)
        genderLabel.text = ""
        genderLabel.setTextColor(resources.getColor(R.color.color_primary))
        genderLabel.gravity = Gravity.CENTER
        genderLabel.setTypeface(statusLabel.typeface, Typeface.BOLD)
        genderLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        labelsLL.addView(genderLabel)

        //initialize a path, a paint and a RectF which are needed during the drawing phase
        path = Path()
        paint = Paint()
        radiusPathRectF = RectF()

        //center the mainBg ImageView
        val mainBgParams = mainBg.layoutParams as LayoutParams
        mainBgParams.addRule(CENTER_IN_PARENT, TRUE)
        mainBg.layoutParams = mainBgParams

        //center the needle RelativeLayout
        val needleParams = needle.layoutParams as LayoutParams
        needleParams.addRule(CENTER_IN_PARENT, TRUE)
        needle.layoutParams = needleParams

        //center the labels LinearLayout
        val labelsLLParams = labelsLL.layoutParams as LayoutParams
        labelsLLParams.addRule(CENTER_IN_PARENT, TRUE)
        labelsLL.layoutParams = labelsLLParams

        //set valueLabel margins
        val valueParams = valueLabel.layoutParams as LinearLayout.LayoutParams
        valueParams.setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt())
        valueLabel.layoutParams = valueParams

        //set statusLabel margins
        val statusParams = statusLabel.layoutParams as LinearLayout.LayoutParams
        statusParams.setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt())
        statusLabel.layoutParams = statusParams

        //set WillNotDraw to false to allow onDraw(Canvas canvas) to be called (This is needed when you have ViewGroups as subviews)
        setWillNotDraw(false)

        //set the value initially to 0
        setValue(0f,"")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val w = r - l
        val h = b - t

        //set the mainBg ImageView width and height
        val mainBgParams = mainBg.layoutParams as LayoutParams
        mainBgParams.width = (w / 1.2).toInt()
        mainBgParams.height = (w / 1.2).toInt()
        mainBg.layoutParams = mainBgParams

        //set the needle width
        val needleW = mainBgParams.height / 11

        //set the needle RelativeLayout width and height
        val needleParams = needle.layoutParams as LayoutParams
        needleParams.width = needleW
        needleParams.height = 2 * mainBgParams.height / 3
        needle.layoutParams = needleParams

        //set the polygon ImageView width and height to the same width of needle. Also add some top margin eg: 2 dps.
        val polygonParams = polygon.layoutParams as LayoutParams
        polygonParams.width = needleW
        polygonParams.height = needleW
        polygonParams.setMargins(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt(), 0, 0)
        polygon.layoutParams = polygonParams
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.w = w
        this.h = h
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1: Save the current drawing configuration
        canvas.save()

        // 2: Move to the center of our drawing rectangle and rotate so that we're pointing at the start of the first segment
        canvas.translate(w.toFloat() / 2, h.toFloat() / 2)
        canvas.rotate((rotationAngle - Math.PI / 2).toFloat())

        // 3: Set up the user's line width
        paint.setStrokeWidth(segmentWidth)

        // 4: Calculate the size of each segment in the total gauge in degrees
        val segmentAngle = totalAngle / segmentColors.size.toFloat()

        // 5: Calculate how wide the segment arcs should be
        val segmentRadius = (w - segmentWidth) / 2 - outerBezelWidth - innerBezelWidth

        // 6: Draw each segment
        for (index in segmentColors.indices) {
            val segment = segmentColors[index]

            // figure out where the segment starts in our arc in degrees
            val start = index.toFloat() * segmentAngle

            //activate its color
            paint.setColor(segment)

            // add a path for the segment
            radiusPathRectF.left = -segmentRadius/2
            radiusPathRectF.top = -segmentRadius/2
            radiusPathRectF.right = segmentRadius/2
            radiusPathRectF.bottom = segmentRadius/2
            path.addArc(radiusPathRectF, -90F, start + segmentAngle)

            // and stroke it using the activated color
            paint.setStyle(Paint.Style.STROKE)
            canvas.drawPath(path, paint)
        }

        // 7: Reset the graphics state
        canvas.restore()
    }
    private var prevValue = -1f;
    /**
     * Call this helper method to set a new value
     * @param value must be a number between 0-100
     */
    @SuppressLint("SetTextI18n")
    fun setValue(value: Float,gender:String) {

        if(prevValue == value)
            return

        // update the value label to show the exact number
        valueLabel.text = (((value.toFloat()*100.0).roundToInt())/100).toString()
        needle.animate()
            .rotationBy(rotationAngle)
            .setDuration(500)
            .setInterpolator(LinearInterpolator())
            .start()

        // update the status label based on the value eg: VERY GOOD or GOOD
//        statusLabel.text = if (value in 1..18) "VERY GOOD" else (if(value in 19..24) "GOOD" else
        genderLabel.text = gender
        if (value in 0.0..19.5){
            statusLabel.text="Underweight"
        }else if (value>18.5 && value<=24){
            statusLabel.text="NormalWeight"
        }else if (value>24 && value <=30){
            statusLabel.text="Overweight"
        }else if(value>30){
            statusLabel.text="Obese"
        }

        // figure out where the needle is, between 0 and 1 (This will set the min value to 0 and max value to 100)
        // in case you want to have a range between 0-1000 divide below with 1000
        val needlePosition = value.toFloat() / 40

        // create a lerp from the start angle (rotationAngle) through to the end angle (rotationAngle + totalAngle)
        val lerpFrom = rotationAngle
        val lerpTo = rotationAngle + totalAngle

        // lerp from the start to the end position, based on the needle's position
        val needleRotation = lerpFrom + (lerpTo - lerpFrom) * needlePosition

        //calculate the rotationBy angle (rotation delta angle)
        var rot = 0f
        val diff = Math.abs(Math.abs(needle.rotation) - Math.abs(needleRotation))
        if(needle.rotation == 0f && needleRotation == rotationAngle)
        {
            rot = rotationAngle
        }
        else if(needle.rotation == rotationAngle && needleRotation == 135f){
            rot = 135f*2;
        }
        else if(needleRotation < 0)
        {
            if(needleRotation < needle.rotation){
                if(needle.rotation > 0) {
                    if (needle.rotation == 135f){
                        rot = -(135f*2 - diff)
                    }
                    else if(needleRotation == rotationAngle){
                        rot = -(135f + Math.abs(needle.rotation))
                    }
                    else {
                        rot = -(Math.abs(needle.rotation) + Math.abs(needleRotation))
                    }
                }
                else {
                    rot = -diff
                }
            }
            else if(needleRotation > needle.rotation){
                rot = +diff
            }
            else{
                rot = rotationAngle
            }
        }
        else if(needleRotation > 0)
        {
            if(needleRotation < needle.rotation){
                rot = -diff
            }
            else if(needleRotation > needle.rotation){
                if(needle.rotation < 0) {
                    if (needle.rotation == rotationAngle){
                        rot = 135f + Math.abs(needleRotation)
                    }
                    else{
                        rot = Math.abs(needle.rotation) + Math.abs(needleRotation)
                    }
                }
                else {
                    rot = +diff
                }
            }
            else{
                rot = rotationAngle
            }
        }
        else if (needleRotation == 0f)
        {
            if(needle.rotation == 135f)
                rot = -diff
            else
                rot = +diff
        }

        //and animate the needle using the rotationBy()
        needle.animate()
            .rotationBy(rot) //if this value is negative it goes anticlockwise and if its positive is goes clockwise
            .setDuration(500)
            .setInterpolator(LinearInterpolator())
            .start()

        prevValue = value
    }
}