package com.Meditation.Sounds.frequencies.lemeor.ui.programs.dialog

import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseDialogFragment
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import kotlinx.android.synthetic.main.dialog_add_edit_playlist.btnSubmit
import kotlinx.android.synthetic.main.dialog_add_edit_playlist.description
import kotlinx.android.synthetic.main.dialog_add_edit_playlist.edtPlayListName

class EditProgramsDialogFragment(private val oldProgram: Program?, val callback: (String) -> Unit) :
    BaseDialogFragment() {
    override val layoutId: Int
        get() = R.layout.dialog_add_edit_playlist

    override fun initView() {
        edtPlayListName.setText(oldProgram?.name ?: "")
        description.text = getString(R.string.txt_edit_playlist)

        btnSubmit.setOnClickListener {
            callback(edtPlayListName.text.toString())
            dismiss()
        }
    }

    override fun onObserve() {

    }
}