package com.words.storageapp.contract

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.words.storageapp.R
import com.words.storageapp.domain.FirebaseContract
import kotlinx.android.synthetic.main.fragment_payment.*

class PaymentFragment : Fragment() {

    private val contract: FirebaseContract by lazy {
        arguments?.get("Pay") as FirebaseContract
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payment, container, false)
        val acctName = view.findViewById<TextView>(R.id.acctName)
        val acctNumber = view.findViewById<TextView>(R.id.accountNum)
        val cardNumber = view.findViewById<TextInputEditText>(R.id.cardNumberText)
        val cardExpiry = view.findViewById<TextInputEditText>(R.id.expiryCardText)
        val ccv = view.findViewById<TextInputEditText>(R.id.cvvText)
        val amount = view.findViewById<TextInputEditText>(R.id.amountText)
        val closeBtn = view.findViewById<ImageView>(R.id.clsIcon)
        val payBtn = view.findViewById<MaterialButton>(R.id.btnPay)

        contract.accountName?.let { acctName.text = it }
        contract.accountName?.let { acctNumber.text = it }

        closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        payBtn.setOnClickListener {

        }

        return view
    }

}