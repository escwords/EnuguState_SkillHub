package com.words.storageapp.client

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.words.storageapp.R
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.databinding.ResultListCardBinding
import com.words.storageapp.domain.FirebaseContract

class ContractListAdapter(val clickListener: ClickListener) :
    ListAdapter<FirebaseContract, ContractListAdapter.ContractViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContractViewHolder(inflater.inflate(R.layout.item_contract_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val contract = getItem(position)
        holder.bind(contract, clickListener)
    }

    class ContractViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val imageIcon = itemView.findViewById<ImageView>(R.id.imageItem)
        private val fName = itemView.findViewById<TextView>(R.id.fullName)
        private val lastName = itemView.findViewById<TextView>(R.id.lNam)
        private val skillView = itemView.findViewById<TextView>(R.id.skillText)
        private val payBtn = itemView.findViewById<MaterialButton>(R.id.payBtn)
        private val rateBtn = itemView.findViewById<MaterialButton>(R.id.btnRate)
        private val removeBtn = itemView.findViewById<ImageView>(R.id.remove)

        fun bind(
            data: FirebaseContract,
            listener: ClickListener
        ) {
            fName.text = "${data.laborerFName}" ///use get string for this fullName
            lastName.text = "${data.laborerLName}"
            skillView.text = data.skill

            payBtn.setOnClickListener {
                listener.payClick(data)
            }

            rateBtn.setOnClickListener {
                listener.rateClick(data)
            }

            removeBtn.setOnClickListener {
                listener.removeClick(data)
            }

            Glide.with(imageIcon.context)
                .load(data.laborerUrl)
                .into(imageIcon)
        }
    }

    class ClickListener(
        val listener: (skills: FirebaseContract) -> Unit,
        val rateListener: (skills: FirebaseContract) -> Unit,
        val removeListener: (skills: FirebaseContract) -> Unit
    ) {
        fun payClick(skill: FirebaseContract) = listener(skill)
        fun rateClick(skill: FirebaseContract) = rateListener(skill)
        fun removeClick(skill: FirebaseContract) = removeListener(skill)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<FirebaseContract>() {
            override fun areItemsTheSame(
                oldItem: FirebaseContract,
                newItem: FirebaseContract
            ): Boolean {
                return oldItem.contractId == newItem.contractId
            }

            override fun areContentsTheSame(
                oldItem: FirebaseContract,
                newItem: FirebaseContract
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}