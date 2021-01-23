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
import kotlinx.android.synthetic.main.fragment_gender.*

private const val TAG = "GenderFragment"

class GenderFragment : Fragment(R.layout.fragment_gender) {

    private val args: GenderFragmentArgs by navArgs()
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var gender: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstName = args.firstName
        lastName = args.lastName
        day = args.day
        month = args.month
        year = args.year

        nextButtonInGenderFragment.setOnClickListener {
           validateUserInputAndNavigateToUserMailFragment(maleOrFemale())
        }

        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun navigateToUserMailFragment(firstName:String, lastName:String, day:String, month:String,year:String, gender: String) {
        val action = GenderFragmentDirections.actionGenderFragmentToUserMailFragment(firstName, lastName, day, month,year, gender)
        findNavController().navigate(action)
    }

    private fun maleOrFemale() : String{
        return when (maleFemaleRadioGroup.checkedRadioButtonId) {
            1 -> {
                gender = "male"
                "male"
            }
            2 -> {
                gender = "female"
                "female"
            }
            else -> ""
        }
    }
    private fun validateUserInputAndNavigateToUserMailFragment(gender:String){
        if (gender.isEmpty()){
            Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show()
        }
        else{
            navigateToUserMailFragment(firstName, lastName, day, month, year, gender)
        }
    }

}