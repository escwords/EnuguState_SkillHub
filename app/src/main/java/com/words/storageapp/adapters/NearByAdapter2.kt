package com.words.storageapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.databinding.ItemNearbyListBinding


class NearByAdapter2(val clickListener: ClickListener) :
    ListAdapter<MiniSkillModel, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SkillsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SkillsViewHolder -> {
                val data = getItem(position)
                holder.bind(data, clickListener)
            }
        }
    }

    class SkillsViewHolder(private val binding: ItemNearbyListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nearBySkill: MiniSkillModel, clickListener: ClickListener) {
            binding.skill = nearBySkill
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SkillsViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return SkillsViewHolder(ItemNearbyListBinding.inflate(inflater, parent, false))
            }
        }

    }

    class ClickListener(val listener: (skills: MiniSkillModel) -> Unit) {
        fun onClick(skill: MiniSkillModel) = listener(skill)
    }

    companion object {

        val diffUtil = object : DiffUtil.ItemCallback<MiniSkillModel>() {
            override fun areItemsTheSame(
                oldItem: MiniSkillModel,
                newItem: MiniSkillModel
            ): Boolean {
                return oldItem.skillId == oldItem.skillId
            }

            override fun areContentsTheSame(
                oldItem: MiniSkillModel,
                newItem: MiniSkillModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}