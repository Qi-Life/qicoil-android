package com.Meditation.Sounds.frequencies.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar
import com.Meditation.Sounds.frequencies.lemeor.loadImageScalar
import com.Meditation.Sounds.frequencies.models.Playlist
import kotlinx.android.synthetic.main.dialog_add_edit_playlist.*
import kotlinx.android.synthetic.main.dialog_silent_quantum_info.btnClose
import kotlinx.android.synthetic.main.dialog_silent_quantum_info.image
import kotlinx.android.synthetic.main.dialog_silent_quantum_info.tvDescription
import kotlinx.android.synthetic.main.dialog_silent_quantum_info.tvTitle
import kotlinx.android.synthetic.main.scalar_album_item.view.image

class SilentQuantumInfoDialog(private val mContext: Context, private val album: Scalar) : Dialog(mContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_silent_quantum_info)
        setCancelable(false)
        val window = this.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window.attributes = wlp
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes = wlp
        init()
    }

    fun init() {
        loadImageScalar(mContext, image, album)
        tvTitle.text = album.name
        tvDescription.text = Html.fromHtml(album.description)
        tvDescription.movementMethod = ScrollingMovementMethod()

        btnClose.setOnClickListener {
            dismiss()
        }
    }
}
