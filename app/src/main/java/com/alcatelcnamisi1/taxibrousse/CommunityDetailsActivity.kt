package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton

class CommunityDetailsActivity : AppCompatActivity() {

    private lateinit var textViewCommunityName: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var buttonProposeRide: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_details)

        textViewCommunityName = findViewById(R.id.textViewCommunityName)
        textViewDestination = findViewById(R.id.textViewDestination)
        buttonProposeRide = findViewById(R.id.buttonProposeRide)

        val communityName = intent.getStringExtra("communityName")
        val destination = intent.getStringExtra("destination")

        textViewCommunityName.text = communityName
        textViewDestination.text = destination

        buttonProposeRide.setOnClickListener {
            println("button Propose Ride clicked")
            val fragment = ProposeRideFragment()
            fragment.arguments = Bundle().apply {
                println("Passing destination: $destination") // Pour vérifier si la destination est présente
                putString("arrival", destination)
            }
            openFragment(fragment, R.id.frameLayoutRidesContainer)
        }

        openFragment(ViewRidesFragment(), R.id.frameLayoutRidesContainer)
    }

    private fun openFragment(fragment: Fragment, containerId: Int) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
