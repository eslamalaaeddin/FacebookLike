package com.example.facebook_clone.ui.fragment.signupflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.facebook_clone.R
import com.example.facebook_clone.ui.activity.ProfilePictureActivity
import kotlinx.android.synthetic.main.fragment_password_new.*

private const val TAG = "NewPasswordFragment"
class NewPasswordFragment : Fragment(R.layout.fragment_password_new) {
    private val args : NewPasswordFragmentArgs by navArgs()

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var gender: String
    private lateinit var email:String
    private lateinit var phone:String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, "ISLAM onViewCreated: $args")

        //firstName = args.firstName.... and so on
        
        nextButtonInPasswordFragment.setOnClickListener {
            val password = passwordTextInputInPasswordFragment.editText?.text.toString()
            validateUserInputAndCreateAccount(password)
        }

        val upButtonImageView : ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun validateUserInputAndCreateAccount(password:String){
        if (password.isEmpty()){
            passwordTextInputInPasswordFragment.error = "You must enter a password"
        }else{
            passwordTextInputInPasswordFragment.error = null
            //create account and navigate to profile picture activity
            navigateToProfilePictureActivity()

        }
    }

    private fun navigateToProfilePictureActivity(){
        val intent = Intent(requireContext(), ProfilePictureActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}