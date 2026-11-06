package online.cowlsly.tome

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PortalEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val tag: String = "",
    val rune: String,
    val kind: String = "WEB",
    val favorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val position: Int = 0
)

class BookStore(context: Context) {
    private val prefs = context.getSharedPreferences("portal_book_v1", Context.MODE_PRIVATE)

    fun isConfigured(): Boolean = prefs.contains("pin_hash")
    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric", false)
    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean("biometric", enabled).apply()

    fun setPin(pin: String) {
        require(pin.length == 6 && pin.all { it in '0'..'9' })
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(pin, salt)
        prefs.edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .putInt("failures", 0)
            .putLong("blocked_until", 0)
            .apply()
    }

    fun checkPin(pin: String): Boolean {
        if (System.currentTimeMillis() < prefs.getLong("blocked_until", 0L)) return false
        val saltB64 = prefs.getString("pin_salt", null) ?: return false
        val hashB64 = prefs.getString("pin_hash", null) ?: return false
        val valid = runCatching {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val expected = Base64.decode(hashB64, Base64.NO_WRAP)
            MessageDigest.isEqual(expected, derive(pin, salt))
        }.getOrDefault(false)
        if (valid) {
            prefs.edit().putInt("failures", 0).putLong("blocked_until", 0).apply()
        } else {
            val count = prefs.getInt("failures", 0) + 1
            val delay = if (count >= 5) 60_000L else 0L
            prefs.edit().putInt("failures", count)
                .putLong("blocked_until", System.currentTimeMillis() + delay).apply()
        }
        return valid
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val chars = pin.toCharArray()
        val spec = PBEKeySpec(chars, salt, 210_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword(); chars.fill('\\u0000') }
    }

    fun load(): List<PortalEntry> = runCatching {
        val arr = JSONArray(prefs.getString("entries", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            PortalEntry(
                id = o.getString("id"),
                name = o.getString("name"),
                url = o.getString("url"),
                tag = o.optString("tag"),
                rune = o.getString("rune"),
                kind = o.optString("kind", "WEB"),
                favorite = o.optBoolean("favorite"),
                addedAt = o.optLong("addedAt"),
                position = o.optInt("position", i)
            )
        }
    }.getOrDefault(emptyList())

    fun save(entries: List<PortalEntry>) {
        require(entries.map { it.id }.distinct().size == entries.size) { "Duplicate portal ID" }
        require(entries.map { it.rune }.distinct().size == entries.size) { "A rune may be assigned only once" }
        require(entries.all { UrlPolicy.normalizeWebUrl(it.url) != null }) { "HTTPS URLs only" }
        require(entries.all { it.kind == "WEB" || it.kind == "SHORTCUT" }) { "Unknown entry type" }
        val arr = JSONArray()
        entries.forEach { entry ->
            arr.put(JSONObject()
                .put("id", entry.id)
                .put("name", entry.name)
                .put("url", entry.url)
                .put("tag", entry.tag)
                .put("rune", entry.rune)
                .put("kind", entry.kind)
                .put("favorite", entry.favorite)
                .put("addedAt", entry.addedAt)
                .put("position", entry.position))
        }
        prefs.edit().putString("entries", arr.toString()).apply()
    }
}
