package com.dial112.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dial112.R

data class FAQ(
    val id: String,
    val question: String,
    val answer: String
)

class FAQAdapter(private val faqs: List<FAQ>) : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
        private val ivExpand: ImageView = itemView.findViewById(R.id.ivExpand)

        fun bind(faq: FAQ) {
            tvQuestion.text = faq.question
            tvAnswer.text = faq.answer

            var isExpanded = false
            itemView.setOnClickListener {
                isExpanded = !isExpanded
                tvAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
                ivExpand.rotation = if (isExpanded) 180f else 0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        holder.bind(faqs[position])
    }

    override fun getItemCount(): Int = faqs.size
}
