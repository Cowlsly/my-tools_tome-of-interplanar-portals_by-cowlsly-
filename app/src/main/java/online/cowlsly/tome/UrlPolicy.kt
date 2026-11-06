package online.cowlsly.tome

import java.net.URI

/**
 * Adapted from WebToApp UrlSafety.kt (Unlicense, shiahonb777/web-to-app).
 * Tome deliberately defaults to HTTPS and denies embedded insecure URLs.
 */
object UrlPolicy {
    fun normalizeWebUrl(input: String): String? {
        val raw = input.trim()
        if (raw.isEmpty() || raw.any { it.isWhitespace() }) return null
        val candidate = if (raw.contains("://") || raw.contains(":")) raw else "https://" + raw
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.host.isNullOrBlank() || uri.rawUserInfo != null) return null
        if (uri.port > 65535 || uri.port == 0) return null
        return uri.normalize().toASCIIString()
    }
}
