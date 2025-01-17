package com.alcatelcnamisi1.taxibrousse
import ApiRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONException

class MyRidesFragment : Fragment() {
    private lateinit var linearLayout: LinearLayout
    private var arrival: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        ApiRequest.getInstance(requireContext()).getMyTrips(
            onResponse = { response ->
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

    private fun parseRidesResponse(response: String): List<Map<String, String>> {
        val rideList = mutableListOf<Map<String, String>>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val rideJson = jsonArray.getJSONObject(i)
            val rideMap = mapOf(
                "departure" to rideJson.getString("departure"),
                "arrival" to rideJson.getString("arrival"),
                "date" to rideJson.getString("date"),
                "description" to rideJson.getString("description")
            )
            rideList.add(rideMap)
        }

        return rideList
    }

    private fun displayRides(rides: List<Map<String, String>>) {

        for (ride in rides) {
            val rideView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_my_rides, linearLayout, false)

            val departureArrivalTextView: TextView = rideView.findViewById(R.id.textViewDepartureArrival)
            val dateTextView: TextView = rideView.findViewById(R.id.textViewDate)
            val descriptionTextView: TextView = rideView.findViewById(R.id.textViewDescription)
            val textSeeDescription: TextView = rideView.findViewById(R.id.textViewSeeDescription)

            departureArrivalTextView.text = "${ride["departure"]} --> ${ride["arrival"]}"
            dateTextView.text = "Départ le : ${ride["date"]}"
            descriptionTextView.text = ride["description"]

            val cardViewRide: CardView = rideView.findViewById(R.id.cardViewMyRide)
            cardViewRide.setOnClickListener {
                if(descriptionTextView.visibility == View.VISIBLE) {
                    textSeeDescription.text = "Afficher description"
                    descriptionTextView.visibility = View.GONE
                }
                else{
                    textSeeDescription.text = "Masquer description"
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
