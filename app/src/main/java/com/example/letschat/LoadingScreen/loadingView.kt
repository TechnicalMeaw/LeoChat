package com.example.letschat.LoadingScreen

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat.getColor
import com.example.letschat.R
import kotlinx.android.synthetic.main.loading_dialog.view.*
import android.os.Handler
import android.view.View

class loadingView {

    companion object{

        private var mAlertDialog : AlertDialog? = null
        private var mDialogView: View? = null
        private var context : Context? = null


        private val hideLoadingDialog = Runnable { kotlin.run {
            mAlertDialog?.dismiss()
        } }

        private val pauseSuccessAnimation = Runnable { kotlin.run {
            mDialogView?.loadingSpin?.pauseAnimation()
            Handler().postDelayed(hideLoadingDialog, 150)
        } }

        private val showSuccessText = Runnable { kotlin.run { mDialogView?.loadingSpin?.setMinAndMaxProgress(0.27f, 0.5f)
            mDialogView?.loadingText?.text = "Success"
            mDialogView?.loadingText?.setTextColor(getColor(context!!, R.color.colorPrimary))
        } }

        private val showSuccessAnimation = Runnable { kotlin.run { mDialogView?.loadingSpin?.setMinAndMaxProgress(0.27f, 0.5f)
            Handler().postDelayed(pauseSuccessAnimation, 3000)
            Handler().postDelayed(showSuccessText, 2000)
        } }



        fun showLoadingScreen(context: Context){
            if (mAlertDialog?.isShowing != true){
                this.context = context
                mDialogView = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null)
                mDialogView?.loadingSpin?.setMinAndMaxProgress(0.0f, 0.1411f)
                mDialogView?.loadingSpin?.loop(true)
                mDialogView?.loadingSpin?.playAnimation()
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)
                    .setCancelable(true)



                mAlertDialog = mBuilder.show()
                mAlertDialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent);

            }
        }

        fun showLoadingSuccess(){
            if (mAlertDialog?.isShowing == true)
                Handler().postDelayed(showSuccessAnimation, 1)
        }

        fun hideLoadingScreen(){
            if (mAlertDialog?.isShowing == true)
                Handler().postDelayed(hideLoadingDialog, 1)
        }

    }
}