package com.kusa.cockpitscope

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MainCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private var rpm = 0f
    private var speed = 0f
    private var throttle = 0f

    private var hasTouch = false
    private var hasTouchpad = false

    private val telemetryEmitter = TelemetryEmitter { data ->
        rpm = data.rpm
        speed = data.speed
        throttle = data.throttle
        invalidate()
    }

    init {
        lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        // Check host capabilities (Touch/Touchpad)
        // Note: This is a simplified check. In real scenarios, we might use constraints.
        hasTouch = true // Requirement Step 1: Input Touch: true
        hasTouchpad = false // Requirement Step 1: Touchpad: false

        telemetryEmitter.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        telemetryEmitter.stop()
    }

    override fun onGetTemplate(): Template {
        val rpmRow = Row.Builder()
            .setTitle("Engine RPM")
            .addText("${rpm.toInt()} RPM")
            .build()

        val speedRow = Row.Builder()
            .setTitle("Vehicle Speed")
            .addText("${speed.toInt()} km/h")
            .build()

        val throttleRow = Row.Builder()
            .setTitle("Throttle Position")
            .addText("${throttle.toInt()} %")
            .build()

        val capabilitiesRow = Row.Builder()
            .setTitle("Input Capabilities")
            .addText("Touch: $hasTouch, Touchpad: $hasTouchpad")
            .build()

        val pane = Pane.Builder()
            .addRow(rpmRow)
            .addRow(speedRow)
            .addRow(throttleRow)
            .addRow(capabilitiesRow)
            .build()

        return PaneTemplate.Builder(pane)
            .setHeader(
                Header.Builder()
                    .setStartHeaderAction(Action.APP_ICON)
                    .setTitle("Cockpit Scope Data")
                    .build()
            )
            .build()
    }
}
