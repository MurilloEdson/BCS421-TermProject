package edu.farmingdale.bcs421_termproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import edu.farmingdale.bcs421_termproject.HealthConnectManager
import edu.farmingdale.bcs421_termproject.databinding.FragmentBodyMeasurementsBinding
import edu.farmingdale.bcs421_termproject.databinding.FragmentPersonalInformationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class BodyMeasurementsFragment : Fragment(R.layout.fragment_body_measurements) {

    private lateinit var binding: FragmentBodyMeasurementsBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBodyMeasurementsBinding.inflate(layoutInflater, container, false)
        var view : View = binding.root
        val accountFragment = AccountFragment()
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, accountFragment)
                commit()
            }
        }
        
        // Code to pull the user's height and weight data from Firestore.

        val heightEditText = view.findViewById<EditText>(R.id.heightET)
        val weightEditText = view.findViewById<EditText>(R.id.weightET)
        val editHeight = view.findViewById<ImageView>(R.id.editHeight)
        val editWeight = view.findViewById<ImageView>(R.id.editWeight)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        lifecycleScope.launch {
            heightEditText.setText(retrieveHeight())
            weightEditText.setText(retrieveWeight())
        }

        val submitButton = view.findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener{
            val newWeight = weightEditText.text.toString()
            lifecycleScope.launch(Dispatchers.IO) {
                writeWeightInput(newWeight.toDouble())
            }
        }
        // Click listeners for the image views to make the edit texts editable (they shouldn't be at first).
        // Code to confirm changes and update Firestore.
        return view
    }

    suspend fun writeWeightInput(weightInput: Double) {
        val time = ZonedDateTime.now().withNano(0)
        val weightRecord = WeightRecord(
            weight = Mass.pounds(weightInput),
            time = time.toInstant(),
            zoneOffset = time.offset
        )
        val records = listOf(weightRecord)
        try {
            val healthConnectClient by lazy { context?.let { HealthConnectClient.getOrCreate(it) } }
            healthConnectClient?.insertRecords(records)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Successfully insert records", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private suspend fun retrieveHeight(): String {
        val field = "height"
        val docRef = db.collection("Users").document(auth.currentUser?.email.toString())
        return try {
            val docSnapshot = docRef.get().await()
            docSnapshot.getString(field)?.toString() ?: field
        } catch (e: FirebaseFirestoreException) {
            e.printStackTrace()
            field
        }
    }
    private suspend fun retrieveWeight(): String {
        val field = "weight"
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