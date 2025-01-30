package com.example.individualproject.utils

import android.app.Activity
import android.app.AlertDialog
import com.example.individualproject.R

class LoadingUtils(val activity: Activity) {
    lateinit var alertDialog: AlertDialog

    fun show(){
        val builder= AlertDialog.Builder(activity)
        val designView= activity.layoutInflater.inflate(R.layout.loading,null)

        builder.setView(designView)
        builder.setCancelable(false)
        alertDialog=builder.create()
        alertDialog.show()
    }

    fun dismiss(){
        alertDialog.dismiss()
    }
}