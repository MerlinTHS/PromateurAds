package de.promateur.ads

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.usercentrics.sdk.Usercentrics
import com.usercentrics.sdk.UsercentricsBanner
import com.usercentrics.sdk.UsercentricsOptions
import com.usercentrics.sdk.UsercentricsUserInteraction
import de.promateur.ads.ui.theme.PromateurAdsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUsercentrics()

        setContent {
            PromateurAdsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.padding(16.dp)) {
                        AdBanner(Modifier.align(Alignment.TopCenter))
                        Button(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = { collectConsent() }
                        ) {
                            Text(text = "Edit consent")
                        }
                    }
                }
            }
        }
    }

    private var initializedAds = false
    private fun initAds() {
        if (initializedAds) return

        MobileAds.initialize(this)
        initializedAds = true
    }

    private fun initUsercentrics() = with(Usercentrics) {
        initialize(context = this@MainActivity, UsercentricsOptions(settingsId = "Yf-hzQby0S9a7s"))
        isReady(
            onSuccess = { status ->
                when {
                    status.shouldCollectConsent -> collectConsent()
                    status.consents.any { it.status } -> initAds()
                }
            },
            onFailure = { error -> showAlertDialog("Error while initializing Usercentrics: $error") }
        )
    }

    private fun collectConsent() {
        val banner = UsercentricsBanner(this)
        banner.showFirstLayer { if (it?.userInteraction != UsercentricsUserInteraction.DENY_ALL) { initAds() } }
    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(true)
            .show()
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val unitId = "/6499/example/native"

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}