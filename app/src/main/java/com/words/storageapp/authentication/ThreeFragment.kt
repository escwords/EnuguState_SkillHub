package com.words.storageapp.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.words.storageapp.R
import com.words.storageapp.ui.main.MainActivity


class ThreeFragment : Fragment() {

    lateinit var registerFragment: RegisterFragment
    private lateinit var accountSpinnerCallback: AdapterView.OnItemSelectedListener
    private var _accountName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_three, container, false)
        val nextBtn = view.findViewById<MaterialButton>(R.id.nextBtn)
        val prevBtn = view.findViewById<ImageView>(R.id.prevBtn)
        val accountSpinner = view.findViewById<Spinner>(R.id.accountSpin)
        val accountNum = view.findViewById<TextView>(R.id.accountNumText)
        accountSpinnerCallBack()

        nextBtn.setOnClickListener {
            (activity as MainActivity).registerModel.apply {
                accountName = _accountName
                accountNumber = accountNum.text.toString()
            }
            registerFragment.moveToNextPager()
        }

        prevBtn.setOnClickListener {
            registerFragment.moveToPrevPager()
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.bank_array
            , android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSpinner.adapter = adapter
        }
        accountSpinner.onItemSelectedListener = accountSpinnerCallback

        return view
    }

    private fun accountSpinnerCallBack() {
        accountSpinnerCallback = object : AdapterView.OnItemSelectedListener {
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
                _accountName = skillString
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(_registerFragment: RegisterFragment) =
            ThreeFragment().apply {
                registerFragment = _registerFragment
            }
    }
}