package sp.apc.sample

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager

fun Context.canAuthenticate(authenticators: Int): Int {
    val manager = BiometricManager.from(this)
    return manager.canAuthenticate(authenticators)
}

fun Context.isBiometricSuccess(authenticators: Int): Boolean {
    val code = canAuthenticate(authenticators)
    return code == BiometricManager.BIOMETRIC_SUCCESS
}

fun Context.getSharedPreferences(): SharedPreferences {
    return getSharedPreferences(
        "${BuildConfig.APPLICATION_ID}.preferences",
        Context.MODE_PRIVATE
    ) ?: error("Shared preferences does not exist!")
}
