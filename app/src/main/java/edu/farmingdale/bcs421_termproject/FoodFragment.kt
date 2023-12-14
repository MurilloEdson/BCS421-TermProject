package edu.farmingdale.bcs421_termproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.farmingdale.bcs421_termproject.Spoonacular.Companion.searchRecipes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FoodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FoodFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_food, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the adapter with an empty list
        adapter = RecipeAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // Fetch and display random recipes of size n
        lifecycleScope.launch {
            val recipes = withContext(Dispatchers.IO) {
                Spoonacular.getRandRecipes(3)
            }
            adapter.setRecipes(recipes)
        }

        val searchButton: Button = view.findViewById(R.id.searchButton)
        val searchEditText: EditText = view.findViewById(R.id.searchEditText)
        val addButton: Button = view.findViewById(R.id.addBtn)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                // Handle empty query if needed
            }
        }

        addButton.setOnClickListener {
            val addFoodFragment = AddFoodFragment()
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, addFoodFragment)
                commit()
            }
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FoodFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FoodFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    class RecipeAdapter(private var recipes: List<Spoonacular>,
                        private val clickListener: FoodFragment
    ) :
        RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

        interface OnRecipeClickListener {
            fun onRecipeClick()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.recipe_item, parent, false)
            return RecipeViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val recipe = recipes[position]
            holder.bind(recipe)
        }

        override fun getItemCount(): Int {
            return recipes.size
        }

        fun setRecipes(recipes: List<Spoonacular>) {
            this.recipes = recipes
            notifyDataSetChanged()
            Log.d("ADAPTER_UPDATE", "Adapter updated with ${recipes.size} recipes")
        }

        inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val descriptionWebView: WebView = itemView.findViewById(R.id.descriptionWebView)
            private val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
            private val readyInMinutesTextView: TextView = itemView.findViewById(R.id.readyInMinutesTextView)
            private val servingsTextView: TextView = itemView.findViewById(R.id.servingsTextView)
            private val fab: FloatingActionButton = itemView.findViewById(R.id.fab)
            private var recipeId = 1
            private var calories = 0.0
            private var fat = 0.0
            private var carbs = 0.0
            private var protein = 0.0
            private var recipeImage = ""

            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM-dd-yyyy")
            val formattedDateString = dateFormat.format(cal.time)
            init {
                // Enable JavaScript in the WebView
                descriptionWebView.settings.javaScriptEnabled = true

                // Add click listener to the FAB
                fab.setOnClickListener {
                    // Add food data to the "Food" collection for the current date
                    val foodData = hashMapOf(
                        "id" to recipeId,
                        "title" to titleTextView.text.toString(),
                        "calories" to calories,
                        "carbs" to carbs,
                        "protein" to protein,
                        "fat" to fat,
                        "imageUrl" to recipeImage
                    )

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
                            clickListener.onRecipeClick()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FAB_CLICK", "Error adding food data to Firestore", e)
                        }
                }
            }

            fun bind(recipe: Spoonacular) {
                titleTextView.text = recipe.title
                // Load description into WebView
                descriptionWebView.loadData(recipe.description, "text/html", "UTF-8")

                // Load image into ImageView using Glide
                recipeImage = recipe.image
                Glide.with(itemView)
                    .load(recipe.image)
                    .into(recipeImageView)

                readyInMinutesTextView.text = "Calories: ${recipe.nutrition.calories}"
                servingsTextView.text = "Servings: ${recipe.servings}"
                recipeId = recipe.id
                calories = recipe.nutrition.calories
                fat = recipe.nutrition.fat
                carbs = recipe.nutrition.carbohydrates
                protein = recipe.nutrition.protein

                println(recipe.nutrition)
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Perform the API call and update the UI with the results
            val recipes = searchRecipes(query)

            // Update the adapter on the main thread
            withContext(Dispatchers.Main) {
                adapter.setRecipes(recipes)
            }
        }
    }

    fun onRecipeClick() {
        // Handle the click action here (show toast, perform action, etc.)
        Toast.makeText(requireContext(), "Food added to Nutrition log.", Toast.LENGTH_SHORT).show()
    }


}