package edu.farmingdale.bcs421_termproject

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.*
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    companion object {
        private const val RC_SIGN_IN = 123
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        firebaseAuth = FirebaseAuth.getInstance()

        // Programatically adjust status bar color since we use multiple colors throughout the app
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.bb_blue)
        }

        val loginButton = findViewById<Button>(R.id.logInButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val googleButton = findViewById<Button>(R.id.googleButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val pass = passwordEditText.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Invalid username and/or password.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show()
            }
        }

        // Move to registration activity
        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
        googleButton.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
            val client = GoogleSignIn.getClient(this,gso)
            client.revokeAccess()
            val sIntent = client.signInIntent
            startActivityForResult(sIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val email = user?.email.toString()

                Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                //startActivity(Intent(this, DashboardActivity::class.java))
                //finish()
                db.collection("Users")
                    .document(email)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                // Document exists, user has signed in before
                                val intent = Intent(this, DashboardActivity::class.java)
                                startActivity(intent)
                            } else {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(ContentValues.TAG, "signInWithEmail:success")

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
                                    "steps-goal" to 0
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
                                    .addOnSuccessListener {
                                        Log.d(
                                            ContentValues.TAG,
                                            "DocumentSnapshot successfully written!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            ContentValues.TAG,
                                            "Error writing document",
                                            e
                                        )
                                    }

                                // Create the food collection to be populated later
                                db.collection("Users").document(email).collection("Food")
                                    .document(todaysDateFormatted)
                                    .set(foodData)
                                    .addOnSuccessListener {
                                        Log.d(
                                            ContentValues.TAG,
                                            "DocumentSnapshot successfully written!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            ContentValues.TAG,
                                            "Error writing document",
                                            e
                                        )
                                    }

                                // Create the goals collection to be populated later
                                db.collection("Users").document(email).collection("Progress")
                                    .document(todaysDateFormatted)
                                    .set(progressData)
                                    .addOnSuccessListener {
                                        Log.d(
                                            ContentValues.TAG,
                                            "DocumentSnapshot successfully written!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            ContentValues.TAG,
                                            "Error writing document",
                                            e
                                        )
                                    }

                                //  Go to second registration activity. Pass through the email to use for firebase methods in next activity.
                                val intent = Intent(this, RegistrationActivity2::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}