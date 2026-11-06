package online.cowlsly.tome

import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private val Brass = Color(0xFFD4AF72)
private val Night = Color(0xFF17191F)
private val Runes = listOf(
    "ᚠ","ᚢ","ᚦ","ᚨ","ᚱ","ᚲ","ᚷ","ᚹ","ᚺ","ᚾ","ᛁ","ᛃ",
    "ᛇ","ᛈ","ᛉ","ᛊ","ᛏ","ᛒ","ᛖ","ᛗ","ᛚ","ᛜ","ᛞ","ᛟ",
    "ᚤ","ᚥ","ᚧ","ᚪ","ᚫ","ᚬ","ᚭ","ᚮ","ᚯ","ᚰ","ᚱ","ᛡ"
).distinct()
private val Sections = listOf("Portals", "Starred", "Shortcuts", "Secrets", "Settings")

class MainActivity : FragmentActivity() {
    private lateinit var store: BookStore
    private var unlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = BookStore(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Brass, background = Night, surface = Color(0xFF24272F),
                onSurface = Color(0xFFF3E7D5), onBackground = Color(0xFFF3E7D5)
            )) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (unlocked) {
                        TomeScreen(store,
                            onOpen = { url ->
                                startActivity(Intent(this, PortalActivity::class.java).putExtra("url", url))
                            },
                            onLock = { unlocked = false },
                            onBiometric = { enabled ->
                                if (!enabled) store.setBiometricEnabled(false)
                                else if (BiometricManager.from(this).canAuthenticate(
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                    ) == BiometricManager.BIOMETRIC_SUCCESS
                                ) store.setBiometricEnabled(true)
                            })
                    } else {
                        UnlockScreen(store, onUnlock = { unlocked = true },
                            onBiometric = { promptBiometric() })
                    }
                }
            }
        }
    }

    private fun promptBiometric() {
        if (!store.isBiometricEnabled() || !store.isConfigured()) return
        if (BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) != BiometricManager.BIOMETRIC_SUCCESS) return
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    unlocked = true
                }
            })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock the Tome")
            .setSubtitle("Biometric authentication")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build())
    }
}

