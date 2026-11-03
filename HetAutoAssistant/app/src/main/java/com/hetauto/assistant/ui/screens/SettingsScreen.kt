package com.hetauto.assistant.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hetauto.assistant.ui.theme.AccentCyan
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.CardBorder
import com.hetauto.assistant.ui.theme.DarkBackground
import com.hetauto.assistant.ui.theme.PrimaryBlue
import com.hetauto.assistant.ui.theme.StatusGreen
import com.hetauto.assistant.ui.theme.StatusRed
import com.hetauto.assistant.ui.theme.TextPrimary
import com.hetauto.assistant.ui.theme.TextSecondary
import com.hetauto.assistant.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("SETTINGS & SECURITY", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Permissions & Compliance Guardrails", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Text("SYSTEM PERMISSIONS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        PermissionItemCard(
            title = "Accessibility Automation Service",
            description = "Enables UI element inspection, clicking, and gestures in developer test screens.",
            isGranted = isAccessibilityActive,
            onAction = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        val canDrawOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
        PermissionItemCard(
            title = "Floating Control Panel Overlay",
            description = "Allows the movable floating control dashboard to sit above test applications.",
            isGranted = canDrawOverlay,
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            }
        )

        Text("SECURITY & ETHICAL GUARDRAILS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SecurityItem("Authorized Testing Only", "The assistant is designed exclusively for developer-owned applications and authorized test environments.")
                SecurityItem("No Ad Fraud / No Bypass", "Strictly forbids and blocks automated interaction with ads, rewarded video claims, or monetization elements.")
                SecurityItem("Zero Fake Accounts", "Automated account registrations, CAPTCHA bypasses, and fake account generation are strictly prohibited.")
                SecurityItem("Credential Privacy", "Passwords, PINs, OTPs, and authentication tokens are never recorded, logged, or collected.")
                SecurityItem("Manual Confirmation Gate", "External app installations and account authentications require explicit manual human confirmation.")
            }
        }

        Text("SYSTEM ARCHITECTURE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ArchItem("Package", "com.hetauto.assistant")
                ArchItem("Target SDK", "34 (Android 14) / Min SDK 26 (Android 8.0)")
                ArchItem("UI Framework", "Jetpack Compose + Material 3")
                ArchItem("Database", "SQLite via Android Jetpack Room 2.6.1")
                ArchItem("Vision Engine", "Google ML Kit On-Device Text Recognition")
                ArchItem("Asynchrony", "Kotlin Coroutines & Reactive StateFlow")
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = if (isGranted) "ACTIVE" else "NOT GRANTED",
                    color = if (isGranted) StatusGreen else StatusRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(text = description, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isGranted) Color(0xFF334155) else PrimaryBlue)
            ) {
                Text(if (isGranted) "Reconfigure" else "Grant Permission", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SecurityItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Security, contentDescription = null, tint = AccentCyan, modifier = Modifier.padding(top = 2.dp).padding(end = 8.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(desc, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ArchItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp)
    }
}
