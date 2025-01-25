package com.alcatelcnamisi1.taxibrousse

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

private const val ARG_PARAM1 = "arrival"
private const val ARG_PARAM2 = "community_id"


class ViewRidesFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout
    private var arrival: String? = null
    private var community_id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrival = it.getString(ARG_PARAM1)
            community_id = it.getString(ARG_PARAM2)

            println("Received arrival: $arrival" + " | " + community_id)
        }
        println("\n OnCreate received data : $arrival" + " | " + community_id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_rides, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearLayout = view.findViewById(R.id.linearLayoutRides)


        fetchRides()
    }

    private fun fetchRides() {
        community_id?.let {
            ApiRequest.getInstance(requireContext()).getTrips(it,
                onResponse = { response ->
                    println("REPONSE DANS LE VRAI FRONT : " + response);
                    try {
                        val rides = parseRidesResponse(response)
                        displayRides(rides)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error parsing rides", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Error fetching rides: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun parseRidesResponse(response: String): List<Map<String, String>> {
        val rideList = mutableListOf<Map<String, String>>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val rideJson = jsonArray.getJSONObject(i)
            //users_id = users_id+rideJson.getString("users_id")
            val rideMap = mapOf(
                "departure" to rideJson.getString("departure"),
                "date" to rideJson.getString("date"),
                "seatsAvailable" to rideJson.getString("seatsAvailable"),
                "recurrence" to rideJson.getString("recurrence"),
                "description" to rideJson.getString("description"),
                "trip_id" to rideJson.getString("trip_id"),
                "users_id" to rideJson.getString("users_id")
            )
            rideList.add(rideMap)
        }
        return rideList
    }

    private fun displayRides(rides: List<Map<String, String>>) {
        val reversedRides = rides.asReversed()

        for (ride in reversedRides) {
            val rideView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_rides, linearLayout, false)

            val departureTextView: TextView = rideView.findViewById(R.id.textViewDeparture)
            val dateTextView: TextView = rideView.findViewById(R.id.textViewDate)
            val seatsTextView: TextView = rideView.findViewById(R.id.textViewSeatsAvailable)
            val recurrenceTextView: TextView = rideView.findViewById(R.id.textViewRecurrence)
            val descriptionTextView: TextView = rideView.findViewById(R.id.textViewDescription)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val overlayUnavailable: View = rideView.findViewById(R.id.overlayUnavailable)
            val textUnavailable: TextView = rideView.findViewById(R.id.textViewUnavailable)
            val textFull: TextView = rideView.findViewById(R.id.textViewFull)
            val textSeeDescription: TextView = rideView.findViewById(R.id.textViewSeeDescription)
            val buttonJoinRide: Button = rideView.findViewById(R.id.buttonJoinRide)


            departureTextView.text = "Départ : ${ride["departure"]}"
            dateTextView.text = ride["date"]
            seatsTextView.text = "${ride["seatsAvailable"]} siège(s) disponibl(s)"
            recurrenceTextView.text = ride["recurrence"]
            descriptionTextView.text = ride["description"]
            buttonJoinRide.isClickable = true

            val rideDate = dateFormat.parse(ride["date"] ?: "")
            val currentDate = Date()
            var isInTheTrip = false;

            var currentUserId = ApiRequest.getInstance(requireContext()).getActiveUserId()

            val usersIdList = ride["users_id"]?.split(";")

            if (rideDate != null && rideDate.before(currentDate)) {
                textUnavailable.visibility = View.VISIBLE
                buttonJoinRide.isClickable = false
                buttonJoinRide.isEnabled = false
            }
            if (usersIdList != null) {
                if (currentUserId in usersIdList) {
                   isInTheTrip = true;
                }
            }
            if (isInTheTrip == true ) {
                buttonJoinRide.text = "Modifier"
            }
            if (isInTheTrip == true && ride["seatsAvailable"] == "0" || (rideDate != null && rideDate.before(currentDate))) {
                overlayUnavailable.visibility = View.VISIBLE
            }
            else if (isInTheTrip==false && ride["seatsAvailable"] == "0" || (rideDate != null && rideDate.before(currentDate))) {
                overlayUnavailable.visibility = View.VISIBLE
                buttonJoinRide.isClickable = false
                buttonJoinRide.isEnabled = false

                if (ride["seatsAvailable"] == "0") {
                    textFull.visibility = View.VISIBLE
                }
            }

            buttonJoinRide.setOnClickListener {
                print("\n button Join Ride clicked")
                val fragment = JoinRideFragment()
                println("\n Data sent : $arrival")
                fragment.arguments = Bundle().apply {
                    putString("departure", ride["departure"])
                    putString("arrival", arrival)
                    putString("date", ride["date"])
                    putString("community_id", community_id)
                    putString("trip_id", ride["trip_id"])
                    putInt("seats_taken", 0)
                    putString("users_id", ride["users_id"])
                    ride["seatsAvailable"]?.let { it1 -> putInt("seats_total", it1.toInt()) }
                }
                print("\n Departure : " + ride["departure"])
                val fragmentManager: FragmentManager = parentFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frameLayoutRidesContainer, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            val cardViewRide: CardView = rideView.findViewById(R.id.cardViewRide)
            cardViewRide.setOnClickListener {
                if(descriptionTextView.visibility == View.VISIBLE) {
                    textSeeDescription.text = "Voir la description"
                    descriptionTextView.visibility = View.GONE
                }
                else{
                    textSeeDescription.text = "Masquer la description"
                    descriptionTextView.visibility = View.VISIBLE
                }
            }

            linearLayout.addView(rideView)
        }

        linearLayout.post {
            (linearLayout.parent as ScrollView)
        }
    }

}
