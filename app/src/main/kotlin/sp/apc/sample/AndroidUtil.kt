package sp.apc.sample

import android.os.Build
import java.security.SecureRandom

fun secureRandom(): SecureRandom {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SecureRandom.getInstanceStrong()
    } else {
        val algorithm = "SHA1PRNG"
        SecureRandom.getInstance(algorithm)
    }
}

fun getAdditional(): String {
    return mapOf<String, String?>(
        "empty" to "",
        "null" to null,
//        "BOARD" to Build.BOARD,
        "BOOTLOADER" to Build.BOOTLOADER,
//        "CPU_ABI" to Build.CPU_ABI,
//        "DEVICE" to Build.DEVICE,
//        "DISPLAY" to Build.DISPLAY,
//        "FINGERPRINT" to Build.FINGERPRINT,
//        "HARDWARE" to Build.HARDWARE,
//        "HOST" to Build.HOST,
        "ID" to Build.ID,
        "MANUFACTURER" to Build.MANUFACTURER,
        "MODEL" to Build.MODEL
//        "PRODUCT" to Build.PRODUCT,
//        "RADIO" to Build.RADIO,
//        "SERIAL" to Build.SERIAL,
//        "SUPPORTED_ABIS" to Build.SUPPORTED_ABIS.contentToString(),
//        "SUPPORTED_32_BIT_ABIS" to Build.SUPPORTED_32_BIT_ABIS.contentToString(),
//        "SUPPORTED_64_BIT_ABIS" to Build.SUPPORTED_64_BIT_ABIS.contentToString(),
//        "TYPE" to Build.TYPE
    ).filterValues { !it.isNullOrEmpty() }
        .toList()
        .filterIsInstance<Pair<String, String>>()
//        .also { println(it.sortedBy { (key, _) -> key }.joinToString(separator = "\n")) }
        .joinToString(prefix = "additional[", separator = "_", postfix = "]") { (k, v) -> "$k:$v" }
}
