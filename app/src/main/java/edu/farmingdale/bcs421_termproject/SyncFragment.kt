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
import edu.farmingdale.bcs421_termproject.databinding.FragmentFriendsBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentGoalsBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentNutritionBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentPersonalInformationBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentProgressBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentRecipesBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentSyncBinding

private lateinit var binding: FragmentSyncBinding
class SyncFragment : Fragment(R.layout.fragment_sync) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSyncBinding.inflate(layoutInflater, container, false)
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