package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton

class CommunityDetailsActivity : AppCompatActivity() {

    private lateinit var textViewCommunityName: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var buttonProposeRide: MaterialButton
    private lateinit var buttonBack: MaterialButton

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
            openProposeRideFragment()
        }

        buttonBack.setOnClickListener {
            navigateBackToHome()
        }
    }

    private fun openProposeRideFragment() {
        val fragment = ProposeRideFragment()
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayoutFragmentContainer, fragment)
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
