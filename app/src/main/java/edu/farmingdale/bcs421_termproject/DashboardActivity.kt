package edu.farmingdale.bcs421_termproject

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    lateinit var bottomNavigation : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)
        // Programatically adjust status bar color since we use multiple colors throughout the app
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.light_gray)
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)
        var homeFragment = HomeFragment()
        var foodFragment = FoodFragment()
        var accountFragment = AccountFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, homeFragment)
            commit()
        }

        // Change which fragment shows based on the selected item (home/add food/account)
        bottomNavigation.setOnItemSelectedListener { it ->
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout, homeFragment)
                        commit()
                    }
                    true
                }
                R.id.food -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout, foodFragment)
                        commit()
                    }
                    true
                }
                R.id.account -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout, accountFragment)
                        commit()
                    }
                    true
                }
                else -> { false}
            }
        }

    }
}