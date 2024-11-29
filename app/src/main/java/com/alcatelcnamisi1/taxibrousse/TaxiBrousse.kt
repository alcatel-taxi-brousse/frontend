package com.alcatelcnamisi1.taxibrousse

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.ale.rainbowsdk.RainbowSdk
import java.util.Properties


class TaxiBrousse : Application() {

    public override fun onCreate(){
        super.onCreate()

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val properties = Properties()
        val assetManager = applicationContext.assets
        val inputStream = assetManager.open("settings.properties")
        properties.load(inputStream)
        val app_id = properties.getProperty("app.id")
        val app_secret = properties.getProperty("app.secret")
        inputStream.close()

        RainbowSdk().initialize(
            applicationContext = this,
            applicationId = app_id,
            applicationSecret = app_secret
        )
    }
}