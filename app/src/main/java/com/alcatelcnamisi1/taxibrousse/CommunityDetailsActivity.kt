package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.ale.rainbowsdk.RainbowSdk

class CommunityDetailsActivity : AppCompatActivity() {

    private lateinit var textViewCommunityName: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var buttonProposeRide: ImageButton
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonChat: ImageButton // Nouveau bouton Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_details)

        textViewCommunityName = findViewById(R.id.textViewCommunityName)
        textViewDestination = findViewById(R.id.textViewDestination)
        buttonProposeRide = findViewById(R.id.buttonProposeRide)
        buttonBack = findViewById(R.id.buttonBack)
        buttonChat = findViewById(R.id.buttonChat) // Initialisation du bouton Chat

        val communityName = intent.getStringExtra("communityName")
        val destination = intent.getStringExtra("destination")
        val community_id = intent.getStringExtra("community_id")

        textViewCommunityName.text = communityName
        textViewDestination.text = destination

        // Listener pour le bouton "Proposer un trajet"
        buttonProposeRide.setOnClickListener {
            val fragment = ProposeRideFragment()
            fragment.arguments = Bundle().apply {
                putString("arrival", destination)
                putString("community_id", community_id)
            }
            openFragment(fragment, R.id.frameLayoutRidesContainer)
        }

        // Listener pour le bouton "Retour"
        buttonBack.setOnClickListener {
            navigateBackToHome()
        }

        // Listener pour le bouton "Chat"
        buttonChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)

            intent.putExtra("community_name", communityName);
            startActivity(intent)
        }

        val fragment = ViewRidesFragment()
        fragment.arguments = Bundle().apply {
            putString("arrival", destination)
            putString("community_id", community_id)
            println("\n Data sent : $destination" + " | " + community_id)
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
        finish() // Optionnel : termine l'activité actuelle
    }
}
