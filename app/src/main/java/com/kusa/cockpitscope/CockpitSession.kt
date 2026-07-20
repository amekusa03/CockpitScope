package com.kusa.cockpitscope

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class CockpitSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return MainCarScreen(carContext)
    }
}
