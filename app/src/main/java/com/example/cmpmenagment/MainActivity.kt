package com.example.cmpmenagment

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : AppCompatActivity() {
    private lateinit var consentInformation: ConsentInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val params = consentRequestParameters()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.reset()
        consentInformation.requestConsentInfoUpdate(this, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                this@MainActivity
            ) { loadAndShowError ->
                // Consent gathering failed.
                if (loadAndShowError != null) {
                    Log.w(
                        "TAG", String.format(
                            "%s: %s",
                            loadAndShowError.errorCode,
                            loadAndShowError.message
                        )
                    )
                }

                // Consent has been gathered.
                if (consentInformation.canRequestAds()) {
                    // initializeMobileAdsSdk()
                    startTimer()
                    loadAds()
                }
            }
        },
            { requestConsentError ->
                // Consent gathering failed.
                Log.w(
                    "TAG", String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            })

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            //  initializeMobileAdsSdk()
            startTimer()
            loadAds()

        }
    }

    private fun consentRequestParameters(): ConsentRequestParameters {
        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("E8A2249DCF6D6F34006080DFBC106973")
            .build()
        return ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
    }

    private fun loadAds() {
        HyperSoftInterstitialHelper.loadAdmobInterstitial(
            this,
            getString(R.string.splash_interstitial)
        )
    }

    private fun startTimer() {
        val timer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                println("Seconds remaining: $secondsRemaining")
            }

            override fun onFinish() {
                val intent = Intent(this@MainActivity, NextActivity::class.java)
                startActivity(intent)
                finish()
                HyperSoftInterstitialHelper.showAdmobInterstitial(this@MainActivity)
            }
        }

        timer.start()
    }
}
