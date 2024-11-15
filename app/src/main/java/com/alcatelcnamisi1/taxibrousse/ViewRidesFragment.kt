package com.alcatelcnamisi1.taxibrousse

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.*

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
                "date" to rideJson.getString("date"),
                "seatsAvailable" to rideJson.getString("seatsAvailable"),
                "recurrence" to rideJson.getString("recurrence"),
                "description" to rideJson.getString("description")
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
            val dateTextView: TextView = rideView.findViewById(R.id.textViewDate)
            val seatsTextView: TextView = rideView.findViewById(R.id.textViewSeatsAvailable)
            val recurrenceTextView: TextView = rideView.findViewById(R.id.textViewRecurrence)
            val descriptionTextView: TextView = rideView.findViewById(R.id.textViewDescription)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val overlayUnavailable: View = rideView.findViewById(R.id.overlayUnavailable)
            val textUnavailable: TextView = rideView.findViewById(R.id.textViewUnavailable)
            val textFull: TextView = rideView.findViewById(R.id.textViewFull)
            val textSeeDescription: TextView = rideView.findViewById(R.id.textViewSeeDescription)


            departureTextView.text = "Departure : ${ride["departure"]}"
            dateTextView.text = ride["date"]
            seatsTextView.text = "${ride["seatsAvailable"]} seats available"
            recurrenceTextView.text = ride["recurrence"]
            descriptionTextView.text = ride["description"]

            if (ride["seatsAvailable"] == "0") {
                overlayUnavailable.visibility = View.VISIBLE
                textFull.visibility = View.VISIBLE
            }

            val rideDate = dateFormat.parse(ride["date"] ?: "")
            val currentDate = Date()
            if (rideDate != null && rideDate.before(currentDate)) {
                overlayUnavailable.visibility = View.VISIBLE
                textUnavailable.visibility = View.VISIBLE
            }

            val cardViewRide: CardView = rideView.findViewById(R.id.cardViewRide)
            cardViewRide.setOnClickListener {
                if(descriptionTextView.visibility == View.VISIBLE) {
                    textSeeDescription.text = "See ride description"
                    descriptionTextView.visibility = View.GONE
                }
                else{
                    textSeeDescription.text = "Hide ride description"
                    descriptionTextView.visibility = View.VISIBLE
                }
            }

            linearLayout.addView(rideView)
        }

        linearLayout.post {
            (linearLayout.parent as ScrollView).fullScroll(View.FOCUS_DOWN)
        }
    }

}
