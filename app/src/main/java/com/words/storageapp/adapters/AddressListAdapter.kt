package com.words.storageapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.R
import java.util.ArrayList

class AddressListAdapter(
    private val addresses: ArrayList<String>,
    val clickListener: AddressClickListener
) : RecyclerView.Adapter<AddressListAdapter.AddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AddressViewHolder(inflater.inflate(R.layout.address_list_item, parent, false))
    }

    override fun getItemCount(): Int = addresses.size

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.bind(address, clickListener)
    }

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressView: TextView = itemView.findViewById(R.id.addressText)
        fun bind(address: String?, clickListener: AddressClickListener) {
            addressView.text = address
            addressView.setOnClickListener { clickListener.onClick(address) }
        }
    }
}

class AddressClickListener(val clickListener: (address: String?) -> Unit) {
    fun onClick(address: String?) = clickListener(address)
}