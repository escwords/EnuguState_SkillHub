package com.words.storageapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.words.storageapp.domain.AssetComment
import com.words.storageapp.util.FORMAT

private const val ARG_PARAM1 = "param1"

class CommentFragment : Fragment() {

    private var param1: AssetComment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelable<AssetComment>(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_comment, container, false)
        val fName = view.findViewById<TextView>(R.id.creator)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar2)
        val comment = view.findViewById<TextView>(R.id.comment)
        val imageView = view.findViewById<ImageView>(R.id.iconImage)
        val time = view.findViewById<TextView>(R.id.time)

        Glide.with(imageView.context)
            .load(param1?.authorUrl)
            .apply(
                RequestOptions().placeholder(R.drawable.animated_loading)
                    .error(R.drawable.broken_img)
            )
            .into(imageView)

        fName.text = param1?.author
        comment.text = param1?.comment
        ratingBar.rating = param1?.starNum?.toFloat() ?: 3.0F
        time.text = FORMAT.format(param1!!.timeStamp).toString()
        //Image is yet to be uploaded

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: AssetComment) =
            CommentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                }
            }
    }
}