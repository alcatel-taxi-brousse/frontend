package com.alcatelcnamisi1.taxibrousse

import ApiRequest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ale.infra.rest.listeners.RainbowError
import com.ale.rainbowsdk.Connection
import com.ale.rainbowsdk.RainbowSdk
import org.json.JSONObject
import android.widget.Toast


class LoginActivity : AppCompatActivity() {

    private var editTextLogin: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextLogin = findViewById<EditText>(R.id.editTextLogin)
        editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        buttonLogin = findViewById<Button>(R.id.buttonLogin)

        buttonLogin?.setOnClickListener {
            login()
        }
    }

    private fun login() {

        buttonLogin?.setEnabled(false)
        buttonLogin?.setText("Chargement...")

        val login = editTextLogin?.text.toString()
        val password = editTextPassword?.text.toString()

        ApiRequest.getInstance(null).login(login, password, { response ->
            println("Response pour le login: $response")

            val token = JSONObject(response).getString("token")

            println("Token extrait : $token")

            RainbowSdk().connection().signInWithToken(
                token = token,
                host = "sandbox.openrainbow.com",
                listener = object : Connection.ISignInListener {
                    override fun onSignInSucceeded() {
                        super.onSignInSucceeded()
                        println("SDK Successfully initialized")
                        openHome()
                    }

                    override fun onSignInFailed(
                        errorCode: Connection.ErrorCode,
                        error: RainbowError<Unit>
                    ) {
                        super.onSignInFailed(errorCode, error)
                        println("SDK initialization error")
                        buttonLogin?.setEnabled(true);
                        buttonLogin?.setText("Se connecter")
                    }
                }
            )

        }, { error ->
            println("Error sur le login: $error")
            Toast.makeText(
                this,
                "Login ou mot de passe invalide. Veuillez réessayer.",
                Toast.LENGTH_SHORT
            ).show()
            buttonLogin?.setEnabled(true);
            buttonLogin?.setText("Se connecter")

            editTextPassword?.setText("");

        })
    }

    fun openHome(){
        val intent = Intent(this, HomeActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}