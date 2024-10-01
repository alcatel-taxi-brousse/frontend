package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException

class ViewCommunitiesActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_communities)

        listView = findViewById(R.id.listViewCommunities)
        fetchCommunities()
    }

    private fun fetchCommunities() {
        ApiRequest.getInstance(this).getCommunities(
            onResponse = { response ->
                try {
                    val communities = parseCommunityResponse(response)
                    displayCommunities(communities)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing communities", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Error fetching communities: $error", Toast.LENGTH_SHORT).show()
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
            communityMap["description"] = communityJson.getString("description")
            communityMap["visibility"] = communityJson.getString("visibility")

            communityList.add(communityMap)
        }

        return communityList
    }

    private fun displayCommunities(communities: List<HashMap<String, String>>) {
        val adapter = SimpleAdapter(
            this,
            communities,
            R.layout.list_item_community,
            arrayOf("name", "destination", "description", "visibility"),
            intArrayOf(
                R.id.textViewCommunityName,
                R.id.textViewCommunityDestination,
                R.id.textViewCommunityDescription,
                R.id.textViewCommunityVisibility
            )
        )
        listView.adapter = adapter
    }
}
