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

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()

                if (query.isEmpty()) {
                    val joinedCommunities = allCommunities.filter {
                        joinedCommunityIds.contains(it["community_id"])
                    }
                    displayCommunities(joinedCommunities)
                } else {
                    ApiRequest.getInstance(requireContext()).searchCommunities(
                        searchQuery = query,
                        onResponse = { response ->
                            try {
                                val searchResults = parseCommunityResponse(response)
                                val filteredResults = searchResults.filter {
                                    !joinedCommunityIds.contains(it["community_id"])
                                }
                                displayCommunities(filteredResults, isSearchResult = true)
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

        fetchCommunities()
    }

    private fun fetchCommunities() {
        ApiRequest.getInstance(requireContext()).getCommunities(
            onResponse = { response ->
                try {
                    val communities = parseCommunityResponse(response)
                    allCommunities.clear()
                    allCommunities.addAll(communities)

                    val joinedCommunities = communities.filter {
                        joinedCommunityIds.contains(it["community_id"])
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
            communityMap["community_id"] = communityJson.getString("community_id")
            communityMap["name"] = communityJson.getString("name")
            communityMap["destination"] = communityJson.getString("destination")
            communityList.add(communityMap)
        }

        return communityList
    }

    private fun displayCommunities(
        communities: List<HashMap<String, String>>,
        isSearchResult: Boolean = false
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

            val communityId = community["community_id"] ?: ""
            val isJoined = joinedCommunityIds.contains(communityId)

            if (isSearchResult) {
                joinButton.visibility = if (!isJoined) View.VISIBLE else View.GONE
            } else {
                joinButton.visibility = View.GONE
            }

            communityView.setOnClickListener {
                val communityName = community["name"]
                val destination = community["destination"]

                val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                intent.putExtra("communityName", communityName)
                intent.putExtra("destination", destination)
                startActivity(intent)
            }

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
