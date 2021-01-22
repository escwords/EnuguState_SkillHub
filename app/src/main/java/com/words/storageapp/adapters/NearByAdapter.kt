package com.words.storageapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.words.storageapp.databinding.ItemNearbyListBinding
import com.words.storageapp.domain.NearBySkill

class NearByAdapter(val query: Query) : FirestoreAdapter<NearByAdapter.NearByViewHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearByViewHolder {
        return NearByViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NearByViewHolder, position: Int) {
        holder.bind(getSnapshot(position))
    }

    class NearByViewHolder(val binding: ItemNearbyListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(snapshot: DocumentSnapshot) {
        }

        companion object {
            fun from(parent: ViewGroup): NearByViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return NearByViewHolder(ItemNearbyListBinding.inflate(inflater, parent, false))
            }
        }
    }
}