@Composable
private fun UnlockScreen(
    store: BookStore,
    onUnlock: () -> Unit,
    onBiometric: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val setup = !store.isConfigured()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ᛟ", style = MaterialTheme.typography.displayLarge, color = Brass)
        Text("The Tome of Interplanar Portals", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        Text(if (setup) "Choose a six-digit PIN to seal your book" else "Enter your six-digit PIN")
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(6) },
            label = { Text("Six-digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (setup) OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it.filter(Char::isDigit).take(6) },
            label = { Text("Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(14.dp))
        Button(enabled = pin.length == 6 && (!setup || confirm.length == 6), onClick = {
            if (setup) {
                if (pin != confirm) error = "PINs do not match"
                else {
                    store.setPin(pin)
                    pin = ""; confirm = ""; onUnlock()
                }
            } else {
                if (store.checkPin(pin)) { pin = ""; onUnlock() }
                else { error = "Incorrect PIN or temporary lockout"; pin = "" }
            }
        }) { Text(if (setup) "Seal and open the Tome" else "Open the Tome") }
        if (!setup && store.isBiometricEnabled()) {
            TextButton(onClick = onBiometric) { Text("Use biometric unlock") }
        }
        Text("Beta: keep your PIN safe. Cloud recovery is not available yet.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TomeScreen(
    store: BookStore,
    onOpen: (String) -> Unit,
    onLock: () -> Unit,
    onBiometric: (Boolean) -> Unit
) {
    var entries by remember { mutableStateOf(store.load()) }
    var section by remember { mutableIntStateOf(0) }
    var sort by remember { mutableStateOf("Custom") }
    var page by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<PortalEntry?>(null) }
    var creating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val visible = entries.filter {
        when (section) {
            0 -> it.kind == "WEB"
            1 -> it.favorite
            2 -> it.kind == "SHORTCUT"
            else -> false
        }
    }.let { list ->
        when (sort) {
            "A-Z" -> list.sortedBy { it.name.lowercase() }
            "Newest" -> list.sortedByDescending { it.addedAt }
            else -> list.sortedBy { it.position }
        }
    }
    val totalPages = maxOf(1, (visible.size + 11) / 12)
    fun save(next: List<PortalEntry>) {
        try { store.save(next); entries = next; message = "" }
        catch (e: IllegalArgumentException) { message = e.message ?: "Unable to save" }
    }
    Column(Modifier.fillMaxSize().background(Night).padding(12.dp)) {
        Text("✦ THE INTERPLANAR TOME ✦",
            color = Brass, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text("Five chapters • Twelve portals per page",
            style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Sections.forEachIndexed { index, name ->
                TextButton(onClick = { section = index; page = 0 }) {
                    Text(name, color = if (index == section) Brass else Color.LightGray)
                }
            }
        }
        if (section <= 2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort:", style = MaterialTheme.typography.labelMedium)
                listOf("Custom", "Newest", "A-Z").forEach { option ->
                    TextButton(onClick = { sort = option; page = 0 }) {
                        Text(option, color = if (sort == option) Brass else Color.LightGray)
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(3.dp)
            ) {
                items(12) { slot ->
                    val entry = visible.getOrNull(page * 12 + slot)
                    Surface(
                        color = Color(0xFF292832),
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(3.dp).height(145.dp).fillMaxWidth()
                    ) {
                        if (entry == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("✧", color = Color.DarkGray,
                                    style = MaterialTheme.typography.headlineMedium)
                            }
                        } else Column(Modifier.padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(entry.rune, color = Brass, style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.clickable { onOpen(entry.url) })
                            Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clickable { onOpen(entry.url) })
                            Text(entry.tag.ifBlank { "Tap rune to open" },
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall)
                            Row {
                                TextButton(onClick = {
                                    save(entries.map {
                                        if (it.id == entry.id) it.copy(favorite = !it.favorite) else it
                                    })
                                }) { Text(if (entry.favorite) "★" else "☆") }
                                TextButton(onClick = { editing = entry }) { Text("Edit") }
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { page-- }, enabled = page > 0) { Text("‹") }
                Spacer(Modifier.width(10.dp))
                Text("Page " + (page + 1) + " / " + totalPages)
                Spacer(Modifier.width(10.dp))
                OutlinedButton(onClick = { page++ }, enabled = page + 1 < totalPages) { Text("›") }
                Spacer(Modifier.weight(1f))
                Button(onClick = { creating = true }) {
                    Text(if (section == 2) "+ Shortcut" else "+ Portal")
                }
            }
            if (sort == "Custom" && visible.isNotEmpty()) {
                Text("Tip: use Edit to change names, tags, runes and order.")
            }
        } else if (section == 3) {
            Text("ᚷ Secret tools", style = MaterialTheme.typography.headlineSmall)
            Text("APK inspect, compile, decompile and connectors are planned, not exposed in this alpha.")
            Text("These tools will require a separate developer permission boundary.")
        } else {
            Text("Book settings", style = MaterialTheme.typography.headlineSmall)
            Text("Biometric unlock: " + if (store.isBiometricEnabled()) "Enabled" else "Off")
            Row {
                Button(onClick = { onBiometric(true); message = "If supported, biometric unlock is enabled" }) {
                    Text("Enable biometric")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { onBiometric(false); message = "Biometric unlock disabled" }) {
                    Text("Disable")
                }
            }
            OutlinedButton(onClick = onLock) { Text("Lock book") }
            Text("No account passphrase reuse. Recovery and encrypted portal storage are future work.")
        }
        if (message.isNotEmpty()) Text(message, color = MaterialTheme.colorScheme.error)
    }
    if (creating || editing != null) {
        val target = editing
        PortalEditor(
            existing = target, used = entries.filter { it.id != target?.id }.map { it.rune }.toSet(),
            kind = if (section == 2) "SHORTCUT" else target?.kind ?: "WEB",
            onDismiss = { creating = false; editing = null },
            onSave = { e ->
                val next = if (target != null) entries.map { if (it.id == target.id) e else it }
                    else entries + e.copy(position = (entries.maxOfOrNull { it.position } ?: -1) + 1)
                save(next); creating = false; editing = null
            },
            onDelete = if (target != null) {{
                save(entries.filter { it.id != target.id })
                editing = null; creating = false
            }} else null
        )
    }
}

@Composable
private fun PortalEditor(
    existing: PortalEntry?,
    used: Set<String>,
    kind: String,
    onDismiss: () -> Unit,
    onSave: (PortalEntry) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var url by remember(existing?.id) { mutableStateOf(existing?.url ?: "") }
    var tag by remember(existing?.id) { mutableStateOf(existing?.tag ?: "") }
    var rune by remember(existing?.id) {
        mutableStateOf(existing?.rune ?: Runes.firstOrNull { it !in used }.orEmpty())
    }
    val valid = name.isNotBlank() && UrlPolicy.normalizeWebUrl(url) != null && rune.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Create a portal" else "Edit portal") },
        text = {
            Column {
                OutlinedTextField(name, { name = it.take(70) }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(url, { url = it.take(1000) }, label = { Text("HTTPS website URL") }, singleLine = true)
                OutlinedTextField(tag, { tag = it.take(70) }, label = { Text("Tag") }, singleLine = true)
                Text("Choose one unclaimed rune", color = Brass)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Runes.filter { it !in used }.forEach { glyph ->
                        TextButton(onClick = { rune = glyph }) {
                            Text(glyph, color = if (rune == glyph) Brass else Color.LightGray,
                                style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                if (existing != null && onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Remove this entry") }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val fixed = UrlPolicy.normalizeWebUrl(url) ?: return@TextButton
                onSave((existing ?: PortalEntry(name = name, url = fixed, rune = rune, kind = kind))
                    .copy(name = name.trim(), url = fixed, tag = tag.trim(), rune = rune))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
