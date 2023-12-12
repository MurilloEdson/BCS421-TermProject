package edu.farmingdale.bcs421_termproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.farmingdale.bcs421_termproject.databinding.FragmentAddFoodBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentProgressBinding
import java.text.SimpleDateFormat
import java.util.Calendar

private lateinit var binding: FragmentAddFoodBinding

class AddFoodFragment : Fragment(R.layout.fragment_add_food) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFoodBinding.inflate(layoutInflater, container, false)
        var view : View = binding.root
        val foodFragment = FoodFragment()
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, foodFragment)
                commit()
            }
        }

        val proteinET = view.findViewById<EditText>(R.id.proteinET)
        val caloriesET = view.findViewById<EditText>(R.id.caloriesET)
        val fatET = view.findViewById<EditText>(R.id.fatET)
        val carbsET = view.findViewById<EditText>(R.id.carbsET)
        val addButton = view.findViewById<Button>(R.id.addFoodButton)

        addButton.setOnClickListener {
            val calories = caloriesET.text.trim().toString().toDouble()
            val carbs = carbsET.text.trim().toString().toDouble()
            val protein = proteinET.text.trim().toString().toDouble()
            val fat = fatET.text.trim().toString().toDouble()
            val foodData = hashMapOf(
                "calories" to calories,
                "carbs" to carbs,
                "protein" to protein,
                "fat" to fat,
            )

            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM-dd-yyyy")
            val formattedDateString = dateFormat.format(cal.time)
            val db = FirebaseFirestore.getInstance()
            val firebaseAuth = FirebaseAuth.getInstance()
            val userDocument = db.collection("Users")
                .document(firebaseAuth.currentUser?.email.toString())
            val mealsCollection = userDocument.collection("Food")
            val dateDocument = mealsCollection.document(formattedDateString) // Assuming formattedDateString is the current date

            // Add food data to the "Food" collection for the current date
            dateDocument.collection("Meals") // New collection for each date
                .add(foodData)
                .addOnSuccessListener { mealDocumentReference ->
                    Log.d("FAB_CLICK", "Food data successfully added to Firestore!")

                    // Update progress data in the progress document directly
                    val progressDocument = userDocument.collection("Progress").document(formattedDateString)
                    progressDocument.update("calories-today", FieldValue.increment(calories))
                    progressDocument.update("carbs-today", FieldValue.increment(carbs))
                    progressDocument.update("protein-today", FieldValue.increment(protein))
                    progressDocument.update("fat-today", FieldValue.increment(fat))
                }
                .addOnFailureListener { e ->
                    Log.w("FAB_CLICK", "Error adding food data to Firestore", e)
                }
        }


        return view
    }

}