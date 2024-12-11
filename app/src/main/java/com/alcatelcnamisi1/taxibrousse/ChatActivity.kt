package com.alcatelcnamisi1.taxibrousse

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val chatMessagesLayout = findViewById<LinearLayout>(R.id.chatMessages)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val backButton = findViewById<ImageButton>(R.id.buttonBack)

        // Listener pour le bouton "Retour"
        backButton.setOnClickListener {
            onBackPressed() // Retour à l'activité précédente
        }

        // Ajouter un listener au bouton d'envoi
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()

            if (messageText.isNotEmpty()) {
                // Ajouter un message de l'utilisateur
                addMessage(chatMessagesLayout, messageText, true)

                // Simuler un message de réponse de l'autre utilisateur
                chatMessagesLayout.postDelayed({
                    addMessage(chatMessagesLayout, "Réponse automatique : $messageText", false)
                }, 1000)

                // Effacer la saisie
                messageInput.text.clear()
            }
        }
    }

    private fun addMessage(chatMessagesLayout: LinearLayout, messageText: String, isUser: Boolean) {
        // Créer une bulle de message
        val messageView = TextView(this)
        messageView.text = messageText
        messageView.textSize = 16f
        messageView.setPadding(16, 8, 16, 8)

        // Appliquer un style selon l'auteur du message
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (isUser) {
            messageView.setBackgroundResource(R.drawable.bubble_user) // Bulle utilisateur
            messageView.setTextColor(Color.WHITE)
            params.gravity = Gravity.END // Aligner à droite
        } else {
            messageView.setBackgroundResource(R.drawable.bubble_other) // Bulle autre
            messageView.setTextColor(Color.BLACK)
            params.gravity = Gravity.START // Aligner à gauche
        }
        params.setMargins(8, 8, 8, 8)
        messageView.layoutParams = params

        // Ajouter la bulle au layout
        chatMessagesLayout.addView(messageView)

        // Faire défiler automatiquement vers le bas
        chatMessagesLayout.post {
            chatMessagesLayout.parent?.requestChildFocus(chatMessagesLayout, messageView)
        }
    }
}
