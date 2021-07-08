package com.words.storageapp.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity

class TwoFragment : Fragment() {

    lateinit var registerFragment: RegisterFragment
    private lateinit var skillsSpinnerCallback: AdapterView.OnItemSelectedListener
    private var _skill: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_two, container, false)
        val nextBtn = view.findViewById<MaterialButton>(R.id.nextBtn)
        val prevBtn = view.findViewById<ImageView>(R.id.prevBtn)
        val skillsSpinner = view.findViewById<Spinner>(R.id.skillsSpinner)
        val skillDesc = view.findViewById<TextInputEditText>(R.id.skillDescText)

        skillsSpinnerCallBack()
        nextBtn.setOnClickListener {
            (activity as MainActivity).registerModel.apply {
                skill = _skill
                skillDescription = skillDesc.text.toString()
            }
            registerFragment.moveToNextPager()
        }

        prevBtn.setOnClickListener {
            registerFragment.moveToPrevPager()
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.skills_array
            , android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            skillsSpinner.adapter = adapter
        }
        skillsSpinner.onItemSelectedListener = skillsSpinnerCallback

        return view
    }

    private fun skillsSpinnerCallBack() {
        skillsSpinnerCallback = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val skillString = parent?.getItemAtPosition(position) as String
                _skill = skillString
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            TwoFragment().apply {
                registerFragment = _registerFragment
            }
    }

}
