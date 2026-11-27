package com.antoniowalls.ava

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AvaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.USE_CLOUD_FIREBASE) return

        val firebase = FirebaseApp.initializeApp(
            this,
            FirebaseOptions.Builder()
                .setProjectId("demo-ava-local")
                .setApplicationId("1:1234567890:android:0000000000000000")
                .setApiKey("local-emulator-only")
                .setStorageBucket("demo-ava-local.appspot.com")
                .build()
        )
        FirebaseAuth.getInstance(firebase).useEmulator(EMULATOR_HOST, 9099)
        FirebaseFirestore.getInstance(firebase).useEmulator(EMULATOR_HOST, 8080)
        FirebaseStorage.getInstance(firebase).useEmulator(EMULATOR_HOST, 9199)
    }

    private companion object {
        const val EMULATOR_HOST = "10.0.2.2"
    }
}
