package com.alcatelcnamisi1.taxibrousse

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "arrival"
private const val ARG_PARAM2 = "community_id"

class ProposeRideFragment : Fragment() {


    private var editTextDeparture: EditText? = null
    private var arrival: String? = null
    private var community_id: String? = null
    private var textViewArrival: TextView? = null
    private var editTextDateTime: EditText? = null
    private var checkBoxRecurrent: CheckBox? = null
    private var spinnerRecurrence: Spinner? = null
    private var spinnerSeats: Spinner? = null
    private var editTextCarModel: EditText? = null
    private var editTextDescription: EditText? = null
    private var buttonProposeRide: Button? = null
    private var buttonReturn: Button? = null
    private var calendar: Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrival = it.getString(ARG_PARAM1)
            community_id = it.getString(ARG_PARAM2)

            println("Received arrival: $arrival | $community_id")
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val view = inflater.inflate(R.layout.fragment_propose_ride, container, false)

        editTextDeparture = view.findViewById(R.id.editTextDeparture)
        textViewArrival = view.findViewById(R.id.textArrival)
        editTextDateTime = view.findViewById(R.id.editTextDateTime)
        checkBoxRecurrent = view.findViewById(R.id.checkBoxRecurrent)
        spinnerRecurrence = view.findViewById(R.id.spinnerRecurrence)
        spinnerSeats = view.findViewById(R.id.spinnerSeats)
        editTextCarModel = view.findViewById(R.id.editTextCarModel)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        buttonProposeRide = view.findViewById(R.id.buttonProposeRide)
        buttonReturn = view.findViewById(R.id.buttonReturn)

        editTextDateTime?.setOnClickListener {
            showDateTimePicker()
        }

        textViewArrival?.text = arrival ?: "No destination provided"

        val seatOptions = arrayOf("Veuillez selectionner une valeur", "1", "2", "3", "4", "5", "6", "7+")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, seatOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeats?.adapter = adapter

        val recurrenceOptions = arrayOf("Veuillez selectionner une valeur", "Journalier", "Hebdomadaire", "Mensuel")
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrenceOptions)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence?.adapter = recurrenceAdapter

        checkBoxRecurrent?.setOnCheckedChangeListener { _, isChecked ->
            spinnerRecurrence?.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        buttonProposeRide?.setOnClickListener {
            proposeRide()
        }

        buttonReturn?.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        fieldListeners()

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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun proposeRide() {

        val departure = editTextDeparture?.text.toString().trim()
        val arrival = textViewArrival?.text.toString().trim()
        val carModel = editTextCarModel?.text.toString().trim()
        val seats = spinnerSeats?.selectedItem.toString().trim()
        var dateTime = editTextDateTime?.text.toString().trim()

        if (!fieldsValidation(departure, arrival, carModel, seats, dateTime)) {
            Toast.makeText(requireContext(), "Veuillez remplir les champs obligatoires.", Toast.LENGTH_SHORT).show()
            return
        }

        dateTime = formatToIso(dateTime);

        val description = editTextDescription?.text.toString()
        val isRecurrent = checkBoxRecurrent?.isChecked ?: false
        val recurrence = if (isRecurrent) spinnerRecurrence?.selectedItem.toString() else "Non récurrent"

        community_id?.let {
            ApiRequest.getInstance(requireContext()).createTrip(
                it,departure,dateTime,recurrence, seats.toIntOrNull() ?: 0,description,
                { response ->
                    Toast.makeText(requireContext(), "Trajet proposé avec succès !", Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                },
                { error ->
                    Toast.makeText(requireContext(), "Erreur lors de la proposition du trajet : $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


    private fun formatToIso(dateTime: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC") // Définit le fuseau horaire en UTC

        return try {
            val date = inputFormat.parse(dateTime) // Convertit la chaîne au format `dd/MM/yyyy HH:mm` en objet `Date`
            isoFormat.format(date!!) // Formate l'objet `Date` en chaîne ISO 8601
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Retourne une chaîne vide en cas d'erreur
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fieldsValidation(departure: String, arrival: String, carModel: String, seats: String, dateTime: String): Boolean {
        var isValid = true

        if (departure.isEmpty()) {
            editTextDeparture?.background = resources.getDrawable(R.drawable.error_background, null)
            isValid = false
        }

        if (carModel.isEmpty()) {
            editTextCarModel?.background = resources.getDrawable(R.drawable.error_background, null)
            isValid = false
        }

        if (seats == "Veuillez selectionner une valeur") {
            val errorTextView = spinnerSeats?.selectedView as? TextView
            errorTextView?.setTextColor(resources.getColor(R.color.red))
            isValid = false
        }

        if (dateTime.isEmpty()) {
            editTextDateTime?.background = resources.getDrawable(R.drawable.error_background, null)
            isValid = false
        }

        return isValid
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fieldListeners() {
        editTextDeparture?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    editTextDeparture?.background = resources.getDrawable(android.R.drawable.edit_text, null)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editTextCarModel?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    editTextCarModel?.background = resources.getDrawable(android.R.drawable.edit_text, null)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        editTextDateTime?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    editTextDateTime?.background = resources.getDrawable(android.R.drawable.edit_text, null)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinnerSeats?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    (view as? TextView)?.setTextColor(resources.getColor(R.color.black))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
