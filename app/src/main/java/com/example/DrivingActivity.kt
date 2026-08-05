package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class DrivingActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tam ekran, yatay mod
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val carId = intent.getStringExtra("car_id") ?: "car_standard"
        val carName = intent.getStringExtra("car_name") ?: "Standard"
        val lang = intent.getStringExtra("lang") ?: "TR"

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        // Pass car info via URL fragment
        val carColor = when {
            carId.contains("lamborghini") || carId.contains("ferrari") -> "0xff2200"
            carId.contains("tesla") -> "0x1a1a2e"
            carId.contains("bmw") -> "0x0a0a0a"
            carId.contains("mercedes") -> "0xc0c0c0"
            else -> "0x2255ff"
        }

        webView.loadUrl("file:///android_asset/driving.html#$carId,$carColor,${carName.replace(" ","_")},$lang")
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
