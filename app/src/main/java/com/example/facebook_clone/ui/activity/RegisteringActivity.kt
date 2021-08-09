package com.example.facebook_clone.ui.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.facebook_clone.R
import kotlinx.android.synthetic.main.activity_registering.*

class RegisteringActivity : AppCompatActivity() {
    private  var destination : Int = 0
    companion object {
        private const val IS_USER_EXIST="isUserExist"
        fun open(context: Context, isUserExist: Boolean) {
            context.startActivity(Intent(context,RegisteringActivity::class.java).apply {
                putExtra(IS_USER_EXIST, isUserExist)

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registering)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.registering_navigation)
        val navController = navHostFragment.navController

         destination =
            if (intent.getBooleanExtra(IS_USER_EXIST, false)) R.id.loginFragment else R.id.userNameFragment
        navGraph.startDestination = destination
        navController.graph = navGraph

    }

    override fun onBackPressed() {
        //if we are in user name fragment
        if (destination != R.id.loginFragment){
            showStopCreatingAccountDialog()
        }else{
            navigateToRecentUsersActivity()
        }

    }

    private fun showStopCreatingAccountDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.stop_creating_account_dialog_layout)

        val continueButton = dialog.findViewById(R.id.continueCreatingAccount) as TextView
        val stopButton = dialog.findViewById(R.id.stopCreatingAccount) as TextView
        continueButton.setOnClickListener {
            dialog.dismiss()
        }
        stopButton.setOnClickListener {
            navigateToRecentUsersActivity()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun navigateToRecentUsersActivity(){
        val intent = Intent(this, RecentUsersActivity::class.java)
        startActivity(intent)
        finish()
    }

    object Dummy{

    }
}