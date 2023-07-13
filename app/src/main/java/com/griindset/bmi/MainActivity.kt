package com.griindset.bmi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.textfield.TextInputEditText

import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var dobTextInput: TextInputEditText
    private  lateinit var editHeight: EditText
    private lateinit var editWeight:EditText
    private lateinit var boxMale :LinearLayout
    private lateinit var boxFemale :LinearLayout
    private lateinit var unitSpinner: Spinner
    private lateinit var gaugeView: GaugeView
    private lateinit var bmiBtn : Button
    private var gender :String =""
    private var height : Float = 0f
    private var weight : Float = 0f
    private lateinit var unit:String
    private lateinit var rootView: LinearLayoutCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView=findViewById(R.id.root_view)
        rootView.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it?.windowToken, 0)
        }
        editHeight = findViewById(R.id.edit_height)
        editWeight = findViewById(R.id.edit_weight)
        boxMale = findViewById(R.id.male_avatar)
        boxFemale =findViewById(R.id.female_avatar)


        boxMale.setOnClickListener {
            boxMale.setBackgroundResource(R.drawable.male_female_focus)
            boxFemale.setBackgroundResource(R.drawable.male_female_box)
            gender = "Male"
        }
        boxFemale.setOnClickListener {
            boxFemale.setBackgroundResource(R.drawable.male_female_focus)
            boxMale.setBackgroundResource(R.drawable.male_female_box)
            gender = "Female"
        }

        val unitOptions = arrayOf("In", "Cm")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitOptions)
        adapter.setDropDownViewResource(R.layout.spinner_text)
        unitSpinner = findViewById(R.id.unitSpinner)
        unitSpinner.adapter = adapter

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                unit = adapterView.getItemAtPosition(position) as String
                val selectedView = unitSpinner.selectedView as TextView
                selectedView.setTextColor(resources.getColor(R.color.black))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
                unit = adapterView.getItemAtPosition(0) as String
                val selectedView = unitSpinner.selectedView as TextView
                selectedView.setTextColor(resources.getColor(R.color.black))
            }
        }
        var age:Int = 0
        dobTextInput = findViewById(R.id.dob_text_input)
        dobTextInput.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Select D.O.B")
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener { selectedDate ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(selectedDate))

                // Set the selected date to the TextInputEditText
                dobTextInput.setText(formattedDate)

                // Calculate the age
                val currentDate = Calendar.getInstance()
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.timeInMillis = selectedDate

                age = currentDate.get(Calendar.YEAR) - selectedCalendar.get(Calendar.YEAR)

                // Additional check to account for the birth month and day
                if (currentDate.get(Calendar.MONTH) < selectedCalendar.get(Calendar.MONTH) ||
                    (currentDate.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                            currentDate.get(Calendar.DAY_OF_MONTH) < selectedCalendar.get(Calendar.DAY_OF_MONTH))) {
                    age--
                }

                // Use the 'age' variable for further processing
                Log.d("Age", age.toString())
            }
            picker.show(supportFragmentManager, picker.toString())
        }

        gaugeView =findViewById(R.id.gauge_view)
        bmiBtn = findViewById(R.id.bmi_btn)
        var isBmi:Boolean = true
        bmiBtn.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it?.windowToken, 0)
            if (isBmi){
                try {
                    height = editHeight.text.toString().toFloat()
                    weight = editWeight.text.toString().toFloat()
                    if(unit=="In"){
                        height= ((2.54*height)/100).toFloat()
                    }else{
                        height /= 100
                    }

                    if (height==0f || height>300f || height<0f ){
                        Toast.makeText(this,"Invalid Height",Toast.LENGTH_SHORT).show()
                    }else if(weight==0f || weight<0f || weight>500f){
                        Toast.makeText(this,"Invalid Weight",Toast.LENGTH_SHORT).show()
                    }else if(gender==""){
                        Toast.makeText(this,"Please Select A gender",Toast.LENGTH_SHORT).show()
                    }else if(age<3){
                        Toast.makeText(this,"BMI Not Applicable For This Age",Toast.LENGTH_SHORT).show()
                    }else{

                        var bmi:Float = weight/(height*height)
                        isBmi=false
                        bmiBtn.text ="RESET"
                        if(bmi>40){
                            Toast.makeText(this,"Wrong measurement inputs",Toast.LENGTH_LONG)
                        }else {
                            gaugeView.setValue(bmi, gender)
                        }
                    }

                }catch (e:Exception){
                    Toast.makeText(this,"Invalid Inputs",Toast.LENGTH_SHORT).show()
                }
            }else{
                editWeight.setText("");
                editHeight.setText("");
                dobTextInput.setText("")
                dobTextInput.hint="DD/MM/YYYY"
                boxFemale.setBackgroundResource(R.drawable.male_female_box)
                boxMale.setBackgroundResource(R.drawable.male_female_box)
                bmiBtn.text="GET BMI"
                isBmi = true
                gaugeView.setValue(0f,"")
            }
        }

    }
}