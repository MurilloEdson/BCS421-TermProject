package edu.farmingdale.bcs421_termproject

import android.adservices.common.AdData
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.farmingdale.bcs421_termproject.databinding.FragmentHomeBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import com.google.android.gms.ads.MobileAds

private lateinit var binding: FragmentHomeBinding

/**
 * Home fragment to display a user's calorie and macro summary.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val cal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MM-dd-yyyy")
    val todaysDateFormatted = dateFormat.format(cal.time) // Today's date formatted for the Firebase methods
    lateinit var caloriesProgressCircle: ProgressBar
    lateinit var proteinProgressCircle: ProgressBar
    lateinit var carbsProgressCircle: ProgressBar
    lateinit var fatProgressCircle: ProgressBar
    lateinit var stepsProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        var view :View = binding.root
        // Load an ad
        MobileAds.initialize(requireActivity()) {}
        val adView : AdView = view.findViewById(R.id.adView)
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        // Calories card
        caloriesProgressCircle = view.findViewById<ProgressBar>(R.id.caloriesProgress)
        val caloriesText = view.findViewById<TextView>(R.id.caloriesProgressTV)
        val goalCalories = view.findViewById<TextView>(R.id.calorieGoalAmountTV)
        val foodCalories = view.findViewById<TextView>(R.id.foodAmountTV)
        val exerciseCalories = view.findViewById<TextView>(R.id.exerciseAmountTV)
        // Macros card
        proteinProgressCircle = view.findViewById<ProgressBar>(R.id.proteinProgress)
        val proteinText = view.findViewById<TextView>(R.id.proteinProgressTV)
        carbsProgressCircle = view.findViewById<ProgressBar>(R.id.carbsProgress)
        val carbsText = view.findViewById<TextView>(R.id.carbsProgressTV)
        fatProgressCircle = view.findViewById<ProgressBar>(R.id.fatProgress)
        val fatText = view.findViewById<TextView>(R.id.fatProgressTV)
        // Steps card
        val stepsAmount = view.findViewById<TextView>(R.id.stepsAmountTV)
        stepsProgressBar = view.findViewById<ProgressBar>(R.id.stepsProgress)
        // Exercise card
        val exerciseCal = view.findViewById<TextView>(R.id.caloriesBurnedTV)
        val exerciseTime = view.findViewById<TextView>(R.id.exerciseTimeTV)



        lifecycleScope.launch {
            // Retrieve all necessary values for displaying a user's summary
            val calorieGoal = retrieveCalorieGoal()
            val proteinGoal = retrieveProteinGoal()
            val carbsGoal = retrieveCarbsGoal()
            val fatGoal = retrieveFatGoal()
            val stepsGoal = retrieveStepGoal()
            val caloriesConsumed = retrieveCaloriesConsumed()
            val caloriesBurned = retrieveCaloriesBurned()
            val proteinConsumed = retrieveProteinConsumed()
            val carbsConsumed = retrieveCarbsConsumed()
            val fatConsumed = retrieveFatConsumed()
            val stepsToday = retrieveSteps()
            val exerciseTimeToday = retrieveExerciseTime()

            // Calculate calories, macros, and steps remaining for the day
            val caloriesRemaining = calorieGoal - caloriesConsumed + caloriesBurned
            Log.d("$caloriesRemaining", "Calories Remaining")
            val proteinRemaining = proteinGoal - proteinConsumed
            val carbsRemaining = carbsGoal - carbsConsumed
            val fatRemaining = fatGoal - fatConsumed

            // Create percentages for calories, macros, and steps to fill up the progress bars accordingly
            val percentOfCaloriesComplete = caloriesConsumed.toFloat() / calorieGoal.toFloat() * 100f
            val percentOfProteinComplete = proteinConsumed.toFloat() / proteinGoal.toFloat() * 100f
            val percentOfCarbsComplete = carbsConsumed.toFloat() / carbsGoal.toFloat() * 100f
            val percentOfFatComplete = fatConsumed.toFloat() / fatGoal.toFloat() * 100f
            val percentOfStepsComplete = stepsToday.toFloat() / stepsGoal.toFloat() * 100f

            // Update the text in the UI
            caloriesText.text = caloriesRemaining.toString() + "\nremaining"
            proteinText.text = proteinRemaining.toString() + "g\nremaining"
            carbsText.text = carbsRemaining.toString() + "g\nremaining"
            fatText.text = fatRemaining.toString() + "g\nremaining"
            stepsAmount.text = stepsToday.toString() + "/" + stepsGoal.toString() + " steps"
            goalCalories.text = calorieGoal.toString()
            foodCalories.text = caloriesConsumed.toString()
            exerciseCalories.text = caloriesBurned.toString()
            exerciseCal.text = caloriesBurned.toString() + " cal"
            exerciseTime.text = exerciseTimeToday.toString()

            // Use a map to map progress bars to their respective percentages.
            val progressMap = mapOf(
                caloriesProgressCircle to percentOfCaloriesComplete,
                proteinProgressCircle to percentOfProteinComplete,
                carbsProgressCircle to percentOfCarbsComplete,
                fatProgressCircle to percentOfFatComplete,
                stepsProgressBar to percentOfStepsComplete
            )
            var progressAnimators: List<ObjectAnimator>? = null

            // Animate the progress bars
            lifecycleScope.launch {
                progressAnimators?.forEach { it.cancel() }

                for ((progressCircle, percentage) in progressMap) {
                    val progressAnimator = ObjectAnimator.ofInt(
                        progressCircle,
                        "progress",
                        0,
                        percentage.toInt()
                    ).apply {
                        duration = 750 // Set animation duration
                        interpolator = FastOutSlowInInterpolator()
                        start() // Start the animation
                    }
                    progressAnimators?.plus(progressAnimator)
                }
            }
        }
        return view
    }

    /**
     * Returns the user's set calorie goal.
     */
    private suspend fun retrieveCalorieGoal(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("calorie-goal")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's set protein goal.
     */
    private suspend fun retrieveProteinGoal(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("protein-goal")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's set carbohydrates goal.
     */
    private suspend fun retrieveCarbsGoal(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("carbs-goal")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's set fat goal.
     */
    private suspend fun retrieveFatGoal(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("fat-goal")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's set steps goal.
     */
    private suspend fun retrieveStepGoal(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("steps-goal")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's calories consumed so far for the day.
     */
    private suspend fun retrieveCaloriesConsumed(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("calories-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's calories burned so far for the day.
     */
    private suspend fun retrieveCaloriesBurned(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("calories-burned-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's protein consumed so far for the day.
     */
    private suspend fun retrieveProteinConsumed(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("protein-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's carbohydrates consumed so far for the day.
     */
    private suspend fun retrieveCarbsConsumed(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("carbs-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's fat consumed so far for the day.
     */
    private suspend fun retrieveFatConsumed(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("fat-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's steps so far for the day.
     */
    private suspend fun retrieveSteps(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("steps-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Returns the user's time spent exercising so far for the day.
     */
    private suspend fun retrieveExerciseTime(): Int {
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString()).collection("Progress").document(todaysDateFormatted)
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getLong("exercise-time-today")?.toInt() ?: 0
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Resets the progress circles to 0 when the fragment comes back into view.
     */
    override fun onResume() {
        super.onResume()
        caloriesProgressCircle.progress = 0
        proteinProgressCircle.progress = 0
        carbsProgressCircle.progress = 0
        fatProgressCircle.progress = 0
        stepsProgressBar.progress = 0
    }

}