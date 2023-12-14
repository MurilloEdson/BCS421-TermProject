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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.farmingdale.bcs421_termproject.databinding.FragmentAccountBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentPersonalInformationBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private lateinit var binding: FragmentPersonalInformationBinding

class PersonalInformationFragment : Fragment(R.layout.fragment_personal_information) {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalInformationBinding.inflate(layoutInflater, container, false)
        var view : View = binding.root
        val accountFragment = AccountFragment()
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, accountFragment)
                commit()
            }
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val dobEditText = view.findViewById<EditText>(R.id.dobET)
        lifecycleScope.launch {
            dobEditText.setText(retrieveDob())
        }

        // Drop down menu for selecting male/female
        val sexDropDownMenu: Spinner = view.findViewById(R.id.sexSpinner)
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.sex_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sexDropDownMenu.adapter = adapter
        }

        // Code to pull date of birth data from Firestore and update dobEditText
        // Code to pull sex data from Firestore and update sexDropDownMenu

        val editDob = view.findViewById<ImageView>(R.id.editDOB)
        val editSex = view.findViewById<ImageView>(R.id.editSex)
        // Click listeners for editDob and editSex. Initially, the fields should not be editable.
        // Then, once we press the button we can edit the DOB and sex. This is still a WIP.
        // Code to confirm changes and update Firestore.
        return view
    }
    private suspend fun retrieveDob(): String {
        val field = "date-of-birth"
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getString(field)?.toString() ?: field
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            field
        }
    }
}