package com.alcatelcnamisi1.taxibrousse
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class HomeActivity : AppCompatActivity() {

    private var buttonCreateCommunity: ImageButton? = null
    //private var buttonJoinRide: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        //a supp
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //a supp

        buttonCreateCommunity = findViewById(R.id.buttonCreateCommunity)
        //buttonProposeRide = findViewById(R.id.buttonProposeRide)

        buttonCreateCommunity?.setOnClickListener {
            openFragment(CreateCommunityFragment(), R.id.frameLayoutCommunitiesContainer)
        }

        /*buttonProposeRide.setOnClickListener {
            openFragment(ProposeRideFragment(), R.id.frameLayoutCommunitiesContainer)
        }*/

        if (savedInstanceState == null) {
            openFragment(ViewCommunitiesFragment(), R.id.frameLayoutCommunitiesContainer)
            openFragment(MyRidesFragment(), R.id.frameLayoutMyRides)
            //openFragment(ViewRidesFragment(), R.id.frameLayoutRidesContainer)
        }
    }


    private fun openFragment(fragment: Fragment, containerId: Int) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()


        ApiRequest.getInstance(null).getCommunity({ response ->
            println("Response: $response")
        }, { error ->
            println("Error: $error")
        })


    }
}
