package edu.farmingdale.bcs421_termproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import edu.farmingdale.bcs421_termproject.databinding.FragmentAccountBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentGoalsBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentNutritionBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentPersonalInformationBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentProgressBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentRecipesBinding

private lateinit var binding: FragmentRecipesBinding
class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipesBinding.inflate(layoutInflater, container, false)
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

}