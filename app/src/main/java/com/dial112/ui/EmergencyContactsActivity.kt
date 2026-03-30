package com.dial112.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dial112.R

class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var cardPolice: CardView
    private lateinit var cardAmbulance: CardView
    private lateinit var cardFire: CardView
    private lateinit var cardWomen: CardView
    private lateinit var btnCallPolice: FrameLayout
    private lateinit var btnCallAmbulance: FrameLayout
    private lateinit var btnCallFire: FrameLayout
    private lateinit var btnCallWomen: FrameLayout
    private lateinit var btnAddContact: TextView
    private lateinit var rvContacts: RecyclerView
    private lateinit var layoutEmptyContacts: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        initializeViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        cardPolice = findViewById(R.id.cardPolice)
        cardAmbulance = findViewById(R.id.cardAmbulance)
        cardFire = findViewById(R.id.cardFire)
        cardWomen = findViewById(R.id.cardWomen)
        btnCallPolice = findViewById(R.id.btnCallPolice)
        btnCallAmbulance = findViewById(R.id.btnCallAmbulance)
        btnCallFire = findViewById(R.id.btnCallFire)
        btnCallWomen = findViewById(R.id.btnCallWomen)
        btnAddContact = findViewById(R.id.btnAddContact)
        rvContacts = findViewById(R.id.rvContacts)
        layoutEmptyContacts = findViewById(R.id.layoutEmptyContacts)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        // Emergency number cards - both card and button can call
        cardPolice.setOnClickListener { callEmergency("112") }
        btnCallPolice.setOnClickListener { callEmergency("112") }

        cardAmbulance.setOnClickListener { callEmergency("115") }
        btnCallAmbulance.setOnClickListener { callEmergency("115") }

        cardFire.setOnClickListener { callEmergency("113") }
        btnCallFire.setOnClickListener { callEmergency("113") }

        cardWomen.setOnClickListener { callEmergency(getString(R.string.number_women)) }
        btnCallWomen.setOnClickListener { callEmergency(getString(R.string.number_women)) }

        // Add personal contact
        btnAddContact.setOnClickListener { addNewContact() }
    }

    private fun setupRecyclerView() {
        rvContacts.layoutManager = LinearLayoutManager(this)
        // Load personal contacts
        loadPersonalContacts()
    }

    private fun loadPersonalContacts() {
        // Fetch personal contacts from database
        // For now, show empty state
        layoutEmptyContacts.visibility = View.VISIBLE
        rvContacts.visibility = View.GONE
    }

    private fun callEmergency(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    private fun addNewContact() {
        Toast.makeText(this, "Add contact dialog coming soon", Toast.LENGTH_SHORT).show()
    }
}
