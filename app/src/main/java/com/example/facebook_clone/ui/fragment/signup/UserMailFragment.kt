package com.example.facebook_clone.ui.fragment.signup

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

private const val TAG = "UserMailFragment"
class UserMailFragment : Fragment(R.layout.fragment_mail_user) {
    private val args : UserMailFragmentArgs by navArgs()
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var gender: String
    private lateinit var email:String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstName = args.firstName
        lastName = args.lastName
        day = args.day
        month = args.month
        year = args.year
        gender = args.gender

        Log.i(TAG, "ISLAM onViewCreated: $args")

        nextButtonInMailFragment.setOnClickListener {
             email = emailTextInputInEmailFragment.editText?.text.toString()
             validateUserInputAndNavigateToPasswordFragment(email)
        }

        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    private fun navigateToPasswordFragment(firstName:String, lastName:String, day:String, month:String,year:String, gender: String, email: String, phone:String ) {
        val action =
            UserMailFragmentDirections.actionUserMailFragmentToNewPasswordFragment(firstName, lastName, day, month, year, gender, email,"")
        findNavController().navigate(action)
    }

    private fun validateUserInputAndNavigateToPasswordFragment(email : String){
        if (email.isEmpty()){
            emailTextInputInEmailFragment.error = "Enter your email, or Sign up with your phone number"
        }else{
            emailTextInputInEmailFragment.error = null
            navigateToPasswordFragment(firstName, lastName, day, month, year, gender, email,"")
        }
    }
}