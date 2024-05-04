package com.alcatelcnamisi1.taxibrousse

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        val login = editTextLogin?.text.toString()
        val password = editTextPassword?.text.toString()

        println("Login: $login")
        println("Password: $password")
    }
}