package online.cowlsly.tome

import org.junit.Assert.*
import org.junit.Test

class UrlPolicyTest {
    @Test fun defaultsToHttps() {
        assertEquals("https://example.com/", UrlPolicy.normalizeWebUrl("example.com/"))
    }
    @Test fun acceptsSecureSite() {
        assertEquals("https://example.org/path", UrlPolicy.normalizeWebUrl("https://example.org/path"))
    }
    @Test fun rejectsInsecureAndDangerousSchemes() {
        listOf("http://example.com", "javascript:alert(1)", "data:text/html,hi", "file:///a",
            "content://authority/data", "intent://something", "").forEach {
            assertNull(it, UrlPolicy.normalizeWebUrl(it))
        }
    }
    @Test fun rejectsEmbeddedCredentials() {
        assertNull(UrlPolicy.normalizeWebUrl("https://person:password@example.com/"))
    }
}
