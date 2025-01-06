package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.dialog_alert_message_notify.btnOk
import kotlinx.android.synthetic.main.dialog_alert_message_notify.tvMessgae

class AlertMessageNotifyDialog(
    private val context: Context, private val message: String? = null
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_alert_message_notify)
        val window = this.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes = wlp
        setCancelable(false)
        init()
    }

    fun init() {
        tvMessgae.text = message
        btnOk.setOnClickListener {
            dismiss()
        }
    }
}
