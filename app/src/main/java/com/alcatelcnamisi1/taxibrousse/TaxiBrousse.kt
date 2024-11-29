package com.alcatelcnamisi1.taxibrousse

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.ale.rainbowsdk.RainbowSdk

class TaxiBrousse : Application() {

    public override fun onCreate(){
        super.onCreate()

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        RainbowSdk().initialize(
            applicationContext = this,
            applicationId = "9527e050808c11efa6661b0bb9c90370",
            applicationSecret = "UDZBdWsdk6iM4Ke18dzQ8WLbgx2NCg4aGjAXuSfQikcmeNnQj6AvNUJjrc6o1yWP"
        )
    }
}