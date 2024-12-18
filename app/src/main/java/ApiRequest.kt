import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Properties
import android.content.SharedPreferences
import org.json.JSONObject

class ApiRequest private constructor(context: Context) {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private var requestQueue: RequestQueue
    private var apiUrl: String
    private var token: String = ""

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
                token = JSONObject(response).getString("token")
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
            Method.POST, "$apiUrl/bubbles",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = communityName
                //params["destination"] = destination
                params["description"] = description
                //params["visibility"] = visibility
                //params["withHistory"] = "false"

                println("Params envoyés : $params")
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers["Authorization"] = "Bearer $token"

                println("Headers envoyés : $headers")
                return headers
            }
        }
        requestQueue.add(stringRequest)
    }

    public fun getCommunity(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ){
        val stringRequest = object : StringRequest(
            Method.GET, "$apiUrl/bubbles",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers["Authorization"] = "Bearer $token"

                println("Headers envoyés : $headers")
                return headers
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

    fun getRides(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dummyRidesJson = """
        [
            {
                "departure": "Paris",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)}",
                "seatsAvailable": "1",
                "recurrence": "Weekly",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            },
            {
                "departure": "Paris",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 1) }.time)}",
                "seatsAvailable": "3",
                "recurrence": "Weekly",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."

            },
            {
                "departure": "Marseille",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 2) }.time)}",
                "seatsAvailable": "2",
                "recurrence": "Daily",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."

            },
            {
                "departure": "Toulouse",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 3) }.time)}",
                "seatsAvailable": "4",
                "recurrence": "Weekly",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            },
            {
                "departure": "Strasbourg",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 3) }.time)}",
                "seatsAvailable": "1",
                "recurrence": "Monthly",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            },
            {
                "departure": "Tours",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 3) }.time)}",
                "seatsAvailable": "0",
                "recurrence": "Daily",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            },
            {
                "departure": "Rennes",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 3) }.time)}",
                "seatsAvailable": "3",
                "recurrence": "Daily",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            }
        ]
    """


        onResponse(dummyRidesJson)
        /*
        val urlWithParams = "$apiUrl/getRides"

        val stringRequest = object : StringRequest(
            Method.GET, urlWithParams,
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }
        ) {}

        requestQueue.add(stringRequest)*/
    }


    fun getMyRides(
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dummyRidesJson = """
        [
            {
                "departure": "Strasbourg",
                "arrival": "Paris",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 1) }.time)}",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."
            },
            {
                "departure": "Paris",
                "arrival": "Strasbourg",
                "date": "${dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, 5) }.time)}",
                "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed leo diam, auctor non faucibus quis, accumsan eu nunc. Quisque sed eleifend quam, a dignissim nunc. Phasellus laoreet lacus in augue rhoncus, quis euismod arcu sollicitudin. Aenean sed nunc nec leo placerat gravida id et ex. Integer lectus massa, feugiat sed hendrerit eu, ultricies consectetur ipsum. Pellentesque dictum, lacus molestie pretium fermentum, est erat viverra velit, sed placerat nisl libero ullamcorper ex. Nullam eu quam mi. Cras at tortor sagittis, vehicula ante nec, congue velit. Duis id justo eu velit rutrum feugiat at vitae sem. Proin interdum felis eget ex venenatis sodales. Suspendisse sagittis nibh at dolor ultrices commodo. Curabitur facilisis eros nec nunc vulputate gravida."

            }
        ]
    """


        onResponse(dummyRidesJson)
        /*
        val urlWithParams = "$apiUrl/getRides"

        val stringRequest = object : StringRequest(
            Method.GET, urlWithParams,
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }
        ) {}

        requestQueue.add(stringRequest)*/
    }


}