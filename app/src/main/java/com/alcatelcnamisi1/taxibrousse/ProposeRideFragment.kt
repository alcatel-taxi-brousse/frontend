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
        val arrival = editTextArrival?.text.toString().trim()
        val carModel = editTextCarModel?.text.toString().trim()
        val seats = spinnerSeats?.selectedItem.toString().trim()
        val dateTime = editTextDateTime?.text.toString().trim()

        if (!fieldsValidation(departure, arrival, carModel, seats, dateTime)) {
            Toast.makeText(requireContext(), "Veuillez remplir les champs obligatoires.", Toast.LENGTH_SHORT).show()
            return
        }

        val description = editTextDescription?.text.toString()
        val isRecurrent = checkBoxRecurrent?.isChecked ?: false
        val recurrence = if (isRecurrent) spinnerRecurrence?.selectedItem.toString() else "Non récurrent"

        ApiRequest.getInstance(requireContext()).proposeRide(
            departure, arrival, calendar.time, isRecurrent, recurrence, seats, carModel, description,
            { response ->
                Toast.makeText(requireContext(), "Trajet proposé avec succès !", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction().remove(this).commit()
            },
            { error ->
                Toast.makeText(requireContext(), "Erreur lors de la proposition du trajet : $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun fieldsValidation(departure: String, arrival: String, carModel: String, seats: String, dateTime: String): Boolean {
        var isValid = true

        if (departure.isEmpty()) {
            editTextDeparture?.background = resources.getDrawable(R.drawable.error_background, null)
            isValid = false
        }

        if (arrival.isEmpty()) {
            editTextArrival?.background = resources.getDrawable(R.drawable.error_background, null)
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

        editTextArrival?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    editTextArrival?.background = resources.getDrawable(android.R.drawable.edit_text, null)
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
