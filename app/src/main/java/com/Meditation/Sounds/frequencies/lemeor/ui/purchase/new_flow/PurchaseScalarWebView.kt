package com.Meditation.Sounds.frequencies.lemeor.ui.purchase.new_flow

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.Meditation.Sounds.frequencies.R
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity
import com.Meditation.Sounds.frequencies.models.event.SyncDataEvent
import kotlinx.android.synthetic.main.fragment_purchase_item_album_web_view.*
import org.greenrobot.eventbus.EventBus

class PurchaseScalarWebView : BaseActivity() {
    private var urlPurchase = ""
    private var isSynced = false
    override fun initLayout(): Int {
        return R.layout.fragment_purchase_item_album_web_view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initComponents() {
        if (intent.hasExtra(EXTRA_URL_PURCHASE) && intent.getStringExtra(EXTRA_URL_PURCHASE) != null) {
            urlPurchase = intent.getStringExtra(EXTRA_URL_PURCHASE).toString()
            webview_purchase.settings.javaScriptEnabled = true
            webview_purchase.settings.domStorageEnabled = true
            webview_purchase.webChromeClient = object : WebChromeClient() {
                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    if (message == "Success" && !isSynced) {
                        isSynced = true
                        EventBus.getDefault().post(SyncDataEvent(isSyncScalar = true))
                    }
                    if (message == "Goback" || message == "Back") {
                        finish()
                    }
                    result?.confirm()
                    return true
                }
            }
            webview_purchase.clearHistory()
            webview_purchase.clearCache(true)
            webview_purchase.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(url.toString())
                    return true
                }
            }
            webview_purchase.loadUrl(urlPurchase)
        } else {
            finish()
        }
    }

    override fun addListener() {
    }

    companion object {
        const val EXTRA_URL_PURCHASE = "extra_url_purchase"
        fun newIntent(context: Context, url: String): Intent {
            val intent = Intent(context, PurchaseScalarWebView::class.java)
            intent.putExtra(EXTRA_URL_PURCHASE, url)
            return intent
        }
    }
}