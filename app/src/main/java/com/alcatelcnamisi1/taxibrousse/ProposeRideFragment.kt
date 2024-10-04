package com.alcatelcnamisi1.taxibrousse

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.Deflater

class ProposeRideFragment : Fragment() {

    private var editTextDeparture: EditText? = null
    private var editTextArrival: EditText? = null
    private var editTextDateTime: EditText? = null
    private var checkBoxRecurrent: CheckBox? = null
    private var spinnerRecurrence: Spinner? = null
    private var spinnerSeats: Spinner? = null
    private var editTextCarModel: EditText? = null
    private var editTextDescription: EditText? = null
    private var buttonProposeRide: Button? = null
    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_propose_ride, container, false)

        editTextDeparture = view.findViewById(R.id.editTextDeparture)
        editTextArrival = view.findViewById(R.id.editTextArrival)
        editTextDateTime = view.findViewById(R.id.editTextDateTime)
        checkBoxRecurrent = view.findViewById(R.id.checkBoxRecurrent)
        spinnerRecurrence = view.findViewById(R.id.spinnerRecurrence)
        spinnerSeats = view.findViewById(R.id.spinnerSeats)
        editTextCarModel = view.findViewById(R.id.editTextCarModel)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        buttonProposeRide = view.findViewById(R.id.buttonProposeRide)

        editTextDateTime?.setOnClickListener {
            showDateTimePicker()
        }

        val seatOptions = arrayOf("1", "2", "3", "4", "5", "6", "7+")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seatOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeats?.adapter = adapter

        val recurrenceOptions = arrayOf("Journalier", "Hebdomadaire", "Mensuel")
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrenceOptions)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence?.adapter = recurrenceAdapter

        checkBoxRecurrent?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spinnerRecurrence?.visibility = View.VISIBLE
            } else {
                spinnerRecurrence?.visibility = View.GONE
            }
        }


        buttonProposeRide?.setOnClickListener {
            proposeRide()
        }

        return view
    }

    private fun showDateTimePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            calendar.set(year, monthOfYear, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                editTextDateTime?.setText(dateFormat.format(calendar.time))

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, currentYear, currentMonth, currentDay).show()
    }

    private fun proposeRide() {
        val departure = editTextDeparture?.text.toString()
        val arrival = editTextArrival?.text.toString()
        val dateTime = calendar.time
        val isRecurrent = checkBoxRecurrent?.isChecked ?: false
        val seats = spinnerSeats?.selectedItem.toString()
        val carModel = editTextCarModel?.text.toString()
        val description = editTextDescription?.text.toString()

        val recurrence = if (isRecurrent) spinnerRecurrence?.selectedItem.toString() else "Non récurrent"

        
        println("Trajet proposé : $departure -> $arrival, Date/Heure: $dateTime, Récurrent: $recurrence, Places: $seats, Véhicule: $carModel, Description: $description")

        /* Api méthode proposeRide() a créer
        ApiRequest.getInstance(null).proposeRide(
            departure, arrival, dateTime, isRecurrent, seats, carModel, description,
            { response -> println("Response: $response") },
            { error -> println("Error: $error") }
        )*/

        val fragmentManager = parentFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
    }
}
