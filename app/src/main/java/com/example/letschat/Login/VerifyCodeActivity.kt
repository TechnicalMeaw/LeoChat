package com.example.letschat.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letschat.LatestMessagesActivity
import com.example.letschat.Notifications.MyFirebaseMessagingService
import com.example.letschat.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_verify_code.*
import java.util.concurrent.TimeUnit


class VerifyCodeActivity : AppCompatActivity() {

    var phoneNum: String? = null
    var notificationToken: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        phoneNum = intent.getStringExtra("PHONE_NUMBER")
        Toast.makeText(this, "Sending code to $phoneNum", Toast.LENGTH_SHORT).show()

        changeNumberTextView.text = "Enter the OTP sent to $phoneNum"

        sendCode(phoneNum!!)

        // Setting Up Notification Token
        MyFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            MyFirebaseMessagingService.token = it.token
            notificationToken = it.token
        }

        finishTextView.setOnClickListener { finish() }
    }

    private fun sendCode(phoneNumber: String){

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        Toast.makeText(this, "Code Sent.", Toast.LENGTH_SHORT).show()
        startCountdown()
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            callbacks,  // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks

        Toast.makeText(this, "Code Sent.", Toast.LENGTH_SHORT).show()
        startCountdown()
    }



    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("VerifyCodeActivity", "onVerificationCompleted:$credential")

            Toast.makeText(this@VerifyCodeActivity, "Success", Toast.LENGTH_SHORT).show()

            // Upload Image to fireStorage
            // val user = FirebaseAuth.getInstance().currentUser
            // updateUI(user)
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w("VerifyCodeActivity", "onVerificationFailed", e)

            // If sign in fails, display a message to the user.
            // updateUI(null)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(this@VerifyCodeActivity, e.message, Toast.LENGTH_LONG).show()
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@VerifyCodeActivity, e.message, Toast.LENGTH_LONG).show()
                // ...
            }

            // Show a message and update the UI
            // ...
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("VerifyCodeActivity", "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token

            // ...
        }
    }


    private fun startCountdown(){
        resendCodeTextView.isClickable = false

        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendCodeTextView.text = (millisUntilFinished/1000).toString()
            }

            override fun onFinish() {
                resendCodeTextView.text = getString(R.string.resend_code)
                resendCodeTextView.isClickable = true
            }
        }
        timer.start()
    }



    private fun verifyVerificationCode(code: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)

        // signing the user
        signInWithPhoneAuthCredential(credential)
    }

    fun resendCode(view: View) {
        resendVerificationCode(phoneNum!!, resendToken!!)
        resendCodeTextView.isClickable = false
    }

    fun verifyCode(view: View) {
        verifyVerificationCode(otpEditText.text.toString())
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("VerifyCodeActivity", "signInWithCredential:success")
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()

                    val user = task.result?.user
                    registerToDatabase(user)
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("VerifyCodeActivity", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun registerToDatabase(user: FirebaseUser?) {

        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    ref.child("notificationToken").setValue(notificationToken).addOnSuccessListener {
                        Log.d("VerifyCodeActivity", "Notification Token Updated Successfully")
                    }
                    redirectToLatestMessagesActivity()
                }else{
                    redirectToEnterUserInfo()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }



    fun redirectToEnterUserInfo(){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("PHONE_NUMBER", phoneNum)
        startActivity(intent)
    }


    fun redirectToLatestMessagesActivity(){
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}