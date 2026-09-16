package com.suncheng.onsite.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("onsite", Context.MODE_PRIVATE)

    var onboardingDone: Boolean
        get() = sp.getBoolean(KEY_ONBOARDING, false)
        set(value) {
            sp.edit().putBoolean(KEY_ONBOARDING, value).apply()
        }

    companion object {
        private const val KEY_ONBOARDING = "onboarding_done"
    }
}
