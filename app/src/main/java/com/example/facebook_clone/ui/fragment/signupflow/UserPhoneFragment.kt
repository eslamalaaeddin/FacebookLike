package com.example.facebook_clone.ui.fragment.signupflow

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.fragment_mail_user.*
import kotlinx.android.synthetic.main.fragment_mail_user.nextButtonInMailFragment
import kotlinx.android.synthetic.main.fragment_mail_user.signUpWithMobileNumberButton
import kotlinx.android.synthetic.main.fragment_phone_user.*
import kotlinx.android.synthetic.main.fragment_user_name.*

private const val TAG = "UserPhoneFragment"
class UserPhoneFragment : Fragment(R.layout.fragment_phone_user) {
    private val args : UserPhoneFragmentArgs by navArgs()
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var gender: String
    private lateinit var phone:String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstName = args.firstName
        lastName = args.lastName
        day = args.day
        month = args.month
        year = args.year
        gender = args.gender

        Log.i(TAG, "ISLAM onViewCreated: $args")

        nextButtonInPhoneFragment.setOnClickListener {
            phone = phoneNumberTextInputInPhoneNumberFragment.editText?.text.toString()
            validateUserInputAndNavigateToPasswordFragment(phone)
        }

        signUpWithEmailButton.setOnClickListener{
            navigateToUserPhoneFragment()
        }
        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    private fun navigateToPasswordFragment(firstName:String, lastName:String, day:String, month:String,year:String, gender: String, email:String ,phone: String) {
        val action =
            UserPhoneFragmentDirections.actionUserPhoneFragmentToNewPasswordFragment(firstName, lastName, day, month, year, gender,"", phone)
        findNavController().navigate(action)
    }

    private fun navigateToUserPhoneFragment(){
        val action =
            UserPhoneFragmentDirections.actionUserPhoneFragmentToUserMailFragment(firstName, lastName, day, month, year, gender)
        findNavController().navigate(action)
    }

    private fun validateUserInputAndNavigateToPasswordFragment(phone : String){
        if (phone.isEmpty()){
            phoneNumberTextInputInPhoneNumberFragment.error = "Enter your Phone number, or Sign up with your email"
        }else{
            phoneNumberTextInputInPhoneNumberFragment.error = null
            navigateToPasswordFragment(firstName, lastName, day, month, year, gender, "",phone)
        }
    }
}