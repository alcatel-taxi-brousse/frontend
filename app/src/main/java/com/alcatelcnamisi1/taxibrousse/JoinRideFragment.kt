package com.alcatelcnamisi1.taxibrousse

import ApiRequest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "departure"
private const val ARG_PARAM2 = "arrival"
private const val ARG_PARAM3 = "date"
private const val ARG_PARAM4 = "seats_taken"
private const val ARG_PARAM5 = "seats_total"
private const val ARG_PARAM6 = "trip_id"
private const val ARG_PARAM7 = "community_id"


/**
 * A simple [Fragment] subclass.
 * Use the [JoinRideFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JoinRideFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var departure: String? = null
    private var arrival: String? = null
    private var date: String? = null
    private var seatsTaken: Int? = null
    private var seatsAvailable: Int? = null
    private var communityId : String? = null
    private var seatsToTake: Int = 1
    private var seatsUpdated: Int? = null
    private var trip_id:String? = null

    private var textViewDeparture: TextView? = null
    private var textViewDate: TextView? = null
    private var textViewSeatCountTotal: TextView? = null
    private var textViewSeatsCount: TextView? = null

    private var buttonSub: Button? = null
    private var buttonAdd: Button? = null
    private var buttonJoinRide: Button? = null
    private var buttonClose: Button? = null

    fun updateButtonsDisable() {
        buttonSub?.isEnabled = seatsToTake > 0
        buttonAdd?.isEnabled = seatsToTake < (seatsAvailable!! - seatsTaken!!)
        buttonJoinRide?.isEnabled = seatsToTake > 0
    }

    fun updateAvailableSeats() {
        seatsUpdated = (seatsAvailable!! - seatsToTake)
        textViewSeatCountTotal?.text = seatsUpdated.toString() + " place(s) still available ( /" + seatsAvailable.toString() + ")"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            departure = it.getString(ARG_PARAM1)
            arrival = it.getString(ARG_PARAM2)
            date = it.getString(ARG_PARAM3)
            seatsTaken = it.getInt(ARG_PARAM4)
            seatsAvailable = it.getInt(ARG_PARAM5)
            trip_id = it.getString(ARG_PARAM6)
            communityId = it.getString(ARG_PARAM7)

        }
        println("\n Data received : $arrival")
    }

    private fun closeJoinRide(){
        activity?.supportFragmentManager?.popBackStack()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_join_ride, container, false)

        // Get elements

        textViewDeparture = view?.findViewById(R.id.textViewDeparture)
        textViewDate = view?.findViewById(R.id.textViewDate)
        textViewSeatCountTotal = view?.findViewById(R.id.textViewSeatCountTotal)
        textViewSeatsCount = view?.findViewById(R.id.textViewSeatCount)
        buttonSub = view?.findViewById(R.id.buttonSub)
        buttonAdd = view?.findViewById(R.id.buttonAdd)
        buttonJoinRide = view?.findViewById(R.id.buttonJoinRide)
        buttonClose = view?.findViewById(R.id.buttonCloseRide)

        print(departure)
        textViewDeparture?.text = departure + "  ->  " + arrival
        textViewDate?.text = date
        updateAvailableSeats()
        textViewSeatsCount?.text = seatsToTake.toString()

        buttonSub?.setOnClickListener {
            seatsToTake--
            updateAvailableSeats()
            textViewSeatsCount?.text = seatsToTake.toString()
            updateButtonsDisable()
        }

        buttonAdd?.setOnClickListener {
            seatsToTake++
            updateAvailableSeats()
            textViewSeatsCount?.text = seatsToTake.toString()
            updateButtonsDisable()
        }

        buttonClose?.setOnClickListener {
            closeJoinRide()
        }

        buttonJoinRide?.setOnClickListener {
            // TODO
            println("JOIN RIDE with seats = $seatsToTake")


            //implement the query
            ApiRequest.getInstance(requireContext()).joinTrip(
                communityId.toString(), trip_id.toString(), seatsToTake,
                { response ->
                    Toast.makeText(requireContext(), "Trajet rejoint avec succès !", Toast.LENGTH_SHORT).show()
                    closeJoinRide()
                },
                { error ->
                    Toast.makeText(requireContext(), "Erreur lors de la tentative de rejoindre le trajet : $error", Toast.LENGTH_SHORT).show()
                }
            )
        }

        updateButtonsDisable()

        //println("Value of token : " + ApiRequest.getInstance(requireContext()).getToken())


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JoinRideFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinRideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}