package edu.farmingdale.bcs421_termproject

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import edu.farmingdale.bcs421_termproject.databinding.FragmentAccountBinding
import edu.farmingdale.bcs421_termproject.ProgressFragment

private lateinit var binding: FragmentAccountBinding
lateinit var firebaseAuth: FirebaseAuth
class AccountFragment : Fragment(R.layout.fragment_account) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        var view :View = binding.root
        firebaseAuth = FirebaseAuth.getInstance()

        val personalInfoButton = view.findViewById<Button>(R.id.infoButton)
        val bodyMeasurementsButton = view.findViewById<Button>(R.id.measurementsButton)
        val nutritionButton = view.findViewById<Button>(R.id.nutritionButton)
        val goalsButton = view.findViewById<Button>(R.id.goalsButton)
        val progressButton = view.findViewById<Button>(R.id.progressButton)
        val recipesButton = view.findViewById<Button>(R.id.recipesButton)
        val friendsButton = view.findViewById<Button>(R.id.friendsButton)
        val syncButton = view.findViewById<Button>(R.id.syncButton)
        val logOutButton = view.findViewById<Button>(R.id.logOutButton)

        val personalInfoFragment = PersonalInformationFragment()
        val measurementsFragment = BodyMeasurementsFragment()
        val nutritionFragment = NutritionFragment()
        val goalsFragment = GoalsFragment()
        val progressFragment = ProgressFragment()
        val recipesFragment = RecipesFragment()
        val friendsFragment = FriendsFragment()
        val syncFragment = SyncFragment()

        personalInfoButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, personalInfoFragment)
                commit()
            }
        }

        bodyMeasurementsButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, measurementsFragment)
                commit()
            }
        }

        nutritionButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, nutritionFragment)
                commit()
            }
        }

        goalsButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, goalsFragment)
                commit()
            }
        }

        progressButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, progressFragment)
                commit()
            }
        }

        recipesButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, recipesFragment)
                commit()
            }
        }

        friendsButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayout, friendsFragment)
                commit()
            }
        }


        syncButton.setOnClickListener {
            val packageName = "edu.farmingdale.bcs421_termproject"

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                // Check if the required permissions are granted
                if (arePermissionsGranted()) {
                    // Permissions are granted, navigate to SyncFragment
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout, syncFragment)
                        commit()
                    }
                } else {
                    // Permissions are not granted, open app settings
                    startActivity(intent)
                }
            } else {
                // Handle the case where there's no activity to handle the intent
                // You can show a toast or a message to inform the user
            }
        }

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            firebaseAuth.signOut()
        }
        return view
    }

    companion object {
        // Define a constant for your permission request code
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    private fun arePermissionsGranted(): Boolean {
        val requiredPermissions = arrayOf(
            "android.permission.health.READ_HEART_RATE",
            "android.permission.health.WRITE_HEART_RATE",
            "android.permission.health.READ_EXERCISE",
            "android.permission.health.WRITE_EXERCISE",
            "android.permission.health.READ_TOTAL_CALORIES_BURNED",
            "android.permission.health.WRITE_TOTAL_CALORIES_BURNED",
            "android.permission.health.READ_WEIGHT",
            "android.permission.health.WRITE_WEIGHT",
            "android.permission.health.READ_BASAL_METABOLIC_RATE",
            "android.permission.health.WRITE_HEIGHT",
            "android.permission.health.READ_HEIGHT"
        )

        // Check if all required permissions are granted
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Return false if any required permission is not granted
                return false
            }
        }

        // Return true if all required permissions are granted
        return true
    }
}
