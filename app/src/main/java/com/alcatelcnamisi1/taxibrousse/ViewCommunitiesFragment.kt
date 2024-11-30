package com.alcatelcnamisi1.taxibrousse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_view_communities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayout = view.findViewById(R.id.linearLayoutCommunities)

        fetchCommunities()
    }

    private fun fetchCommunities() {

        ApiRequest.getInstance(requireContext()).getCommunities(
            onResponse = { response ->
                try {

                    val communities = parseCommunityResponse(response)
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

            communityMap["name"] = communityJson.getString("name")
            communityMap["destination"] = communityJson.getString("destination")

            communityList.add(communityMap)
        }

        return communityList
    }

    private fun displayCommunities(communities: List<HashMap<String, String>>) {
        for (community in communities) {
            val communityView = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_item_community, linearLayout, false)

            val nameTextView: TextView = communityView.findViewById(R.id.textViewCommunityName)
            val destinationTextView: TextView = communityView.findViewById(R.id.textViewCommunityDestination)
            //val arrowButton: ImageButton = communityView.findViewById(R.id.buttonArrow)

            nameTextView.text = community["name"]
            destinationTextView.text = community["destination"]

            communityView.setOnClickListener {
                val communityName = community["name"]
                val destination = community["destination"]

                val intent = Intent(requireContext(), CommunityDetailsActivity::class.java)
                intent.putExtra("communityName", communityName)
                intent.putExtra("destination", destination)
                startActivity(intent)
            }

            linearLayout.addView(communityView)
        }
    }

}
