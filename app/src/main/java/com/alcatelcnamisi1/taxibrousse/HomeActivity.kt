package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class HomeActivity : AppCompatActivity() {

    private var buttonCreateCommunity: ImageButton? = null
    private var buttonJoinRide: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

       buttonCreateCommunity = findViewById<ImageButton>(R.id.buttonCreateCommunity)

       buttonCreateCommunity?.setOnClickListener{
            print("button create Community clicked");

            val fragment = CreateCommunityFragment();

            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayoutCreateCommunity, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }


        buttonJoinRide = findViewById(R.id.buttonJoinRide)
        buttonJoinRide?.setOnClickListener {
            print("button Join Ride clicked");
            val fragment = JoinRideFragment()
            fragment.arguments = Bundle().apply {
                putString("destination", "Prison")
                putString("date", "2024-11-11")
                putInt("seats_taken", 2)
                putInt("seats_total", 5)
            }
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayoutCreateCommunity, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }




        val fragment = ViewCommunitiesFragment()
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutFragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}
