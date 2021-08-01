package com.example.letschat.Login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.letschat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        performAnimation()

        val ccp = findViewById<CountryCodePicker>(R.id.ccp)

        loginSendCodeBtn.setOnClickListener{
            closeKeyboard(loginPhoneEditText)
            if(loginPhoneEditText.text?.length == 10 ){
                val phoneNum = ccp.selectedCountryCodeWithPlus + loginPhoneEditText.text
                RedirectTosendCode(phoneNum)
            }else
                Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()

        }

    }

    private fun performAnimation() {
        val translateAnimBG= AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.login_center_to_top);

        val translateInput = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.login_textfield_side_to_center);

        val translateBtn = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.login_btn_side_to_center);

        val translateTitle = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.login_title_top_to_down);

        loginAnimationBg.startAnimation(translateAnimBG);
        loginPhoneInputLinearLayout.startAnimation(translateInput)
        loginSendCodeBtn.startAnimation(translateBtn)
        loginTitleTextView.startAnimation(translateTitle)
    }

    private fun RedirectTosendCode(phoneNumber: String){
        val intent = Intent(this, VerifyCodeActivity::class.java)
        intent.putExtra("PHONE_NUMBER", phoneNumber)
        startActivity(intent)
    }

    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}