import android.content.Context
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Properties
import org.json.JSONObject
import java.util.TimeZone


class ApiRequest<JSONException> private constructor(context: Context) {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private var requestQueue: RequestQueue
    private var apiUrl: String
    private var token: String = ""
    private var user_id: String = ""

    init {
        val properties = Properties()
        val assetManager = context.assets
        val inputStream = assetManager.open("settings.properties")
        properties.load(inputStream)
        apiUrl = properties.getProperty("api.url")
        inputStream.close()

        requestQueue = Volley.newRequestQueue(contextRef.get())
    }

    companion object {
        private var instance: ApiRequest<Any?>? = null

        fun getInstance(context: Context?): ApiRequest<Any?> {
            synchronized(ApiRequest::class) {
                if (instance == null && context != null) {
                    instance = ApiRequest(context.applicationContext)
                }
            }

            return instance!!
        }
    }

    fun login(
        login: String,
        password: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val stringRequest =
                object :
                        StringRequest(
                                Method.POST,
                                "$apiUrl/auth",
                                Response.Listener { response ->
                                    onResponse(response)
                                    token = JSONObject(response).getString("token")

                                    try {
                                        val jsonResponse = JSONObject(response)
                                        val loggedInUser = jsonResponse.getJSONObject("loggedInUser")
                                        //println("join ride fragment - "+loggedInUser.optString("id", "ID non trouvé"))
                                        this.user_id  = loggedInUser.optString("id", "ID non trouvé")
                                    } catch (e :Error) {
                                        println("Erreur lors de l'extraction de l'ID : " + e.message)
                                    }
                                },
                                Response.ErrorListener { error -> onError("${error.message}") }
                        ) {
                    override fun getParams(): Map<String, String> {
                        val params = HashMap<String, String>()
                        params["email"] = login
                        params["password"] = password
                        return params
                    }
                }

        requestQueue.add(stringRequest)
    }

