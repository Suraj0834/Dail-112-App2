package com.dial112.ui

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AIRepository
import com.dial112.databinding.ActivityChatBinding
import com.dial112.model.ChatMessage
import com.dial112.adapter.ChatAdapter
import com.dial112.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var aiRepository: AIRepository
    private lateinit var tokenManager: TokenManager

    private val messages = mutableListOf<ChatMessage>()
    private val sessionId = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        aiRepository = AIRepository(ApiClient.apiService, tokenManager)

        setupUI()
        setupListeners()
        sendWelcomeMessage()
    }

    private fun setupUI() {
        // Setup back button
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messages)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        // Send button
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Enter key to send
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        // Voice input
        binding.btnVoice.setOnClickListener {
            // Implement voice input
        }

        // Attach button
        binding.btnAttach.setOnClickListener {
            // Implement file attachment
        }

        // Quick reply chips
        setupQuickReplies()
    }

    private fun setupQuickReplies() {
        binding.chipReportCrime.setOnClickListener {
            binding.etMessage.setText("I want to report a crime")
            sendMessage()
        }

        binding.chipFindStation.setOnClickListener {
            binding.etMessage.setText("Where is the nearest police station?")
            sendMessage()
        }

        binding.chipLegalHelp.setOnClickListener {
            binding.etMessage.setText("I need legal help")
            sendMessage()
        }

        binding.chipDocuments.setOnClickListener {
            binding.etMessage.setText("What documents do I need?")
            sendMessage()
        }
    }

    private fun sendWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = "Hello! I'm your AI Assistant. I can help you with:\n\n" +
                    "• Filing complaints\n" +
                    "• Finding nearby police stations\n" +
                    "• Legal information\n" +
                    "• Document requirements\n\n" +
                    "How can I help you today?",
            isFromUser = false,
            timestamp = getCurrentTime()
        )
        addMessage(welcomeMessage)
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // Add user message
        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = text,
            isFromUser = true,
            timestamp = getCurrentTime()
        )
        addMessage(userMessage)

        // Clear input
        binding.etMessage.text?.clear()

        // Simulate AI response
        simulateAIResponse(text)
    }

    private fun simulateAIResponse(userMessage: String) {
        // Call real AI backend
        lifecycleScope.launch {
            when (val result = aiRepository.chatWithAI(userMessage, sessionId)) {
                is Resource.Success -> {
                    val response = result.data!!.response
                    val aiMessage = ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        text = response,
                        isFromUser = false,
                        timestamp = getCurrentTime()
                    )
                    addMessage(aiMessage)
                }
                is Resource.Error -> {
                    val errorMessage = ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        text = "Sorry, I'm having trouble connecting. Please try again.",
                        isFromUser = false,
                        timestamp = getCurrentTime()
                    )
                    addMessage(errorMessage)
                }
                else -> {}
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
