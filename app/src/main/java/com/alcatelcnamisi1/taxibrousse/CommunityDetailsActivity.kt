package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

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

        textViewCommunityName.text = communityName
        textViewDestination.text = destination

        // Listener pour le bouton "Proposer un trajet"
        buttonProposeRide.setOnClickListener {
            val fragment = ProposeRideFragment()
            fragment.arguments = Bundle().apply {
                putString("arrival", destination)
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
            startActivity(intent)
        }

        // Charger le fragment par défaut
        openFragment(ViewRidesFragment(), R.id.frameLayoutRidesContainer)
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
