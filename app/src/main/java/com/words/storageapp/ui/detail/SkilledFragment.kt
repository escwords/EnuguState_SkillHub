package com.words.storageapp.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.words.storageapp.CommentFragment
import com.words.storageapp.R
import com.words.storageapp.database.AppDatabase
import com.words.storageapp.database.model.RecentSkillModel
import com.words.storageapp.databinding.FragmentDetail2Binding
import com.words.storageapp.domain.*
import com.words.storageapp.ui.main.MainActivity
import com.words.storageapp.ui.search.SearchRepository
import com.words.storageapp.util.InjectorUtil
import com.words.storageapp.util.USERID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SkilledFragment : Fragment() {

    private val skillId: String by lazy {
        arguments?.get(USERID) as String
    }

    @Inject
    lateinit var searchRepository: SearchRepository

    private val detailViewModel: DetailViewModel by viewModels {
        InjectorUtil.provideDetailViewModelFactory(skillId, searchRepository)
    }

    private var mobileNo: String? = null
    private lateinit var recentSkillModel: RecentSkillModel

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var commentCollectionRef: DatabaseReference
    private lateinit var contractDbPath: DatabaseReference

    private lateinit var commentsViewPager: ViewPager2
    private lateinit var fireStore: FirebaseFirestore
    lateinit var localDb: AppDatabase

    private lateinit var hireBtn: MaterialButton
    private lateinit var callBtn: MaterialButton
    private lateinit var ratingNum: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var commentsCard: MaterialCardView

    private var currentPager = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference
        commentCollectionRef = databaseReference.child("skills")
            .child(skillId.trimIndent()).child("comments")
        contractDbPath = Firebase.database.reference.child("contracts")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).daggerAppLevelComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDetail2Binding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.detailModel = detailViewModel
        commentsCard = binding.commentCard

        callBtn = binding.callBtn
        hireBtn = binding.hireBtn
        commentsViewPager = binding.commentPager
        ratingNum = binding.ratingNum
        ratingBar = binding.ratingStar
        localDb = (activity as MainActivity).db //local

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        detailViewModel.skillData.observe(viewLifecycleOwner, Observer { skill ->

            val comments = skill.comments.toDomainComment()
            if (comments.isEmpty()) {
                hideCommentCard()
                showSnackBar { "No Comments" }
            }
            Timber.i("result: $comments")

            val commentAdapter = CommentPager(this, comments.distinctBy { it.authorId })
            commentsViewPager.adapter = commentAdapter

            val handle = Handler()
            val update = Runnable {
                if (currentPager == comments.size) {
                    currentPager = 0
                }
                commentsViewPager.setCurrentItem(currentPager++, true)
            }

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    handle.post(update)
                }
            }, 2000, 5000)

        })

        detailViewModel.detailData.observe(viewLifecycleOwner, Observer { detailData ->
            if (detailData != null) {
                mobileNo = detailData.mobile
                recentSkillModel = detailData.run {
                    RecentSkillModel(
                        id = id,
                        imgUrl = imageUrl,
                        firstName = firstName,
                        lastName = lastName,
                        skill = skill
                    )
                }
            }
        })

        callBtn.setOnClickListener {
            mobileNo?.let {
                callDialog(it)
                //detailViewModel.insertInRecentTable(recentSkillModel)
            }
        }

        hireBtn.setOnClickListener {
            val user = firebaseAuth.currentUser?.uid
            if (user != null) {
                hireDialog(user)
            } else {
                notRegistered()
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        commentCollectionRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNull {
                        it.getValue(FirebaseComment::class.java)
                    }.also { commentList ->
                        CoroutineScope(Dispatchers.IO).launch {
                            Timber.i("laborers: $commentList")
                            localDb.commentDao().insertAll(commentList.toCommentsDb())
                        }
                    }
                }
            })
    }

    private fun hireLaborer(uid: String) {

        detailViewModel.detailData.observe(viewLifecycleOwner,
            Observer { detailData ->

                val documentRef = contractDbPath.push()

                val contract = FirebaseContract(
                    accountName = detailData.accountName,
                    accountNumber = detailData.accountNumber,
                    clientId = uid,
                    contractId = documentRef.key,
                    laborerId = skillId,
                    laborerUrl = detailData.imageUrl,
                    skill = detailData.skill,
                    laborerFName = detailData.firstName,
                    laborerLName = detailData.lastName
                )

                documentRef.setValue(contract)
                    .addOnSuccessListener {
                        lifecycleScope.launch {
                            val action = R.id.action_skilledFragment2_to_clientProfileFragment
                            findNavController().navigate(action)
                        }
                    }.addOnFailureListener {
                        lifecycleScope.launch {
                            //unable to hire Laborer
                            Toast.makeText(
                                requireContext(),
                                "No internet Access",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            })
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber") //or use Uri.fromParts()
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun moveToLogin() {
        val action = R.id.action_skilledFragment2_to_loginFragment
        findNavController().navigate(action)
    }


    private fun showSnackBar(call: () -> String) {
        Snackbar.make(
            requireActivity().findViewById<ConstraintLayout>(R.id.detalScreen)
            , call.invoke(), Snackbar.LENGTH_LONG
        ).show()
    }

    private fun hireDialog(user: String) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("You're About to Hire Laborer.")
            setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
                hireLaborer(user)
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun notRegistered() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Login to HireLaborer.")
            setMessage("You can only hire laborer when you are logged in.")
            setPositiveButton("Login") { dialog, _ ->
                dialog.dismiss()
                moveToLogin()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    fun hideCommentCard() {
        commentsCard.visibility = View.GONE
    }

    private fun callDialog(number: String) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Call Skilled Laborer.")
            setMessage("Your are about to leave the app to call laborer.")

            setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                dialPhoneNumber(number)
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }

    }

    class CommentPager(
        val fragment: Fragment,
        private val comments: List<AssetComment>
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount() = comments.size

        override fun createFragment(position: Int): Fragment =
            CommentFragment.newInstance(comments[position])

    }
}