package edu.farmingdale.bcs421_termproject

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        // Programatically adjust status bar color since we use multiple colors throughout the app
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.bb_blue)
        }

        val loginButton = findViewById<Button>(R.id.logInButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        // Move to registration activity
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser

                            // Setting up the new user's data fields in their unique document
                            val mainData = hashMapOf(
                                "email" to email,
                                "height" to 0,
                                "weight" to 0,
                                "age" to 0,
                                "date-of-birth" to "none",
                                "sex" to "none",
                                "calorie-goal" to 0,
                                "protein-goal" to 0,
                                "carbs-goal" to 0,
                                "fat-goal" to 0,
                                "steps-goal" to 0,
                                "signed-in-before" to false
                            )

                            // Setting up the new user's food data. Empty at first as they have not added any food.
                            val foodData = hashMapOf(
                                "first-food-entry" to true // This is just to get some data in the hash map so it can work with Firebase's methods.
                            )

                            // Setting up the user's daily goal data. Starts at 0 as they have not entered calories/macros for the day.
                            val progressData = hashMapOf(
                                "calories-today" to 0,
                                "protein-today" to 0,
                                "carbs-today" to 0,
                                "fat-today" to 0,
                                "steps-today" to 0,
                                "calories-burned-today" to 0,
                                "exercise-time-today" to 0
                            )

                            // Used for getting today's date in a specific format as the name of the documents in the Food and Goals collections.
                            val cal = Calendar.getInstance()
                            val dateFormat = SimpleDateFormat("MM-dd-yyyy")
                            val todaysDateFormatted = dateFormat.format(cal.time)

                            // Create a new user document with their unique email address as the name of the document
                            db.collection("Users").document(email)
                                .set(mainData)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                            // Create the food collection to be populated later
                            db.collection("Users").document(email).collection("Food").document(todaysDateFormatted)
                                .set(foodData)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                            // Create the goals collection to be populated later
                            db.collection("Users").document(email).collection("Progress").document(todaysDateFormatted)
                                .set(progressData)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                            //  Go to second registration activity. Pass through the email to use for firebase methods in next activity.
                            val intent = Intent(this, RegistrationActivity2::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Emails must be unique and passwords must be at least 6 characters long.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show()
            }


        }

    }
}