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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
    val db = FirebaseFirestore.getInstance()

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

        ///val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Initialize the adapter with an empty list
        adapter = RecipeAdapter(emptyList())
        recyclerView.adapter = adapter

        // Fetch and display 3 random recipes
        lifecycleScope.launch {
            val recipes = withContext(Dispatchers.IO) {
                Spoonacular.getRandRecipes(1)
            }
            adapter.setRecipes(recipes)
        }

        val searchButton: Button = view.findViewById(R.id.searchButton)
        val searchEditText: EditText = view.findViewById(R.id.searchEditText)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                // Handle empty query if needed
                // For example, show a toast or provide user feedback
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


    class RecipeAdapter(private var recipes: List<Spoonacular>) :
        RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.recipe_item, parent, false)
            return RecipeViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val recipe = recipes[position]
            holder.bind(recipe)
            Log.d("BIND_METHOD", "Data bound for recipe: ${recipe.title}")
        }

        override fun getItemCount(): Int {
            return recipes.size
        }

        fun setRecipes(recipes: List<Spoonacular>) {
            this.recipes = recipes
            notifyDataSetChanged()
            Log.d("ADAPTER_UPDATE", "Adapter updated with ${recipes.size} recipes")
        }

        class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val descriptionWebView: WebView = itemView.findViewById(R.id.descriptionWebView)
            private val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)
            private val readyInMinutesTextView: TextView = itemView.findViewById(R.id.readyInMinutesTextView)
            private val servingsTextView: TextView = itemView.findViewById(R.id.servingsTextView)
            private val fab: FloatingActionButton = itemView.findViewById(R.id.fab)
            private var recipeId = 1

            init {
                // Enable JavaScript in the WebView
                descriptionWebView.settings.javaScriptEnabled = true

                // Add click listener to the FAB
                fab.setOnClickListener {
                    /* Add food data to the Food collection
                    db.collection("Users").document(email).collection("Food").document("Put date here")
                        .set("Put hashMap data here")
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    */

                    // Create a JsonObject and output it to the console
                    // val recipeJsonObject = createRecipeJsonObject()
                    // Log.d("FAB_CLICK", "Recipe JSON: $recipeJsonObject")
                }
            }



            fun bind(recipe: Spoonacular) {
                titleTextView.text = recipe.title
                // Load description into WebView
                descriptionWebView.loadData(recipe.description, "text/html", "UTF-8")

                // Load image into ImageView using Glide
                Glide.with(itemView)
                    .load(recipe.image)
                    .into(recipeImageView)

                // Set Ready In Minutes and Servings
                readyInMinutesTextView.text = "Ready In Minutes: ${recipe.readyInMinutes}"
                servingsTextView.text = "Servings: ${recipe.servings}"
                recipeId = recipe.id
                println(recipe.nutrition)
                Log.d("BIND_METHOD", "Data bound for recipe: ${recipe.title}")
            }

            private fun createRecipeJsonObject(): JSONObject {
                val recipeJsonObject = JSONObject()
                recipeJsonObject.put("id", recipeId)
                recipeJsonObject.put("title", titleTextView.text.toString())
                recipeJsonObject.put("description", descriptionWebView.url) // You may need to adjust this based on the actual data in the WebView
                recipeJsonObject.put("image", recipeImageView.contentDescription) // You may need to adjust this based on the actual data in the ImageView
                recipeJsonObject.put("readyInMinutes", readyInMinutesTextView.text.toString())
                recipeJsonObject.put("servings", servingsTextView.text.toString())
                return recipeJsonObject
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Perform the API call and update the UI with the results
            val recipes = searchRecipes(query)
            Log.d("API_RESPONSE", recipes.toString())

            // Update the adapter on the main thread
            withContext(Dispatchers.Main) {
                adapter.setRecipes(recipes)
            }
        }
    }


}