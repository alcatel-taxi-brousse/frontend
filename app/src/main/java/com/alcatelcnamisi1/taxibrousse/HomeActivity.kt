package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class HomeActivity : AppCompatActivity() {

    private lateinit var buttonCreateCommunity: Button
    private lateinit var buttonProposeRide: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        buttonCreateCommunity = findViewById(R.id.buttonCreateCommunity)
        buttonProposeRide = findViewById(R.id.buttonProposeRide)

        buttonCreateCommunity.setOnClickListener {
            openFragment(CreateCommunityFragment(), R.id.frameLayoutCommunitiesContainer)
        }

        buttonProposeRide.setOnClickListener {
            openFragment(ProposeRideFragment(), R.id.frameLayoutCommunitiesContainer)
        }

        // Charger ViewCommunitiesFragment et ViewRidesFragment au démarrage
        if (savedInstanceState == null) {
            openFragment(ViewCommunitiesFragment(), R.id.frameLayoutCommunitiesContainer)
            openFragment(ViewRidesFragment(), R.id.frameLayoutRidesContainer)
        }
    }

    private fun openFragment(fragment: Fragment, containerId: Int) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}
