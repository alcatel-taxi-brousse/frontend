import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.lang.ref.WeakReference
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
            Method.POST, "$apiUrl/login",
            Response.Listener { response ->
                onResponse(response)
            },
            Response.ErrorListener { error ->
                onError("${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["login"] = login
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
}