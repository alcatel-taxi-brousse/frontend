package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton

class CommunityDetailsActivity : AppCompatActivity() {

    private lateinit var textViewCommunityName: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var buttonProposeRide: ImageButton
    private lateinit var buttonBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_details)

        textViewCommunityName = findViewById(R.id.textViewCommunityName)
        textViewDestination = findViewById(R.id.textViewDestination)
        buttonProposeRide = findViewById(R.id.buttonProposeRide)
        buttonBack = findViewById(R.id.buttonBack)

        val communityName = intent.getStringExtra("communityName")
        val destination = intent.getStringExtra("destination")

        textViewCommunityName.text = communityName
        textViewDestination.text = destination


        buttonProposeRide.setOnClickListener {
            println("button Propose Ride clicked")
            val fragment = ProposeRideFragment()
            fragment.arguments = Bundle().apply {
                println("Passing destination: $destination")
                putString("arrival", destination)
            }
            openFragment(fragment, R.id.frameLayoutRidesContainer)
        }

        buttonBack.setOnClickListener {
            navigateBackToHome()
        }

        val fragment = ViewRidesFragment()
        fragment.arguments = Bundle().apply {
            putString("arrival", destination)
            println("\n Data sent : $destination")
        }
        openFragment(fragment, R.id.frameLayoutRidesContainer)
    }

    private fun openFragment(fragment: Fragment, containerId: Int) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }


    private fun navigateBackToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Optional to clear the current activity
    }
}
