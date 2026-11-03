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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.hetauto.assistant.overlay.FloatingPanelService
import com.hetauto.assistant.ui.components.ActionControlButtons
import com.hetauto.assistant.ui.components.LogViewer
import com.hetauto.assistant.ui.components.MetricCard
import com.hetauto.assistant.ui.components.StatusChip
import com.hetauto.assistant.ui.theme.AccentCyan
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.CardBorder
import com.hetauto.assistant.ui.theme.DarkBackground
import com.hetauto.assistant.ui.theme.PrimaryBlue
import com.hetauto.assistant.ui.theme.StatusAmber
import com.hetauto.assistant.ui.theme.StatusGreen
import com.hetauto.assistant.ui.theme.StatusRed
import com.hetauto.assistant.ui.theme.TextMuted
import com.hetauto.assistant.ui.theme.TextPrimary
import com.hetauto.assistant.ui.theme.TextSecondary
import com.hetauto.assistant.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val state by viewModel.automationState.collectAsState()
    val currentRep by viewModel.currentRepetition.collectAsState()
    val targetReps by viewModel.targetRepetitions.collectAsState()
    val isContinuous by viewModel.isContinuousMode.collectAsState()
    val completedActions by viewModel.completedActions.collectAsState()
    val remainingActions by viewModel.remainingActions.collectAsState()
    val durationText by viewModel.sessionDurationFormatted.collectAsState()
    val currentStage by viewModel.currentStage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val selectedReps by viewModel.selectedReps.collectAsState()
    val continuousToggle by viewModel.continuousToggle.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HET AUTO ASSISTANT",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Developer UI Automation Engine",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            StatusChip(state = state)
        }

        if (!isAccessibilityActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Service Disabled", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Required for UI automation and screen interaction.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Enable", fontSize = 11.sp)
                    }
                }
            }
        }

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33F59E0B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAmber)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CURRENT WORKFLOW STAGE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = currentStage?.displayName ?: "No Stage Active (Idle)",
                    color = if (currentStage != null) AccentCyan else TextMuted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = if (isContinuous) "Mode: Continuous Testing" else "Repetition: $currentRep of $targetReps",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Session Duration",
                value = durationText,
                modifier = Modifier.weight(1f),
                highlightColor = AccentCyan
            )
            MetricCard(
                title = "Current Repetition",
                value = if (isContinuous) "$currentRep (∞)" else "$currentRep / $targetReps",
                modifier = Modifier.weight(1f),
                highlightColor = PrimaryBlue
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                title = "Completed Actions",
                value = completedActions.toString(),
                modifier = Modifier.weight(1f),
                highlightColor = StatusGreen
            )
            MetricCard(
                title = "Remaining Actions",
                value = if (isContinuous) "Continuous" else remainingActions.toString(),
                modifier = Modifier.weight(1f),
                highlightColor = StatusAmber
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(14.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "TEST CYCLE CONFIGURATION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 5, 10, 25).forEach { count ->
                    FilterChip(
                        selected = selectedReps == count && !continuousToggle,
                        onClick = {
                            viewModel.setSelectedReps(count)
                            viewModel.setContinuousMode(false)
                        },
                        label = { Text("$count reps") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                FilterChip(
                    selected = continuousToggle,
                    onClick = { viewModel.setContinuousMode(!continuousToggle) },
                    label = { Text("Continuous") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        ActionControlButtons(
            state = state,
            onStart = { viewModel.startAutomation() },
            onPause = { viewModel.pauseAutomation() },
            onResume = { viewModel.resumeAutomation() },
            onStop = { viewModel.stopAutomation() }
        )

        OutlinedButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } else {
                    FloatingPanelService.start(context)
                }
            },
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Layers, contentDescription = null, tint = AccentCyan)
            Spacer(Modifier.width(8.dp))
            Text("Launch Floating Control Panel Overlay", color = TextPrimary, fontSize = 13.sp)
        }

        LogViewer(logs = logs)
    }
}
