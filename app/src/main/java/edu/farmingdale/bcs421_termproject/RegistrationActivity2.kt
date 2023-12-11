package edu.farmingdale.bcs421_termproject

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.ArrayList
import java.util.Calendar

/**
 * The second registration activity to handle height/weight/age user input on intitial registration.
 */
class RegistrationActivity2 : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    // The multipliers to be used in the Mifflin-St Jeor formula calculations.
    val sedentaryMultiplier = 1.2
    val slightlyActiveMultiplier = 1.375
    val lightlyActiveMultiplier = 1.425
    val moderatelyActiveMultiplier = 1.55
    val veryActiveMultiplier = 1.75
    val proteinRatio = 0.25
    val carbsRatio = 0.50
    val fatRatio = 0.25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity2)
        var email = intent.getStringExtra("email") // Get email passed through the intent
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        // Programatically adjust status bar color since we use multiple colors throughout the app
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.bb_blue)
        }

        val completeButton = findViewById<Button>(R.id.completeButton)
        val heightEditText = findViewById<EditText>(R.id.heightET)
        val weightEditText = findViewById<EditText>(R.id.weightET)
        val dobEditText = findViewById<EditText>(R.id.dobET)

        // Populate both spinners with the different options for sex and activity level.
        val sexDropDownMenu: Spinner = findViewById(R.id.sexSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.sex_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sexDropDownMenu.adapter = adapter
        }
        val activityDropDownMenu: Spinner = findViewById(R.id.activitySpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.activity_level_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            activityDropDownMenu.adapter = adapter
        }

        completeButton.setOnClickListener {
            val height = heightEditText.text.toString().trim()
            val weight = weightEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val sex = sexDropDownMenu.selectedItem.toString()
            val activityLevel = activityDropDownMenu.selectedItem.toString()

            if (height.isNotEmpty() && weight.isNotEmpty() && dob.isNotEmpty() && sex.isNotEmpty() && activityLevel.isNotEmpty()) {
                // Check regex for date of birth first
                if (checkDOB(dob)) {
                    // Get the last 4 digits of DOB and do the math for the user's current age
                    val lastDigits = dob.takeLast(4).toInt()
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val age = currentYear - lastDigits

                    // Calculate calories and macros
                    val listOfCaloriesAndMacros = calculateCaloriesMacros(height.toInt(), weight.toFloat(), age, sex, activityLevel)
                    // Set up the hash map data to be added to Firestore
                    val mainData = hashMapOf(
                        "height" to height,
                        "weight" to weight,
                        "date-of-birth" to dob,
                        "age" to age,
                        "sex" to sex,
                        "calorie-goal" to listOfCaloriesAndMacros[0],
                        "protein-goal" to listOfCaloriesAndMacros[1],
                        "carbs-goal" to listOfCaloriesAndMacros[2],
                        "fat-goal" to listOfCaloriesAndMacros[3],
                        "steps-goal" to 5000,
                        "signed-in-before" to true
                    )

                    // Update the user's document with their maintenance calorie and macro goals
                    if (email != null) {
                        db.collection("Users").document(email)
                            .set(mainData)
                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    }
                    // Go to Dashboard activity
                    startActivity(Intent(this, DashboardActivity::class.java))
                } else {
                    Toast.makeText(this, "Fill all fields in the correct format.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * Checks date of birth format. Must be MM/DD/YYYY. Returns true if the user inputted a valid format.
     */
    private fun checkDOB(dob: String): Boolean {
        val dobPattern = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d{2}$"
        return dob.matches(dobPattern.toRegex())
    }

    /**
     * Calculates the maintenance calories and macros for a user. Uses the Mifflin-St Jeor formula.
     */
    private fun calculateCaloriesMacros(height: Int, weight: Float, age: Int, sex: String, activityLevel: String) : List<Int> {
        // First, get the multiplier based on activity level
        val activityLevelArray = resources.getStringArray(R.array.activity_level_options)
        var multiplier = 0.0
        if (activityLevel == activityLevelArray[0]) {
            multiplier = sedentaryMultiplier
        }
        if (activityLevel == activityLevelArray[1]) {
            multiplier = slightlyActiveMultiplier
        }
        if (activityLevel == activityLevelArray[2]) {
            multiplier = lightlyActiveMultiplier
        }
        if (activityLevel == activityLevelArray[3]) {
            multiplier = moderatelyActiveMultiplier
        }
        if (activityLevel == activityLevelArray[4]) {
            multiplier = veryActiveMultiplier
        }
        // Convert weight and height to be compatible with the Mifflin-St Jeor formula.
        val heightAdjusted = height * 2.54 // cm
        val weightAdjusted = weight / 2.205 // kg

        var calories = 0
        // Use the Mifflin-St Jeor formula to calculate calories
        if (sex == "Male") {
            calories = (((10 * weightAdjusted) + (6.25 * heightAdjusted) - (5 * age) + 5) * multiplier).toInt()
        } else {
            calories = (((10 * weightAdjusted) + (6.25 * heightAdjusted) - (5 * age) - 161) * multiplier).toInt()
        }
        // Divide the calories based on a widely accepted protein/carb/fat ratio to weight maintenance
        val protein = ((calories * proteinRatio) / 4).toInt()
        val carbs = ((calories * carbsRatio) / 4).toInt()
        val fat = ((calories * fatRatio) / 9).toInt()
        return listOf(calories, protein, carbs, fat) // Return a list of all 4 numbers to put into Firestore later
    }
}
