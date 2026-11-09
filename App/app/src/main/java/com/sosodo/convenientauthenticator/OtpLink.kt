package com.sosodo.convenientauthenticator

import android.net.Uri
import java.util.Locale
import androidx.core.net.toUri

//https://github.com/google/google-authenticator/wiki/Key-Uri-Format

fun parseOtpAuthLink(link: String): Entry? {
    val uri = link.toUri()

    if (!uri.isHierarchical || uri.scheme != "otpauth" || uri.authority != "totp") {
        return null
    }

    for (name in uri.queryParameterNames) {
        if (uri.getQueryParameters(name).size != 1) return null
    }

    val algorithmValue = uri.getQueryParameter("algorithm")
        ?.uppercase(Locale.ROOT)
        ?: "SHA1"

    val hashAlgorithm = HashAlgorithm.entries
        .firstOrNull { it.name == algorithmValue }
        ?: return null

    val digits = uri.getQueryParameter("digits")?.let { value ->
        value.toIntOrNull() ?: return null
    } ?: 6
    if (digits != 6 && digits != 8) return null


    val period = uri.getQueryParameter("period")?.let { value ->
        value.toIntOrNull() ?: return null
    } ?: 30

    if (period <= 0) return null

    val secret = uri.getQueryParameter("secret") ?: return null


    val label = uri.path.orEmpty().removePrefix("/")
    if (label.isBlank()) return null

    val separator = label.indexOf(':')
    val labelIssuer = if (separator >= 0) {
        label.substring(0, separator).trim()
    } else {
        null
    }
    val username = if (separator >= 0) {
        label.substring(separator + 1).trim()
    } else {
        label.trim()
    }

    if (username.isBlank() || ':' in username) return null

    val parameterIssuer = uri.getQueryParameter("issuer")?.trim()

    if (labelIssuer != null && parameterIssuer != null && labelIssuer != parameterIssuer) {
        return null
    }

    return Entry(
        secret = secret,
        issuer = parameterIssuer ?: labelIssuer.orEmpty(),
        username = username,
        period = period,
        hashAlgorithm = hashAlgorithm,
        digits = digits
    )
}

fun generateOtpAuthLink(entry: Entry): String {
    val issuer = entry.issuer
    val username = entry.username

    require(username.isNotBlank()) {
        "Не указано имя аккаунта"
    }

    val label = if (issuer.isEmpty()) {
        username
    } else {
        "$issuer:$username"
    }

    return Uri.Builder()
        .scheme("otpauth")
        .authority("totp")
        .appendPath(label)
        .appendQueryParameter("secret", entry.secret)
        .apply {
            if (issuer.isNotEmpty()) {
                appendQueryParameter("issuer", issuer)
            }
        }
        .appendQueryParameter("algorithm", entry.hashAlgorithm.name)
        .appendQueryParameter("digits", entry.digits.toString())
        .appendQueryParameter("period", entry.period.toString())
        .build()
        .toString()
}
