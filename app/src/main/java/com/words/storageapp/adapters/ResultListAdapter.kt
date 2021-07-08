package com.words.storageapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.words.storageapp.R
import com.words.storageapp.database.model.MiniSkillModel
import com.words.storageapp.databinding.ResultListCardBinding

class ResultListAdapter(private val listener: ClickListener) :
    ListAdapter<MiniSkillModel, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //val inflater = LayoutInflater.from(parent.context)
        return SkillsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SkillsViewHolder -> {
                val data = getItem(position)
                holder.bind(data, listener)
            }
        }
    }

    class SkillsViewHolder(private val binding: ResultListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: MiniSkillModel, listener: ClickListener) {
            binding.skills = data
            binding.clickListener = listener

            Glide.with(binding.iconImage.context)
                .load(data.imageUrl)
                .apply(
                    RequestOptions().placeholder(R.drawable.animated_loading)
                        .error(R.drawable.ic_icon_person)
                )
                .into(binding.iconImage)

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SkillsViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return SkillsViewHolder(ResultListCardBinding.inflate(inflater, parent, false))
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