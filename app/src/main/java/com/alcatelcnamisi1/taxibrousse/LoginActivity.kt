package com.alcatelcnamisi1.taxibrousse

import ApiRequest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ale.infra.list.ArrayItemList
import com.ale.infra.manager.room.CreateRoomBody
import com.ale.infra.manager.room.Room
import com.ale.infra.rest.listeners.RainbowError
import com.ale.rainbowsdk.Connection
import com.ale.rainbowsdk.RainbowSdk
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.content.Context
import android.content.SharedPreferences



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
        val ttoken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb3VudFJlbmV3ZWQiOjAsIm1heFRva2VuUmVuZXciOjcsInVzZXIiOnsiaWQiOiI2NjQ3NTI5OGM3NjU5MGVkYzAwNTZlMDkiLCJsb2dpbkVtYWlsIjoibWVocmhhcmRAY2ZhaS1mb3JtYXRpb24uZnIifSwiZW52aXJvbm1lbnQiOnsiZW52aXJvbm1lbnROYW1lIjoib2ZmaWNpYWwiLCJlbnZpcm9ubWVudEFwaVVybCI6Imh0dHBzOi8vc2FuZGJveC5vcGVucmFpbmJvdy5jb20ifSwiYXBwIjp7ImlkIjoiYzcyMTljMzA3NGM2MTFlZmE2NjYxYjBiYjljOTAzNzAiLCJuYW1lIjoiTWFlbCBzYW5kYm94In0sImlhdCI6MTczMjg4NzQ2MSwiZXhwIjoxNzM0MTgzNDYxfQ.Kl80uoyBrNS2PjuMkaz5dsdiNSjiij2gHMfQVamIPCUXpU1lTp3ruJEFjktobcL19GArLFF_-CMfJ1NTNV1wp1wXSC_EKdAenJFYRea3yMYkJk_ijxfYRiZCGGrsKVOnFw75dQYZA5y1Hbprd6gmI88i59VkB-0enluAztjwDxKWUlrN2iue1pgWW4eWSd_vufd95EhGtWG2FFC3pXuZodrvoZlK23wLSVVhDobPN7M4UGWNAi6S3BjUqeuYfuGi8HLWy8YEa-ganA_1JrcdmQXHAxjhGLe_AHzEE66NrT0EJWWY9nc5rmXBVOzoP3Dffi4Fl2gOcbYP9W7qnm-0YA"
        RainbowSdk().connection().signInWithToken(
            token = ttoken,
            host = "sandbox.openrainbow.com",
            listener = object : Connection.ISignInListener {
                override fun onSignInSucceeded() {
                    super.onSignInSucceeded()
                    println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                }

                override fun onSignInFailed(
                    errorCode: Connection.ErrorCode,
                    error: RainbowError<Unit>
                ) {
                    super.onSignInFailed(errorCode, error)
                    println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN")
                }
            }
        )

    }

    private fun login() {
        val login = editTextLogin?.text.toString()
        val password = editTextPassword?.text.toString()

        ApiRequest.getInstance(null).login(login, password, { response ->
            println("Response: $response")

            val token = JSONObject(response).getString("token")
            println("Token extrait : $token")

            val sharedPreferences: SharedPreferences =
                this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("auth_token", token)
            editor.apply()

        }, { error ->
            println("Error: $error")
        })

        val intent = Intent(this, HomeActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}