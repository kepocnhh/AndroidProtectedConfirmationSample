package sp.apc.sample

import android.content.Context
import android.provider.Settings
import android.widget.Toast

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.getDeviceId(): String {
    val result = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    check(!result.isNullOrEmpty())
    return result
}
