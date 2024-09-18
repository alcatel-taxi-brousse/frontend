package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateCommunityFragment : Fragment() {
    private var editTextCommunityName: EditText? = null
    private var editTextDestination: EditText? = null
    private var editTextDescription: EditText? = null
    private var editTextVisibility: EditText? = null
    private var buttonCreateCommunity: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Retrieve parameters if needed
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_community, container, false)

        // Initialize your views here
        editTextCommunityName = view.findViewById(R.id.editTextCommunityName)
        editTextDestination = view.findViewById(R.id.editTextDestination)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextVisibility = view.findViewById(R.id.editTextVisibility)
        buttonCreateCommunity = view.findViewById(R.id.ButtonCreateCommunity)

        buttonCreateCommunity?.setOnClickListener {
            createCommunity()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun createCommunity() {
        val communityName = editTextCommunityName?.text.toString()
        val destination = editTextDestination?.text.toString()
        val description = editTextDescription?.text.toString()
        val visibility = editTextVisibility?.text.toString()

        ApiRequest.getInstance(null).createCommunity(communityName, destination, description, visibility, { response ->
            println("Response: $response")
        }, { error ->
            println("Error: $error")
        })
    }
}