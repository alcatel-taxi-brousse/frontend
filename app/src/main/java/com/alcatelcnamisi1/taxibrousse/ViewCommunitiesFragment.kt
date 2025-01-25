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
import android.widget.ImageView
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

    val communityJoined: MutableList<String> = mutableListOf()

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
                    displayCommunities(allCommunities)
                } else {
                    ApiRequest.getInstance(requireContext()).searchCommunities(
                        searchQuery = query,
                        onResponse = { response ->
                            try {
                                val searchResults = parseCommunityResponseSearch(response)
                                val filteredResults = searchResults.filter {
                                    true
                                    //!joinedCommunityIds.contains(it["community_id"])
                                }
                                displayCommunitiesSearch(filteredResults, isSearchResult = true)
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
        ApiRequest.getInstance(requireContext()).getCommunity(
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

        println("VIEW COMMUNITY FRAGMENT - jsonArray : "+ jsonArray )

        for (i in 0 until jsonArray.length()) {
            val communityJson = jsonArray.getJSONObject(i)
            val communityMap = HashMap<String, String>()
            if(communityJson.has("community_id")) {
                communityMap["community_id"] = communityJson.getString("community_id")
            } else {
                communityMap["community_id"] = communityJson.getString("id")
            }
            communityMap["name"] = communityJson.getString("name")
            communityMap["destination"] = communityJson.getString("destination")
            //Cette ligne provoque l'erreur de parsing sur le search
            communityMap["currentUserInCommunity"]= communityJson.getString("currentUserInCommunity")
            if (communityMap["currentUserInCommunity"] == "true") {
                println("ViewCommunityFragment - On ajoute l'id de communauté : " + communityMap["community_id"])
                communityMap["community_id"]?.let { communityId ->
                    communityJoined.add(communityId)
                }
            }

            communityList.add(communityMap)
        }
        return communityList
    }

    private fun parseCommunityResponseSearch(response: String): List<HashMap<String, String>> {
        val communityList = mutableListOf<HashMap<String, String>>()
        val jsonArray = JSONArray(response)

        println("VIEW COMMUNITY FRAGMENT - jsonArray : "+ jsonArray )

        for (i in 0 until jsonArray.length()) {
            val communityJson = jsonArray.getJSONObject(i)
            val communityMap = HashMap<String, String>()
            if(communityJson.has("community_id")) {
                communityMap["community_id"] = communityJson.getString("community_id")
                if(communityMap["community_id"] in communityJoined){
                    communityMap["isJoined"]="true";
                }
                else{
                    communityMap["isJoined"]="false";
                }
            } else {
                communityMap["community_id"] = communityJson.getString("id")
            }
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
            if(community["currentUserInCommunity"] == "true"){
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
                    if (!isJoined) {
                        val communityName = community["name"]
                        val destination = community["destination"]

                        val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                        intent.putExtra("communityName", communityName)
                        intent.putExtra("destination", destination)
                        intent.putExtra("community_id", communityId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Veuillez rejoindre cette communauté pour voir les détails.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                joinButton.setOnClickListener {
                    ApiRequest.getInstance(requireContext()).joinCommunity(
                        communityId = communityId,
                        onResponse = {
                            joinedCommunityIds.add(communityId)
                            joinButton.text = "Rejointe"
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
    private fun displayCommunitiesSearch(
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
            val arrowImageView: ImageView = communityView.findViewById(R.id.buttonArrow)

            nameTextView.text = community["name"]
            destinationTextView.text = community["destination"]

            val communityId = community["community_id"] ?: ""
            var isJoined = false;

            if(community["isJoined"]=="true"){
                isJoined = true;
            }

            if (isSearchResult) {
                if(!isJoined){
                    joinButton.visibility = View.VISIBLE;
                    arrowImageView.visibility = View.INVISIBLE;
                    communityView.setOnClickListener {
                    }
                }
                else{
                    joinButton.visibility = View.GONE;

                    communityView.setOnClickListener {
                        val communityName = community["name"]
                        val destination = community["destination"]

                        val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                        intent.putExtra("communityName", communityName)
                        intent.putExtra("destination", destination)
                        intent.putExtra("community_id", communityId)
                        startActivity(intent)
                    }
                }
            } else {
                joinButton.visibility = View.GONE
                communityView.isClickable = true;
                communityView.setOnClickListener {
                    val communityName = community["name"]
                    val destination = community["destination"]

                    val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                    intent.putExtra("communityName", communityName)
                    intent.putExtra("destination", destination)
                    intent.putExtra("community_id", communityId)
                    startActivity(intent)
                }
            }



            joinButton.setOnClickListener {
                ApiRequest.getInstance(requireContext()).joinCommunity(
                    communityId = communityId,
                    onResponse = {
                        joinedCommunityIds.add(communityId)
                        joinButton.text = "Rejointe"
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
