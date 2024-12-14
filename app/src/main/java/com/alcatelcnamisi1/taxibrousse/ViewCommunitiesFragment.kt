package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONArray
import org.json.JSONException

class ViewCommunitiesFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var searchBar: EditText
    private val joinedCommunityIds = mutableSetOf<String>()
    private val allCommunities = mutableListOf<HashMap<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_view_communities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayout = view.findViewById(R.id.linearLayoutCommunities)
        searchBar = requireActivity().findViewById(R.id.searchBar)

        // Add text change listener for search
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filteredCommunities = allCommunities.filter { community ->
                    community["name"]?.lowercase()?.contains(query) == true ||
                            community["destination"]?.lowercase()?.contains(query) == true
                }
                displayCommunities(filteredCommunities)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchJoinedCommunities { fetchCommunities() }
    }

    private fun fetchCommunities() {

        ApiRequest.getInstance(requireContext()).getCommunities(
            onResponse = { response ->
                try {

                    val communities = parseCommunityResponse(response)
                    allCommunities.clear()
                    allCommunities.addAll(communities)
                    displayCommunities(communities)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing communities", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error fetching communities: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun parseCommunityResponse(response: String): List<HashMap<String, String>> {
        val communityList = mutableListOf<HashMap<String, String>>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val communityJson = jsonArray.getJSONObject(i)
            val communityMap = HashMap<String, String>()

            communityMap["id"] = communityJson.getString("id") // Parse the id
            communityMap["name"] = communityJson.getString("name")
            communityMap["destination"] = communityJson.getString("destination")

            communityList.add(communityMap)
        }

        return communityList
    }



    private fun fetchJoinedCommunities(onComplete: () -> Unit) {
        ApiRequest.getInstance(requireContext()).getJoinedCommunities(
            onResponse = { response ->
                try {
                    joinedCommunityIds.clear()
                    joinedCommunityIds.addAll(parseJoinedCommunityResponse(response))
                    onComplete()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing joined communities", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error fetching joined communities: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun parseJoinedCommunityResponse(response: String): Set<String> {
        val joinedIds = mutableSetOf<String>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            joinedIds.add(jsonObject.getString("id"))
        }

        return joinedIds
    }


    private fun displayCommunities(communities: List<HashMap<String, String>>) {
        linearLayout.removeAllViews()

        for (community in communities) {
            val communityView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_community, linearLayout, false)

            val nameTextView: TextView = communityView.findViewById(R.id.textViewCommunityName)
            val destinationTextView: TextView = communityView.findViewById(R.id.textViewCommunityDestination)
            val joinButton: Button = communityView.findViewById(R.id.buttonJoinCommunity)
            val arrowButton: ImageButton = communityView.findViewById(R.id.buttonArrow)

            nameTextView.text = community["name"]
            destinationTextView.text = community["destination"]

            val communityId = community["id"] ?: ""
            val isJoined = joinedCommunityIds.contains(communityId)

            // Update button state based on join status
            joinButton.text = if (isJoined) "Joined" else "Join"
            joinButton.isEnabled = !isJoined

            joinButton.setOnClickListener {
                if (!isJoined) {
                    ApiRequest.getInstance(requireContext()).joinCommunity(
                        community,
                        onResponse = {
                            joinedCommunityIds.add(communityId)
                            joinButton.text = "Joined"
                            joinButton.isEnabled = false
                            Toast.makeText(
                                requireContext(),
                                "Successfully joined ${community["name"]}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = { error ->
                            Toast.makeText(
                                requireContext(),
                                "Error joining community: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            arrowButton.setOnClickListener {
                val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                intent.putExtra("communityName", community["name"])
                intent.putExtra("destination", community["destination"])
                startActivity(intent)
            }

            linearLayout.addView(communityView)
        }
    }








}
