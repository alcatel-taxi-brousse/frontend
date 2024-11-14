package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONException

class ViewRidesFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout

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
        ApiRequest.getInstance(requireContext()).getRides(
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
                "seatsAvailable" to rideJson.getString("seatsAvailable"),
                "recurrence" to rideJson.getString("recurrence")
            )
            rideList.add(rideMap)
        }

        return rideList
    }

    private fun displayRides(rides: List<Map<String, String>>) {
        for (ride in rides) {
            val rideView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_rides, linearLayout, false)

            val departureTextView: TextView = rideView.findViewById(R.id.textViewDeparture)
            val arrivalTextView: TextView = rideView.findViewById(R.id.textViewArrival)
            val dateTextView: TextView = rideView.findViewById(R.id.textViewDate)
            val seatsTextView: TextView = rideView.findViewById(R.id.textViewSeatsAvailable)
            val recurrenceTextView: TextView = rideView.findViewById((R.id.textViewRecurrence))

            departureTextView.text = ride["departure"]
            arrivalTextView.text = ride["arrival"]
            dateTextView.text = ride["date"]
            seatsTextView.text = "${ride["seatsAvailable"]} places disponibles"
            recurrenceTextView.text = ride["recurrence"]

            linearLayout.addView(rideView)
        }
    }
}
