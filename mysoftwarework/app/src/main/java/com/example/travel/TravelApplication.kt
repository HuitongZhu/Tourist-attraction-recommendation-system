package com.example.travel

import android.app.Application
import android.util.Log
import com.amap.api.maps.MapsInitializer

class TravelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initAmapPrivacy()
    }

    /** 高德 SDK 隐私合规，未调用会导致 MapView 空白 */
    private fun initAmapPrivacy() {
        try {
            MapsInitializer.updatePrivacyShow(this, true, true)
            MapsInitializer.updatePrivacyAgree(this, true)
        } catch (e: Exception) {
            Log.e("TravelApplication", "Amap privacy init failed: ${e.message}", e)
        }
    }
}
