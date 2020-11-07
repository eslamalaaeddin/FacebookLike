package com.example.facebook_clone.ui.fragment.signinflow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.facebook_clone.R
import com.example.facebook_clone.ui.activity.RegisteringActivity
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_user_name.*

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener {
            val mailOrPhone = phoneOrEmailTextInputInLoginFragment.editText?.text.toString()
            val password = passwordTextInputInLoginFragment.editText?.text.toString()

            validateUserInputAndNavigateToNewsFeed(mailOrPhone, password)
        }

        createNewFacebookAccountInLoginFragment.setOnClickListener {
            navigateToCreateNewAccount()
        }

        forgetPasswordTextView.setOnClickListener{
            navigateToForgetPasswordFragment()
        }

    }


    private fun navigateToForgetPasswordFragment() {
        val action = LoginFragmentDirections.actionLoginFragmentToForgetPasswordFragment()
        findNavController().navigate(action)
    }

    private fun navigateToCreateNewAccount() {
        RegisteringActivity.open(requireContext(),false)
        activity?.finish()
    }

    private fun navigateToNewsFeedActivity(){
        val action = LoginFragmentDirections.actionLoginFragmentToNewsFeedActivity()
        findNavController().navigate(action)
        activity?.finish()
    }

    private fun validateUserInputAndNavigateToNewsFeed(mailOrPhone :String, password : String){
        if (mailOrPhone.isEmpty() || password.isEmpty()) {
            if (mailOrPhone.isEmpty()) {
                phoneOrEmailTextInputInLoginFragment.error = "Enter an email or phone"

            }
            if (password.isEmpty()) {
                passwordTextInputInLoginFragment.error = "Enter your password"
            }
        } else {
            //First check if it is mail
            //else it is phone
            //TODO
            phoneOrEmailTextInputInLoginFragment.error = null
            passwordTextInputInLoginFragment.error = null
            navigateToNewsFeedActivity()
        }
    }



}