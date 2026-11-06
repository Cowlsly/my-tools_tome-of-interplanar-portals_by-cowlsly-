package online.cowlsly.tome

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity

/**
 * Each saved portal opens as an in-app web experience.
 * APK packaging is NOT part of this v0.1 alpha.
 */
class PortalActivity : FragmentActivity() {
    private var browser: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = UrlPolicy.normalizeWebUrl(intent.getStringExtra("url").orEmpty())
        if (url == null) { finish(); return }

        val view = WebView(this)
        browser = view
        WebView.setWebContentsDebuggingEnabled(false)
        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            safeBrowsingEnabled = true
        }
        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(view, false)
        view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val target = request?.url?.toString() ?: return true
                // External schemes are blocked. OAuth that requires a system browser
                // needs a separate approved Custom Tabs implementation.
                return UrlPolicy.normalizeWebUrl(target) == null
            }
        }
        view.webChromeClient = WebChromeClient()
        setContentView(view)
        view.loadUrl(url)
    }

    @Deprecated("Legacy onBackPressed is retained for Android 8 compatibility.")
    override fun onBackPressed() {
        val view = browser
        if (view != null && view.canGoBack()) view.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        browser?.apply { stopLoading(); loadUrl("about:blank"); destroy() }
        browser = null
        super.onDestroy()
    }
}
