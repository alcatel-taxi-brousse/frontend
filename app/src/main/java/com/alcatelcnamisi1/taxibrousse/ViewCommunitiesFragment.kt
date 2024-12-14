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
import androidx.cardview.widget.CardView
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

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()

                if (query.isEmpty()) {
                    val joinedCommunities = allCommunities.filter {
                        joinedCommunityIds.contains(it["id"])
                    }
                    displayCommunities(joinedCommunities)
                } else {
                    ApiRequest.getInstance(requireContext()).searchCommunities(
                        searchQuery = query,
                        onResponse = { response ->
                            try {
                                val searchResults = parseCommunityResponse(response)
                                displayCommunities(searchResults, showJoinButton = true)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Toast.makeText(requireContext(), "Error parsing search results", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { error ->
                            Toast.makeText(requireContext(), "Error searching communities: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
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
                    val joinedCommunities = communities.filter {
                        joinedCommunityIds.contains(it["id"])
                    }
                    displayCommunities(joinedCommunities)
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
            communityMap["id"] = communityJson.getString("id")
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

    private fun displayCommunities(
        communities: List<HashMap<String, String>>,
        showJoinButton: Boolean = false
    ) {
        linearLayout.removeAllViews()

        for (community in communities) {
            val communityView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_community, linearLayout, false)

            val nameTextView: TextView = communityView.findViewById(R.id.textViewCommunityName)
            val destinationTextView: TextView = communityView.findViewById(R.id.textViewCommunityDestination)
            val joinButton: Button = communityView.findViewById(R.id.buttonJoinCommunity)

            nameTextView.text = community["name"]
            destinationTextView.text = community["destination"]

            val communityId = community["id"] ?: ""
            val isJoined = joinedCommunityIds.contains(communityId)


            joinButton.visibility = if (showJoinButton && !isJoined) View.VISIBLE else View.GONE

            joinButton.setOnClickListener {
                ApiRequest.getInstance(requireContext()).joinCommunity(
                    communityId = communityId,
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

            linearLayout.addView(communityView)
        }
    }
}
