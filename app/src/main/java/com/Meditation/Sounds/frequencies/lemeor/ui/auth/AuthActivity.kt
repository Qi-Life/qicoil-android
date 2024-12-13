package com.Meditation.Sounds.frequencies.lemeor.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.Meditation.Sounds.frequencies.BuildConfig
import com.Meditation.Sounds.frequencies.QApplication
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.lemeor.data.api.ApiConfig.getPassResetUrl
import com.Meditation.Sounds.frequencies.lemeor.data.api.RetrofitBuilder
import com.Meditation.Sounds.frequencies.lemeor.data.database.DataBase
import com.Meditation.Sounds.frequencies.lemeor.data.model.AuthResponse
import com.Meditation.Sounds.frequencies.lemeor.data.model.Program
import com.Meditation.Sounds.frequencies.lemeor.data.remote.ApiHelper
import com.Meditation.Sounds.frequencies.lemeor.data.utils.Resource
import com.Meditation.Sounds.frequencies.lemeor.data.utils.ViewModelFactory
import com.Meditation.Sounds.frequencies.lemeor.tools.HudHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isAppPurchased
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.isLogged
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.preference
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.saveUser
import com.Meditation.Sounds.frequencies.lemeor.tools.PreferenceHelper.token
import com.Meditation.Sounds.frequencies.lemeor.ui.TrialActivity
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.LoginFragment.OnLoginListener
import com.Meditation.Sounds.frequencies.lemeor.ui.auth.RegistrationFragment.OnRegistrationListener
import com.Meditation.Sounds.frequencies.models.event.SyncDataEvent
import com.Meditation.Sounds.frequencies.models.event.UpdateViewSilentQuantumEvent
import com.Meditation.Sounds.frequencies.utils.Constants
import com.Meditation.Sounds.frequencies.utils.Constants.Companion.PREF_SETTING_ADVANCE_SCALAR_ON_OFF
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class AuthActivity : AppCompatActivity(), OnLoginListener, OnRegistrationListener {

    private lateinit var mViewModel: AuthViewModel

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                ApiHelper(RetrofitBuilder(applicationContext).apiService),
                DataBase.getInstance(applicationContext)
            )
        )[AuthViewModel::class.java]
        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.auth_container, fragment, fragment.javaClass.simpleName)

        if (!supportFragmentManager.isStateSaved) {
            transaction.commit()
        } else {
            transaction.commitAllowingStateLoss()
        }
    }

    private fun saveAuthData(resource: Resource<AuthResponse>) {
        preference(applicationContext).isLogged = true
        preference(applicationContext).token = resource.data?.token
        saveUser(applicationContext, resource.data?.user)
        resource.data?.user?.let { user -> updateUnlocked(applicationContext, user, true) }

        val isAppPurchased = resource.data?.user?.is_purchased == 1
        preference(QApplication.getInstance().applicationContext).isAppPurchased = isAppPurchased
        SharedPreferenceHelper.getInstance().setBool(PREF_SETTING_ADVANCE_SCALAR_ON_OFF, isAppPurchased)
        //update UI tab
        EventBus.getDefault().post(UpdateViewSilentQuantumEvent)

        if (resource.data?.user?.program_schedule != null) {
            val programSchedule = resource.data.user.program_schedule
            if ((programSchedule?.programId ?: 0) > 0) {
                PreferenceHelper.saveScheduleProgram(this@AuthActivity, Program(id = programSchedule?.programId ?: 0, name = programSchedule?.programName ?: ""))
            }

            //local
            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_START_TIME_AM, programSchedule?.startTimeAm ?: 0f)
            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_END_TIME_AM, programSchedule?.stopTimeAm ?: 180f)

            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_START_TIME_PM, programSchedule?.startTimePm ?: 540f)
            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_END_TIME_PM, programSchedule?.stopTimePm ?: 719f)

            //server
            SharedPreferenceHelper.getInstance()
                .setInt(Constants.PREF_SCHEDULE_PROGRAM_ID_API, programSchedule?.programId ?: -1)

            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_START_TIME_AM_API, programSchedule?.startTimeAm ?: 0f)
            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_END_TIME_AM_API, programSchedule?.stopTimeAm ?: 0f)

            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_START_TIME_PM_API, programSchedule?.startTimePm ?: 0f)
            SharedPreferenceHelper.getInstance()
                .setFloat(Constants.PREF_SCHEDULE_END_TIME_PM_API, programSchedule?.stopTimePm ?: 0f)
        }

        EventBus.getDefault().post("showDisclaimer")
        Handler(Looper.getMainLooper()).postDelayed({ EventBus.getDefault().post(SyncDataEvent())}, 2000)
    }

    private fun sendDataWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed({sendData()}, 3000)
    }

    private fun sendData() {
        HudHelper.hide()
        val intent = Intent()
        setResult(RESULT_OK, intent)
        if (!BuildConfig.IS_FREE) {
            CoroutineScope(Dispatchers.IO).launch {
                val albumDao = DataBase.getInstance(applicationContext).albumDao()
                albumDao.getAllAlbums().find { !it.isUnlocked }?.let {
                    runOnUiThread { openAd() }
                }
            }
        }
        finish()
    }

    private fun openAd() {
        val intent1 = Intent(this, TrialActivity::class.java)
        startActivity(intent1)
    }

    override fun onLoginInteraction(email: String, password: String) {
        if (email == "guest") {
            sendDataWithDelay()
            val eventValues = HashMap<String, Any>()
            eventValues[AFInAppEventParameterName.REVENUE] = 0
            AppsFlyerLib.getInstance().logEvent(
                applicationContext,
                "guest_login",
                eventValues
            )
        } else {
            mViewModel.login(email, password).observe(this) {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            saveAuthData(resource)
                            Constants.isGuestLogin = false
                            sendDataWithDelay()
                            val eventValues = HashMap<String, Any>()
                            eventValues[AFInAppEventParameterName.REVENUE] = 0
                            AppsFlyerLib.getInstance().logEvent(
                                applicationContext,
                                "login",
                                eventValues
                            )
                        }
                        Resource.Status.ERROR -> {
                            HudHelper.hide()
                            Toast.makeText(
                                applicationContext,
                                it.message ?: getString(R.string.msg_error_occurred),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Resource.Status.LOADING -> {
                            HudHelper.show(this)
                        }
                    }
                }
            }
        }
    }

    override fun onOpenRegistration() {
        replaceFragment(RegistrationFragment())
    }

    override fun onRegistrationInteraction(
        name: String,
        email: String,
        pass: String,
        confirm: String,
        uuid: String
    ) {
        mViewModel.register(email, pass, confirm, name, uuid).observe(this) {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        saveAuthData(resource)
                        Constants.isGuestLogin = false
                        sendDataWithDelay()
                        val eventValues = HashMap<String, Any>()
                        eventValues[AFInAppEventParameterName.REVENUE] = 0
                        AppsFlyerLib.getInstance().logEvent(
                            applicationContext,
                            "register",
                            eventValues
                        )
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(
                            applicationContext,
                            it.message ?: getString(R.string.msg_error_occurred),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        }
    }

    override fun onOpenLogin() {
        replaceFragment(LoginFragment())
    }

    override fun onOpenForgotPassword() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getPassResetUrl())))
    }

    override fun onGoogleLogin(email: String, name: String, google_id: String) {
        mViewModel.googleLogin(email, name, google_id).observe(this) {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        saveAuthData(resource)
                        Constants.isGuestLogin = false
                        sendDataWithDelay()
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(
                            applicationContext,
                            it.message ?: getString(R.string.msg_error_occurred),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        }
    }


    override fun onFbLogin(email: String, name: String, fb_id: String) {
        mViewModel.fbLogin(email, name, fb_id).observe(this) {
            it?.let { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        saveAuthData(resource)
                        Constants.isGuestLogin = false
                        sendDataWithDelay()
                    }
                    Resource.Status.ERROR -> {
                        HudHelper.hide()
                        Toast.makeText(
                            applicationContext,
                            it.message ?: getString(R.string.msg_error_occurred),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.Status.LOADING -> {
                        HudHelper.show(this)
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}