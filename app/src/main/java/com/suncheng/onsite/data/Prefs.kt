package com.suncheng.onsite.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("onsite", Context.MODE_PRIVATE)

    var onboardingDone: Boolean
        get() = sp.getBoolean(KEY_ONBOARDING, false)
        set(value) {
            sp.edit().putBoolean(KEY_ONBOARDING, value).apply()
        }

    fun wasArrived(noteId: String): Boolean = arrivedIds().contains(noteId)

    fun markArrived(noteId: String) {
        sp.edit().putStringSet(KEY_ARRIVED, arrivedIds() + noteId).apply()
    }

    fun clearArrived(noteId: String) {
        sp.edit().putStringSet(KEY_ARRIVED, arrivedIds() - noteId).apply()
    }

    private fun arrivedIds(): Set<String> = sp.getStringSet(KEY_ARRIVED, emptySet())?.toSet().orEmpty()

    companion object {
        private const val KEY_ONBOARDING = "onboarding_done"
        private const val KEY_ARRIVED = "arrived_ids"
    }
}
