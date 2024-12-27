package com.Meditation.Sounds.frequencies.lemeor.ui.options.change_pass

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.showAlert
import com.Meditation.Sounds.frequencies.lemeor.tools.HudHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import kotlinx.android.synthetic.main.activity_change_pass.change_pass_btn
import kotlinx.android.synthetic.main.activity_change_pass.change_pass_et_confirm_new_pass
import kotlinx.android.synthetic.main.activity_change_pass.change_pass_et_new_pass
import kotlinx.android.synthetic.main.activity_change_pass.change_pass_et_old_pass

class ChangePassActivity : AppCompatActivity() {

    private lateinit var mViewModel: ChangePassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)

        mViewModel = ViewModelProvider(
            this, ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        )[ChangePassViewModel::class.java]

        change_pass_btn.setOnClickListener { onChange() }
    }

    private fun onChange() {
        if (!isValidChangePassword()) {
            return
        }

        val oldPass = change_pass_et_old_pass.getText().trim()
        val newPass = change_pass_et_new_pass.getText().trim()
        val confirmPass = change_pass_et_confirm_new_pass.getText().trim()
        val userEmail = PreferenceHelper.getUser(applicationContext)?.email

        userEmail?.let { user_email ->
            mViewModel.updateProfile(user_email, oldPass, newPass, null, confirmPass)
                .observe(this) { user ->
                    user?.let { resource ->
                        when (resource.status) {
                            Resource.Status.SUCCESS -> {
                                HudHelper.hide()

                                PreferenceHelper.saveUser(applicationContext, resource.data)

                                finish()
                            }

                            Resource.Status.ERROR -> {
                                HudHelper.hide()
                                showAlert(this@ChangePassActivity, user.message ?: getString(R.string.msg_error_occurred))
                            }

                            Resource.Status.LOADING -> {
                                HudHelper.show(this)
                            }
                        }
                    }
                }
        }
    }

    private fun isValidChangePassword(): Boolean {
        if (change_pass_et_old_pass.getText().trim().isEmpty()) {
            change_pass_et_old_pass.showError(getString(R.string.tv_please_enter_old_pass))
            return false
        }
        if (change_pass_et_old_pass.getText().trim().length < 6) {
            change_pass_et_old_pass.showError(getString(R.string.tv_err_old_pass_characters))
            return false
        }
        if (change_pass_et_new_pass.getText().trim().isEmpty()) {
            change_pass_et_new_pass.showError(getString(R.string.tv_please_enter_new_pass))
            return false
        }
        if (change_pass_et_new_pass.getText().trim().length < 6) {
            change_pass_et_new_pass.showError(getString(R.string.tv_err_new_pass_characters))
            return false
        }
        if (change_pass_et_confirm_new_pass.getText().trim().isEmpty()) {
            change_pass_et_confirm_new_pass.showError(getString(R.string.tv_please_enter_confirm_pass))
            return false
        }
        if (change_pass_et_confirm_new_pass.getText().trim().length < 6) {
            change_pass_et_confirm_new_pass.showError(getString(R.string.tv_err_confirm_pass_characters))

            return false
        }
        if (change_pass_et_confirm_new_pass.getText()
                .trim() != change_pass_et_new_pass.getText().trim()
        ) {
            change_pass_et_confirm_new_pass.showError(getString(R.string.tv_please_enter_confirm_new_pass))

            return false
        }
        return true
    }
}