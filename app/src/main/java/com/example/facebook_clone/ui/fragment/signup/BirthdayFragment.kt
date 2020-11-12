package com.example.facebook_clone.ui.fragment.signup

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.fragment_birthday.*


private const val TAG = "BirthdayFragment"
class BirthdayFragment : Fragment(R.layout.fragment_birthday) {
    private val args : BirthdayFragmentArgs by navArgs()
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstName = args.firstName
        lastName = args.lastName

        Log.i(TAG, "ISLAM onViewCreated: $args")
        nextButtonInBirthdayFragment.setOnClickListener {
            getUserBirthday()
            //date will be accepted if it is today!!
            navigateToGenderFragment(firstName, lastName, day, month, year)
        }

        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }


    }

    private fun navigateToGenderFragment(firstName:String, lastName:String, day:String, month:String,year:String) {
        val action = BirthdayFragmentDirections.actionBirthdayFragmentToGenderFragment(firstName, lastName, day, month, year)
        findNavController().navigate(action)
    }

    private fun getUserBirthday(){
         day = (datePicker.dayOfMonth).toString()
         month = (datePicker.month + 1).toString()
         year = (datePicker.year).toString()
    }
}