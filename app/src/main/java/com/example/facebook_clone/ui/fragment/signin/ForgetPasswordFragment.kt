package com.example.facebook_clone.ui.fragment.signin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.viewmodel.fragment.ForgetPasswordFragmentViewModel
import kotlinx.android.synthetic.main.fragment_forget_password.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgetPasswordFragment : Fragment(R.layout.fragment_forget_password) {
    private val forgetPassFragViewModel by viewModel<ForgetPasswordFragmentViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upButtonImageView.setOnClickListener { navigateToLoginFragment() }

        findYourAccount.setOnClickListener {
            if (getMail().isEmpty()){
                emailTextInput.error = "Please enter your mail"
            }else{
                emailTextInput.error = null
                forgetPassFragViewModel.sendEmailToResetPassword(getMail()).addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Utils.toastMessage(requireContext(), "Email has been sent to your email")
                        navigateToLoginFragment()
                    }else{
                        Utils.toastMessage(requireContext(), task.exception?.message.toString())
                    }
                }

            }
        }
    }

    private fun navigateToLoginFragment() {
        val action = ForgetPasswordFragmentDirections.actionForgetPasswordFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun getMail() : String  {
        return emailTextInput.editText?.text.toString()
    }





}