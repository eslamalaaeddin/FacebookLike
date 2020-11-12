package com.example.facebook_clone.ui.fragment.signup

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.User
import com.example.facebook_clone.ui.activity.ProfilePictureActivity
import com.example.facebook_clone.viewmodel.PasswordFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_password.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "NewPasswordFragment"

class NewPasswordFragment : Fragment(R.layout.fragment_password) {
    private val args: NewPasswordFragmentArgs by navArgs()
    private val passFragViewModel by viewModel<PasswordFragmentViewModel>()
    private val auth: FirebaseAuth by inject()
    private var progressDialog: ProgressDialog? = null

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var day: String
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var gender: String
    private lateinit var email: String
    private lateinit var phone: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstName = args.firstName
        lastName = args.lastName
        day = args.day
        month = args.month
        year = args.year
        gender = args.gender
        email = args.email
        phone = args.phone

        nextButtonInPasswordFragment.setOnClickListener {
            val password = passwordTextInputInPasswordFragment.editText?.text.toString()
            validateUserInputAndCreateAccount(password)
        }

        val upButtonImageView: ImageView = view.findViewById(R.id.upButtonImageView)

        upButtonImageView.setOnClickListener {
            navigateToMailFragment(firstName, lastName, day, month, year, gender, email, phone)
        }


    }

    private fun navigateToMailFragment(
        firstName: String,
        lastName: String,
        day: String,
        month: String,
        year: String,
        gender: String,
        email: String,
        phone: String
    ) {
        val action = NewPasswordFragmentDirections.actionNewPasswordFragmentToUserMailFragment2(
            firstName,
            lastName,
            day,
            month,
            year,
            gender
        )
        findNavController().navigate(action)

    }

    private fun validateUserInputAndCreateAccount(password: String) {
        if (password.isEmpty()) {
            passwordTextInputInPasswordFragment.error = "You must enter a password"
        } else {
            passwordTextInputInPasswordFragment.error = null
            progressDialog = Utils.showProgressDialog(requireContext(), "Please wait...")
            passFragViewModel.createAccountWithMailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = createUser()
                        passFragViewModel.uploadUserDataToDB(user).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                progressDialog?.dismiss()
                                navigateToProfilePictureActivity()
                            } else {
                                Utils.toastMessage(
                                    requireContext(),
                                    task.exception?.message.toString()
                                )
                            }
                        }

                    } else {
                        Utils.toastMessage(requireContext(), task.exception?.message.toString())
                    }
                }
        }

    }

    private fun navigateToProfilePictureActivity() {
        val intent = Intent(requireContext(), ProfilePictureActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun createUser(): User {
        return User(
            id = auth.currentUser?.uid.toString(),
            name = "$firstName $lastName",
            gender = gender,
            birthDay = "$day/$month/$year",
        )
    }

}