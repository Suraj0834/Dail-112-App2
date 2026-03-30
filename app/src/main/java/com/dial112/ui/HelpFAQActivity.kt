package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.dial112.R

class HelpFAQActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var etSearch: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var cardContactSupport: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_faq)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        etSearch = findViewById(R.id.etSearch)
        ivClearSearch = findViewById(R.id.ivClearSearch)
        cardContactSupport = findViewById(R.id.cardContactSupport)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        // Search functionality
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                ivClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                searchFAQs(query)
            }
        })

        ivClearSearch.setOnClickListener {
            etSearch.setText("")
        }

        // Contact Support
        cardContactSupport.setOnClickListener {
            // Open contact support - could open email or chat
            Toast.makeText(this, "Opening support chat...", Toast.LENGTH_SHORT).show()
            // Intent to open ChatActivity or email
            // startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun searchFAQs(query: String) {
        // Search FAQs by query
        // In a real implementation, this would filter the FAQ list
        if (query.isNotEmpty()) {
            // Filter and show results
        }
    }
}
