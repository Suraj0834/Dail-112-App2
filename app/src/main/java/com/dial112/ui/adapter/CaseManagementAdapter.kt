package com.dial112.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dial112.R
import com.dial112.data.model.response.Case
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class CaseManagementAdapter(
    private val onCaseClick: (Case) -> Unit,
    private val onAssignClick: (Case) -> Unit,
    private val currentUserId: String?
) : ListAdapter<Case, CaseManagementAdapter.CaseViewHolder>(CaseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_case_management, parent, false)
        return CaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val tvCaseId: TextView = itemView.findViewById(R.id.tvCaseId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCaseTitle: TextView = itemView.findViewById(R.id.tvCaseTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val layoutOfficerInfo: LinearLayout = itemView.findViewById(R.id.layoutOfficerInfo)
        private val ivOfficerAvatar: CircleImageView = itemView.findViewById(R.id.ivOfficerAvatar)
        private val tvOfficerName: TextView = itemView.findViewById(R.id.tvOfficerName)
        private val btnAssignToMe: Button = itemView.findViewById(R.id.btnAssignToMe)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)

        fun bind(case: Case) {
            // Case ID
            tvCaseId.text = "#${case.id}"

            // Case Title
            tvCaseTitle.text = case.title ?: "Case Details"

            // Location
            tvLocation.text = case.location ?: "Unknown Location"

            // Date
            tvDate.text = formatTimeAgo(case.createdAt ?: "")

            // Category Icon
            setCategoryIcon(case.category)

            // Status Badge
            setStatusBadge(case.status, case.assignedTo)

            // Priority
            setPriority(case.priority)

            // Show/Hide Officer Info or Assign Button
            if (case.assignedTo != null) {
                layoutOfficerInfo.visibility = View.VISIBLE
                btnAssignToMe.visibility = View.GONE
                tvOfficerName.text = case.assignedOfficer?.name ?: "Officer Assigned"
            } else {
                layoutOfficerInfo.visibility = View.GONE
                btnAssignToMe.visibility = View.VISIBLE
            }

            // Click listeners
            itemView.setOnClickListener {
                onCaseClick(case)
            }

            btnAssignToMe.setOnClickListener {
                onAssignClick(case)
            }
        }

        private fun setCategoryIcon(category: String?) {
            val iconRes = when (category?.lowercase()) {
                "theft" -> R.drawable.ic_case
                "assault" -> R.drawable.ic_priority_high
                "accident" -> R.drawable.ic_location
                else -> R.drawable.ic_case
            }
            ivCategoryIcon.setImageResource(iconRes)
        }

        private fun setStatusBadge(status: String?, assignedTo: String?) {
            val context = itemView.context

            when {
                assignedTo == null -> {
                    tvStatus.text = context.getString(R.string.unassigned)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending)
                }
                status?.lowercase() == "active" || status?.lowercase() == "assigned" -> {
                    tvStatus.text = context.getString(R.string.case_status_active)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_badge_active)
                }
                status?.lowercase() == "resolved" -> {
                    tvStatus.text = context.getString(R.string.case_status_resolved)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_badge_resolved)
                }
                status?.lowercase() == "pending" || status?.lowercase() == "new" -> {
                    tvStatus.text = context.getString(R.string.case_new)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending)
                }
                else -> {
                    tvStatus.text = status ?: "Unknown"
                    tvStatus.setBackgroundResource(R.drawable.bg_status_badge_pending)
                }
            }
        }

        private fun setPriority(priority: String?) {
            val context = itemView.context

            when (priority?.lowercase()) {
                "high" -> {
                    tvPriority.text = context.getString(R.string.high_priority)
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.error_red))
                    tvPriority.visibility = View.VISIBLE
                }
                "medium" -> {
                    tvPriority.text = context.getString(R.string.medium_priority)
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.warning_yellow))
                    tvPriority.visibility = View.VISIBLE
                }
                "low" -> {
                    tvPriority.text = context.getString(R.string.low_priority)
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.success_green))
                    tvPriority.visibility = View.VISIBLE
                }
                else -> {
                    tvPriority.visibility = View.GONE
                }
            }
        }

        private fun formatTimeAgo(dateString: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(dateString)
                val now = Date()
                val diff = now.time - (date?.time ?: 0)

                val minutes = diff / (1000 * 60)
                val hours = diff / (1000 * 60 * 60)
                val days = diff / (1000 * 60 * 60 * 24)

                when {
                    minutes < 1 -> "Just now"
                    minutes < 60 -> "$minutes min ago"
                    hours < 24 -> "$hours hours ago"
                    days < 7 -> "$days days ago"
                    else -> {
                        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        displayFormat.format(date ?: Date())
                    }
                }
            } catch (e: Exception) {
                "Recently"
            }
        }
    }

    class CaseDiffCallback : DiffUtil.ItemCallback<Case>() {
        override fun areItemsTheSame(oldItem: Case, newItem: Case): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Case, newItem: Case): Boolean {
            return oldItem == newItem
        }
    }
}
