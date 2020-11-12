package com.example.facebook_clone.ui.fragment.signup

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.fragment_user_name.*

private const val TAG = "UserNameFragment"
class UserNameFragment : Fragment(R.layout.fragment_user_name) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextButtonInNameFragment.setOnClickListener {
            val firstName = firstNameTextInput.editText?.text.toString()
            val lastName = lastNameTextInput.editText?.text.toString()
            validateUserInputAndNavigateToBirthdayFragment(firstName, lastName)
        }

        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    private fun navigateToBirthdayFragment(firstName: String, lastName: String) {
        val action =
            UserNameFragmentDirections.actionUserNameFragmentToBirthdayFragment(firstName, lastName)
        findNavController().navigate(action)
    }

    private fun validateUserInputAndNavigateToBirthdayFragment(
        firstName: String,
        lastName: String
    ) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            if (firstName.isEmpty()) {
                firstNameTextInput.error = "Enter your first name"

            }
            if (lastName.isEmpty()) {
                lastNameTextInput.error = "Enter your last name"
            }
        } else {
            firstNameTextInput.error = null
            lastNameTextInput.error = null
            navigateToBirthdayFragment(firstName, lastName)
        }


    }


}