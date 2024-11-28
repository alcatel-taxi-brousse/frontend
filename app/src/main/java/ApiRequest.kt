import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

class ApiRequest private constructor(context: Context) {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private var requestQueue: RequestQueue
    private var apiUrl: String

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
        private var instance: ApiRequest? = null

        fun getInstance(context: Context?): ApiRequest {
            synchronized(ApiRequest::class) {
                if (instance == null &&context != null) {
                    instance = ApiRequest(context.applicationContext)
                }
            }

            return instance!!
        }
    }

    public fun login(
        login: String,
        password: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/auth",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = login
                params["password"] = password
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    public fun createCommunity(
        communityName: String,
        destination: String,
        description: String,
        visibility: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ){
        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/CreateCommunity",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["communityName"] = communityName
                params["destination"] = destination
                params["description"] = description
                params["visibility"] = visibility
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    fun proposeRide(
        departure: String,
        arrival: String,
        dateTime: Date,
        isRecurrent: Boolean,
        recurrence: String,
        seats: String,
        carModel: String,
        description: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime = dateFormat.format(dateTime)

        val stringRequest = object : StringRequest(
            Method.POST, "$apiUrl/ProposeRide",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["departure"] = departure
                params["arrival"] = arrival
                params["dateTime"] = formattedDateTime
                params["isRecurrent"] = isRecurrent.toString()
                params["recurrence"] = recurrence
                params["seats"] = seats
                params["carModel"] = carModel
                params["description"] = description
                return params
            }
        }

        requestQueue.add(stringRequest)
    }


    public fun getCommunities(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val mockResponse = """
        [
            {
                "name": "Community A",
                "destination": "Destination A",
                "description": "This is the first community.",
                "visibility": "Public"
            },
            {
                "name": "Community B",
                "destination": "Destination B",
                "description": "This is the second community.",
                "visibility": "Private"
            },
            {
                "name": "Community C",
                "destination": "Destination C",
                "description": "This is the third community.",
                "visibility": "Public"
            },
            {
                "name": "Community D",
                "destination": "Destination C",
                "description": "This is the third community.",
                "visibility": "Public"
            }
        ]
    """

        onResponse(mockResponse)/*
        val urlWithParams = "$apiUrl/getCommunities"

        val stringRequest = object : StringRequest(
            Method.GET, urlWithParams,
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }
        ){}*/
       // requestQueue.add(stringRequest)
    }
}