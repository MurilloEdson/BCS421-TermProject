package edu.farmingdale.bcs421_termproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.farmingdale.bcs421_termproject.databinding.FragmentNutritionBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NutritionFragment : Fragment(R.layout.fragment_nutrition) {
    private lateinit var binding: FragmentNutritionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealItemAdapter
    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNutritionBinding.inflate(layoutInflater, container, false)
        var view : View = binding.root
        val accountFragment = AccountFragment()
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, accountFragment)
                commit()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MealItemAdapter()
        recyclerView.adapter = adapter

        // Fetch and display data from Firestore
        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val formattedDateString = getCurrentFormattedDate()

        // Fetch data from Firestore
        db.collection("Users")
            .document(firebaseAuth.currentUser?.email.toString())
            .collection("Food")
            .document(formattedDateString)
            .collection("Meals")
            .get()
            .addOnSuccessListener { documents ->
                val mealList = mutableListOf<MealItem>()

                for (document in documents) {
                    val mealData = document.data
                    val mealItem = MealItem(
                        (mealData["id"] as Long).toInt(),
                        mealData["title"] as String,
                        mealData["calories"] as Double,
                        mealData["carbs"] as Double,
                        mealData["protein"] as Double,
                        mealData["fat"] as Double,
                        mealData["meal"] as String,
                        mealData["imageUrl"] as String
                    )
                    mealList.add(mealItem)
                }

                // Update the adapter with the fetched data
                adapter.setMealItems(mealList)
            }
            .addOnFailureListener { e ->
                Log.w("FETCH_DATA", "Error fetching data from Firestore", e)
            }
    }

    private fun getCurrentFormattedDate(): String {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
        return dateFormat.format(cal.time)
    }
}

class MealItemAdapter : RecyclerView.Adapter<MealItemAdapter.MealItemViewHolder>() {

    private var mealItems: List<MealItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.meal_item, parent, false)
        return MealItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        val mealItem = mealItems[position]
        holder.setAdapter(this)
        holder.bind(mealItem)
    }

    override fun getItemCount(): Int {
        return mealItems.size
    }

    fun setMealItems(mealItems: List<MealItem>) {
        this.mealItems = mealItems
        notifyDataSetChanged()
        Log.d("ADAPTER_UPDATE", "Adapter updated with ${mealItems.size} meal items")
    }

    class MealItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.caloriesTextView)
        private val carbsTextView: TextView = itemView.findViewById(R.id.carbsTextView)
        private val proteinTextView: TextView = itemView.findViewById(R.id.proteinTextView)
        private val fatTextView: TextView = itemView.findViewById(R.id.fatTextView)
        private val mealTextView: TextView = itemView.findViewById(R.id.mealTextView)
        private val mealImageView: ImageView = itemView.findViewById(R.id.mealImageView)
        private val mealDeleteButton: Button = itemView.findViewById(R.id.mealDeleteButton)
        private var mealItems: List<MealItem> = emptyList()
        private lateinit var adapter: MealItemAdapter

        fun setAdapter(adapter: MealItemAdapter) {
            this.adapter = adapter
        }
        fun bind(mealItem: MealItem) {
            titleTextView.text = mealItem.title
            caloriesTextView.text = "Calories: ${mealItem.calories}"
            carbsTextView.text = "Carbs: ${mealItem.carbs}"
            proteinTextView.text = "Protein: ${mealItem.protein}"
            fatTextView.text = "Fat: ${mealItem.fat}"
            mealTextView.text = "Meal: ${mealItem.meal}"

            // Load image into ImageView using Glide
            Glide.with(itemView)
                .load(mealItem.imageUrl)
                .into(mealImageView)

            // Set click listener for delete button
            mealDeleteButton.setOnClickListener {
                // Call a function to delete the meal from Firestore
                deleteMealFromFirestore(mealItem.id)
            }
        }

        private fun deleteMealFromFirestore(mealId: Int) {
            val db = FirebaseFirestore.getInstance()
            val firebaseAuth = FirebaseAuth.getInstance()

            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM-dd-yyyy")
            val formattedDateString = dateFormat.format(cal.time)

            // Delete the meal from Firestore
            db.collection("Users")
                .document(firebaseAuth.currentUser?.email.toString())
                .collection("Food")
                .document(formattedDateString)
                .collection("Meals")
                .whereEqualTo("id", mealId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                        Log.d("DELETE_MEAL", "Meal successfully deleted from Firestore!")

                        // Remove the deleted item from the local mealItems list
                        val updatedMealItems = mealItems.toMutableList()
                        updatedMealItems.removeAll { it.id == mealId }
                        mealItems = updatedMealItems
                        // Notify the adapter that the dataset has changed
                        adapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DELETE_MEAL", "Error deleting meal from Firestore", e)
                }
        }
    }
}

data class MealItem(
    val id: Int,
    val title: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val meal: String,
    val imageUrl: String // Add this if you have an image URL
)