package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.utils.StringsUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_sign_up.mBtnGetStartedRegister
import kotlinx.android.synthetic.main.dialog_sign_up.mEdConfirmPasswordRegister
import kotlinx.android.synthetic.main.dialog_sign_up.mEdEmailRegister
import kotlinx.android.synthetic.main.dialog_sign_up.mEdNameRegister
import kotlinx.android.synthetic.main.dialog_sign_up.mEdPasswordRegister
import kotlinx.android.synthetic.main.dialog_sign_up.mTvSignIn

class RegistrationFragment : Fragment() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    interface OnRegistrationListener {
        fun onRegistrationInteraction(
            name: String,
            email: String,
            pass: String,
            confirm: String,
            uuid: String
        )

        fun onOpenLogin()
    }

    private var mListener: OnRegistrationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRegistrationListener) {
            mListener = context
        } else {
            throw RuntimeException(" must implement OnRegistrationListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        mTvSignIn.text = Html.fromHtml(getString(R.string.tv_link_sign_in))

        mTvSignIn.setOnClickListener { mListener?.onOpenLogin() }

        mBtnGetStartedRegister.isEnabled = false
        mBtnGetStartedRegister.setOnClickListener {
            if (isValidRegister()) {
                firebaseAnalytics.logEvent("Sign_Up") {
                    param("Name", mEdNameRegister.getText())
                    param("Email", mEdEmailRegister.getText())
                    // param(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
                }
                mListener?.onRegistrationInteraction(
                    mEdNameRegister.getText(),
                    mEdEmailRegister.getText(),
                    mEdPasswordRegister.getText(),
                    mEdConfirmPasswordRegister.getText(),
                    StringsUtils.getDeviceId(requireContext())
                )
            }
        }

        mEdNameRegister.editText.doOnTextChanged { text, _, _, _ ->
            mBtnGetStartedRegister.isEnabled = text.toString().isNotEmpty() && mEdEmailRegister.getText().isNotEmpty() && mEdPasswordRegister.getText().isNotEmpty() && mEdConfirmPasswordRegister.getText().isNotEmpty()
        }

        mEdEmailRegister.editText.doOnTextChanged { text, _, _, _ ->
            mBtnGetStartedRegister.isEnabled = text.toString().isNotEmpty() && mEdNameRegister.getText().isNotEmpty() && mEdPasswordRegister.getText().isNotEmpty() && mEdConfirmPasswordRegister.getText().isNotEmpty()
        }

        mEdPasswordRegister.editText.doOnTextChanged { text, _, _, _ ->
            mBtnGetStartedRegister.isEnabled = text.toString().isNotEmpty() && mEdNameRegister.getText().isNotEmpty() && mEdEmailRegister.getText().isNotEmpty() && mEdConfirmPasswordRegister.getText().isNotEmpty()
        }

        mEdConfirmPasswordRegister.editText.doOnTextChanged { text, _, _, _ ->
            mBtnGetStartedRegister.isEnabled = text.toString().isNotEmpty() && mEdNameRegister.getText().isNotEmpty() && mEdEmailRegister.getText().isNotEmpty() && mEdPasswordRegister.getText().isNotEmpty()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            })
    }

    private fun isValidRegister(): Boolean {
        if (mEdNameRegister.getText().isEmpty()) {
            mEdNameRegister.showError(getString(R.string.tv_please_enter_name))
            return false
        }
        if (mEdEmailRegister.getText().isEmpty()) {
            mEdEmailRegister.showError(getString(R.string.tv_please_enter_email))
            return false
        }
        if (!mEdEmailRegister.getText().matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+".toRegex())) {
            mEdEmailRegister.showError(getString(R.string.tv_invalid_email_add))
            return false
        }
        if (mEdPasswordRegister.getText().isEmpty()) {
            mEdPasswordRegister.showError(getString(R.string.tv_please_enter_pass))
            return false
        }
        if (mEdPasswordRegister.getText().length < 6) {
            mEdPasswordRegister.showError(getString(R.string.tv_err_pass_characters))
            return false
        }
        if (mEdConfirmPasswordRegister.getText().isEmpty()) {
            mEdConfirmPasswordRegister.showError(getString(R.string.tv_please_enter_confirm_password))
            return false
        }
        if (mEdConfirmPasswordRegister.getText() != mEdPasswordRegister.getText()) {
            mEdConfirmPasswordRegister.showError(getString(R.string.tv_please_check_confirm_password))
            return false
        }
        return true
    }
}