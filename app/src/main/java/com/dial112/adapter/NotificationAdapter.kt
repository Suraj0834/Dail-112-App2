package com.dial112.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dial112.R

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: Int
)

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onDeleteListener: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val btnClose: ImageButton = itemView.findViewById(R.id.btn_close)

        fun bind(notification: Notification) {
            ivIcon.setImageResource(notification.icon)
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvTime.text = notification.time

            btnClose.setOnClickListener {
                onDeleteListener(notification)
                notifications.remove(notification)
                notifyItemRemoved(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size
}
