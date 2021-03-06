package com.words.storageapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseUser
import com.words.storageapp.util.FORMAT
import com.words.storageapp.util.utilities.ItemClickListener

class AllSkillsAdapter(private val listener: ItemClickListener<FirebaseUser>) :
    ListAdapter<FirebaseUser, AllSkillsAdapter.AllSkillsViewModel>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSkillsViewModel {
        val inflater = LayoutInflater.from(parent.context)
        return AllSkillsViewModel(inflater.inflate(R.layout.item_admin_skills, parent, false))
    }

    override fun onBindViewHolder(holder: AllSkillsViewModel, position: Int) {
        val data = getItem(position)
        holder.bind(data, listener)
    }

    class AllSkillsViewModel(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image = itemView.findViewById<ImageView>(R.id.profileIconImage)
        private val firstName = itemView.findViewById<TextView>(R.id.firstName)
        private val lastName = itemView.findViewById<TextView>(R.id.lastName)
        private val skillText = itemView.findViewById<TextView>(R.id.skills)
        private val statusText = itemView.findViewById<TextView>(R.id.statusText)
        private val uid = itemView.findViewById<TextView>(R.id.id)
        private val viewButton = itemView.findViewById<MaterialButton>(R.id.profileBtn)
        private val date = itemView.findViewById<TextView>(R.id.date)
        private val locality = itemView.findViewById<TextView>(R.id.locality)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            skillData: FirebaseUser,
            listener: ItemClickListener<FirebaseUser>
        ) {

            firstName.text = skillData.firstName
            lastName.text = skillData.lastName
            skillText.text = skillData.skill
            statusText.text = skillData.accountStatus
            locality.text = skillData.locality
            uid.text = skillData.id
            val resource = itemView.resources

            Glide.with(image.context)
                .load(skillData.imageUrl)
                .apply(
                    RequestOptions().placeholder(R.drawable.animated_loading)
                        .error(R.drawable.broken_img)
                )
                .into(image)

            date.text = FORMAT.format(skillData.timeStamp).toString()

            viewButton.setOnClickListener {
                listener.onClick(skillData)
            }

            if (skillData.accountStatus != null &&
                skillData.accountStatus == "Active"
            ) {
                statusText.text = skillData.accountStatus
                statusText.background = resource.getDrawable(R.drawable.green_rectangle)
                statusText.text = skillData.accountStatus
            } else if (skillData.accountStatus == "New") {
                statusText.text = skillData.accountStatus
                statusText.background = resource.getDrawable(R.drawable.yello_rectangle)
                statusText.text = skillData.accountStatus
            }
        }
    }

    companion object {

        val diffUtil =
            object : DiffUtil.ItemCallback<FirebaseUser>() {
                override fun areItemsTheSame(
                    oldItem: FirebaseUser,
                    newItem: FirebaseUser
                ): Boolean {
                    return oldItem.skillId == newItem.skillId
                }

                override fun areContentsTheSame(
                    oldItem: FirebaseUser,
                    newItem: FirebaseUser
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}