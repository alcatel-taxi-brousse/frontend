package com.alcatelcnamisi1.taxibrousse

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ale.infra.list.ArrayItemList
import com.ale.infra.list.IItemListChangeListener
import com.ale.infra.manager.IMMessage
import com.ale.infra.proxy.conversation.IRainbowConversation
import com.ale.rainbowsdk.RainbowSdk
import com.bumptech.glide.Glide
import org.jivesoftware.smackx.chatstates.ChatState


class ChatActivity : AppCompatActivity() {

    lateinit var m_conversation: IRainbowConversation
    lateinit var messages: ArrayItemList<IMMessage>
    private lateinit var scrollView: ScrollView
    private lateinit var editText: EditText

    private val typingHandler = Handler(Looper.getMainLooper())
    private var isTyping = false

    private val m_changeListener = IItemListChangeListener {
    runOnUiThread {
        val chatMessagesLayout = findViewById<LinearLayout>(R.id.chatMessages)
        for (message in messages.items) {
            addMessage(chatMessagesLayout, formatMessage(message), isFromUser(message))
        }
    }
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val chatMessagesLayout = findViewById<LinearLayout>(R.id.chatMessages)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val backButton = findViewById<ImageButton>(R.id.buttonBack)

        scrollView = findViewById(R.id.chatScroll)

        val communityName = intent.getStringExtra("community_name")
        // Initialiser la conversation
        val room = RainbowSdk.instance().bubbles().findBubblesByName("azeazeaze")[0]
        println("Room connected: $room")
        m_conversation = RainbowSdk.instance().im().getConversationFromRoom(room)

        messages = m_conversation.messages
        m_conversation.messages.registerChangeListener(m_changeListener)

        addMessage(chatMessagesLayout, room.name, false)
        for (message in messages.items) {
            addMessage(chatMessagesLayout, formatMessage(message), isFromUser(message))
        }


        // Listener pour le bouton "Retour"
        backButton.setOnClickListener {
            onBackPressed() // Retour à l'activité précédente
        }

        // Ajouter un listener au bouton d'envoi
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString()
            RainbowSdk.instance().im().sendMessageToConversation(m_conversation, messageText)
            if (messageText.isNotEmpty()) {
                // Ajouter un message de l'utilisateur
                addMessage(chatMessagesLayout, messageText, true)

                // Effacer la saisie
                messageInput.text.clear()
                RainbowSdk.instance().im().sendIsTyping(m_conversation, ChatState.inactive)
            }
        }

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val lastChild = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = lastChild.bottom - (scrollView.height + scrollView.scrollY)
            if (diff == 0) {
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    RainbowSdk.instance().im().markMessageFromConversationAsRead(m_conversation, messages.items[messages.count - 1])
                }
            }
        }

        messageInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isTyping) {
                    isTyping = true
                    RainbowSdk.instance().im().sendIsTyping(m_conversation, ChatState.composing)
                }
                typingHandler.removeCallbacksAndMessages(null)
                typingHandler.postDelayed({
                    isTyping = false
                    RainbowSdk.instance().im().sendIsTyping(m_conversation, ChatState.inactive)
                }, 2000)
            }
        })
    }

    private fun formatMessage(message: IMMessage): String {
        val contact = RainbowSdk.instance().contacts().getContactFromJid(message.contactJid)
        return "${contact?.firstName.toString()}:\n\t${message.messageContent}"
    }

    private fun isFromUser(message: IMMessage): Boolean {
        return message.contactJid == RainbowSdk.instance().user().getConnectedUser().jid
    }

    private fun addMessage(chatMessagesLayout: LinearLayout, messageText: String, isUser: Boolean) {
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

    override fun onDestroy() {
        super.onDestroy()
        m_conversation.messages.unregisterChangeListener(m_changeListener)
    }


}