    fun createCommunity(
        communityName: String,
        destination: String,
        withHistory: String,
        description: String,
        private: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        //println("Create community entered")

        val jsonBody = JSONObject()
        try {
            jsonBody.put("name", communityName)
            jsonBody.put("destination", destination)
            jsonBody.put("withHistory", false)
            jsonBody.put("description", description)
            jsonBody.put("private", false)
        } catch (e: Exception) {
            onError("Erreur lors de la création du JSON : ${e.message}")
            return
        }

        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/communities",
            Response.Listener { response ->
                //println("Réponse de l'API : $response")
                val jsonObject = JSONObject(response) // Convertir la réponse JSON en objet JSONObject
                val id = jsonObject.optString("id", "Default ID") // Extraire la clé "id"

                joinCommunity(
                    communityId = id,
                    onResponse = {
                        onResponse(response)
                    },
                    onError = { error ->
                        onResponse(response);
                    }
                )
            },
            Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                if (networkResponse != null) {
                    val statusCode = networkResponse.statusCode
                    val data = String(networkResponse.data ?: ByteArray(0), Charsets.UTF_8)
                    //println("Erreur réseau - Code : $statusCode, Message : $data")
                    onError("Erreur : $statusCode -> $data")
                } else {
                    //println("Erreur inconnue : ${error.message}")
                    onError("Erreur inconnue : ${error.message}")
                }
            }
        ) {
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                //println("Headers envoyés : $headers")
                return headers
            }
        }

        //println("Requête envoyée avec le corps : ${jsonBody.toString(2)}")
        requestQueue.add(stringRequest)
    }

    fun getCommunity(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (token.isNullOrBlank()) {
            onError("token est invalide ou null.")
            return
        }

        val stringRequest = object : StringRequest(
            Method.GET, "$apiUrl/communities/app",
            Response.Listener { response ->
                try {
                    // Parse la réponse JSON
                    val originalArray = JSONArray(response)
                    val simplifiedArray = JSONArray()

                    for (i in 0 until originalArray.length()) {
                        val community = originalArray.getJSONObject(i)

                        // Récupérer les utilisateurs
                        val usersArray = community.optJSONArray("users")
                        val userIds = mutableListOf<String>()

                        usersArray?.let {
                            for (j in 0 until it.length()) {
                                val user = it.getJSONObject(j)
                                userIds.add(user.optString("userId"))
                            }
                        }

                        var isInCommunity = "false";

                        val userIdsString = userIds.joinToString(";")
                        println("API REQUST TEST - UsersIDs : " + userIdsString)

                        val usersIdList = userIdsString.split(";")

                        if(usersIdList!=null){
                            if (getActiveUserId() in usersIdList){
                                isInCommunity = "true"
                            }
                        }

                        // Ajouter les données simplifiées de la communauté
                        val simplifiedCommunity = JSONObject()
                        simplifiedCommunity.put("community_id", community.optString("id"))
                        simplifiedCommunity.put("name", community.optString("name"))
                        simplifiedCommunity.put("destination", community.optString("destination", "Unknown"))
                        simplifiedCommunity.put("description", community.optString("description", "No description available"))
                        simplifiedCommunity.put("visibility", community.optString("visibility", "Public"))
                        simplifiedCommunity.put("currentUserInCommunity", isInCommunity)

                        simplifiedArray.put(simplifiedCommunity)
                    }

                    // Format JSON final
                    val simplifiedResponse = simplifiedArray.toString(4) // JSON formaté
                    onResponse(simplifiedResponse)
                } catch (e: Exception) {
                    onError("Erreur lors de la transformation de la réponse : ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                onError("Erreur réseau : ${error.message}")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    fun getTrips(
        communityId: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val stringRequest = object : StringRequest(
            Method.GET, "$apiUrl/communities/$communityId/trips",
            Response.Listener { response ->
                try {
                    val originalArray = JSONArray(response)
                    val simplifiedArray = JSONArray()

                    for (i in 0 until originalArray.length()) {
                        val community = originalArray.getJSONObject(i)

                        val nbSeatsCar = community.optInt("nb_seats_car", 0)

                        val usersArray = community.optJSONArray("users") ?: JSONArray()
                        var totalPeople = 0

                        var User_ids = ""

                        for (j in 0 until usersArray.length()) {
                            val userJson = usersArray.getJSONObject(j)

                            val userTripEntity = userJson.optJSONObject("UserTripEntity")
                            if (userTripEntity != null) {
                                User_ids += userJson.optString("user_id")
                                User_ids += ";"
                                User_ids += userTripEntity.optInt("nb_people", 0)
                                User_ids += ";"

                                totalPeople += userTripEntity.optInt("nb_people", 0)
                            }
                        }

                        val seatsAvailable = nbSeatsCar - totalPeople

                        val simplifiedCommunity = JSONObject()
                        simplifiedCommunity.put("departure", community.optString("start_location"))
                        simplifiedCommunity.put("date", formatDate(community.optString("date")))
                        simplifiedCommunity.put("seatsAvailable", seatsAvailable.toString()) // Résultat calculé
                        simplifiedCommunity.put("recurrence", community.optString("frequence", "No frequency available"))
                        simplifiedCommunity.put("description", community.optString("description", "Public"))
                        simplifiedCommunity.put("trip_id", community.optString("trip_id"))

                        simplifiedCommunity.put("users_id", User_ids);

                        simplifiedArray.put(simplifiedCommunity)
                    }

                    val simplifiedResponse = simplifiedArray.toString(4) // JSON formaté
                    onResponse(simplifiedResponse)
                } catch (e: Exception) {
                    onError("Erreur lors de la transformation de la réponse : ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                onError("Erreur réseau : ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    fun createTrip(
        communityId: String,
        start_location: String,
        date: String,
        frequency: String,
        nb_seats_car: Int,
        description: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (communityId.isNullOrBlank()) {
            onError("communityId est invalide ou null.")
            return
        }

        val jsonBody = JSONObject()
        try {
            jsonBody.put("start_location", start_location)
            jsonBody.put("date", date) // Format ISO, comme dans ton exemple
            jsonBody.put("frequence", frequency)
            jsonBody.put("nb_seats_car", nb_seats_car)
            jsonBody.put("description", description)
        } catch (e: Exception) {
            onError("Erreur lors de la création du JSON : ${e.message}")
            return
        }

        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/communities/$communityId/trips",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
                //println("ERREUR DE OUF : ${error.cause}")
            }
        ) {
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                //println("Headers envoyés : $headers")
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }


    fun joinTrip(
        communityId: String,
        tripId: String,
        nbPeople: Int,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        //println("$apiUrl/communities/$communityId/trips/$tripId/join")

        println("API REQUEST - NB PEOPLE : "+nbPeople)

        val jsonBody = JSONObject()
        try {
            jsonBody.put("nbPeople", nbPeople)
        } catch (e: Exception) {
            onError("Erreur lors de la création du JSON : ${e.message}")
            return
        }

        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/communities/$communityId/trips/$tripId/join",
            Response.Listener { response ->
                //println("Join Trip réussi")
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
                //println("Erreur rencontrée : ${error.cause}")
            }
        ) {
            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                //println("Headers envoyés : $headers")
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    fun getMyTrips(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ){
        val stringRequest = object : StringRequest(
            Method.GET, "$apiUrl/trips/me",
            Response.Listener { response ->

                println("response de my community :" + response);

                try {

                    //println("RETOUR DE LA REPONSE pour les trips : " + response)
                    // Transformation de la réponse pour produire une version simplifiée
                    val originalArray = JSONArray(response)
                    val simplifiedArray = JSONArray()

                    for (i in 0 until originalArray.length()) {
                        val community = originalArray.getJSONObject(i)

                        val communityObject = community.optJSONObject("community") // Récupère l'objet "community"

                        // Création d'un objet simplifié
                        val simplifiedCommunity = JSONObject()
                        simplifiedCommunity.put("departure", community.optString("start_location"))
                        simplifiedCommunity.put("arrival", communityObject?.optString("destination"))
                        simplifiedCommunity.put("date", formatDate(community.optString("date")))
                        simplifiedCommunity.put("description", community.optString("description", "Public"))

                        // Ajout de l'objet simplifié dans le tableau final
                        simplifiedArray.put(simplifiedCommunity)
                    }

                    // Conversion du tableau simplifié en chaîne JSON
                    val simplifiedResponse = simplifiedArray.toString(4) // JSON formaté
                    onResponse(simplifiedResponse)
                    //println("Réponse simplifiée de la communauté : $simplifiedResponse")
                } catch (e: Exception) {
                    onError("Erreur lors de la transformation de la réponse : ${e.message}")
                    //println("Erreur : ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                onError("Erreur réseau : ${error.message}")
                //println("Erreur lors de la récupération des trips : ${error.message}")
            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers["Authorization"] = "Bearer $token"

                //println("Headers envoyés : $headers")
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    fun joinCommunity(
        communityId: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    )

        {
            val urlWithParams = "$apiUrl/communities/$communityId/join"

            val stringRequest = object : StringRequest(
                Method.POST, urlWithParams,
                Response.Listener { response ->
                    onResponse(response)
                },
                Response.ErrorListener { error ->
                    onError("${error.message}")
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()

                    headers["Authorization"] = "Bearer $token"

                    //println("Headers envoyés : $headers")
                    return headers
                }
            }

            requestQueue.add(stringRequest)

    }

    fun searchCommunities(
        searchQuery: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    )
    {
        val urlWithParams = "$apiUrl/communities/search?search=$searchQuery"

        val stringRequest = object : StringRequest(
            Method.GET, urlWithParams,
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(stringRequest)

    }

    fun leaveTrips(
        tripId: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    )
    {
        val urlWithParams = "$apiUrl/communities/{communityId}/trips/$tripId/leave"

        val stringRequest = object : StringRequest(
            Method.DELETE, urlWithParams,
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        requestQueue.add(stringRequest)

    }

    private fun formatDate(isoDate: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = isoFormat.parse(isoDate)

            targetFormat.format(date ?: "")
        } catch (e: Exception) {
            //println("Erreur lors du parsing de la date : ${e.message}")
            isoDate // Renvoie la date brute en cas d'erreur
        }
    }


    fun getActiveUserId():String
    {
        return if(this.user_id!=""){
            this.user_id;
        } else{
            "User ID Vide";
        }
    }

}
