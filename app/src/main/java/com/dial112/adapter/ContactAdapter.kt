package com.dial112.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dial112.R

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val avatar: Int
)

class ContactAdapter(
    private val contacts: MutableList<Contact>,
    private val onCallListener: (Contact) -> Unit,
    private val onDeleteListener: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        private val btnCall: ImageButton = itemView.findViewById(R.id.btn_call)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(contact: Contact) {
            ivAvatar.setImageResource(contact.avatar)
            tvName.text = contact.name
            tvPhone.text = contact.phone

            btnCall.setOnClickListener { onCallListener(contact) }
            btnDelete.setOnClickListener {
                onDeleteListener(contact)
                contacts.remove(contact)
                notifyItemRemoved(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size
}
