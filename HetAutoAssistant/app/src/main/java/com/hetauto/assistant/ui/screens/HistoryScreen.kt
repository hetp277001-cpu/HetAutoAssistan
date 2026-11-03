package com.hetauto.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hetauto.assistant.data.entity.TestSessionEntity
import com.hetauto.assistant.ui.theme.AccentCyan
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.CardBorder
import com.hetauto.assistant.ui.theme.DarkBackground
import com.hetauto.assistant.ui.theme.StatusGreen
import com.hetauto.assistant.ui.theme.StatusRed
import com.hetauto.assistant.ui.theme.TextMuted
import com.hetauto.assistant.ui.theme.TextPrimary
import com.hetauto.assistant.ui.theme.TextSecondary
import com.hetauto.assistant.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.allSessions.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AUTOMATION SESSIONS", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Test Execution Audit Log", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (sessions.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StatusRed)
                    Spacer(Modifier.padding(end = 4.dp))
                    Text("Clear", color = StatusRed, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No session records found in database.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Audit Logs?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all session history, logs, and screenshots from the Room database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SessionCard(session: TestSessionEntity) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    val durationSec = if (session.endTime != null) (session.endTime - session.startTime) / 1000 else 0

    val statusColor = when (session.status) {
        "COMPLETED" -> StatusGreen
        "FAILED" -> StatusRed
        "RUNNING" -> AccentCyan
        else -> TextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Session #${session.id} - ${session.workflowName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = session.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Started: ${dateFormatter.format(Date(session.startTime))}",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Repetitions: ${session.completedRepetitions} / ${if (session.targetRepetitions < 0) "∞" else session.targetRepetitions}",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Actions: ${session.totalActions}",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Duration: ${durationSec}s",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
            if (!session.failureReason.isNullOrBlank()) {
                Text(
                    text = "Failure Reason: ${session.failureReason}",
                    color = StatusRed,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
