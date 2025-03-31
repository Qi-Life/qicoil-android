package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.Meditation.Sounds.frequencies.R
import kotlinx.android.synthetic.main.dialog_alert_message.btnCancel
import kotlinx.android.synthetic.main.dialog_alert_message.btnOK
import kotlinx.android.synthetic.main.dialog_alert_message.description
import kotlinx.android.synthetic.main.dialog_alert_message.tvDescription

class AlertMessageDialog(
    private val context: Context,
    private val message: String? = null,
    private val isHideBtnNo: Boolean? = false,
    private val title: String? = null,
    private var onOkClick: (() -> Unit)? = null
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_alert_message)
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
        tvDescription.text = message
        if (title != null){
            description.text = title
        }
        if (isHideBtnNo == true) {
            btnCancel.visibility = View.GONE
        }
        btnOK.setOnClickListener {
            onOkClick?.invoke()
            dismiss()
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}
