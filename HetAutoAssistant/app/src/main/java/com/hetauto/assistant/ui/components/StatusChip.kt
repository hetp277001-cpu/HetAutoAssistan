package com.hetauto.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hetauto.assistant.loop.AutomationState
import com.hetauto.assistant.ui.theme.StatusAmber
import com.hetauto.assistant.ui.theme.StatusGreen
import com.hetauto.assistant.ui.theme.StatusRed
import com.hetauto.assistant.ui.theme.TextMuted

@Composable
fun StatusChip(state: AutomationState, modifier: Modifier = Modifier) {
    val (bgColor, dotColor, label) = when (state) {
        AutomationState.RUNNING -> Triple(Color(0x2222C55E), StatusGreen, "RUNNING")
        AutomationState.PAUSED -> Triple(Color(0x22F59E0B), StatusAmber, "PAUSED")
        AutomationState.STOPPED -> Triple(Color(0x2264748B), TextMuted, "STOPPED")
        AutomationState.COMPLETED -> Triple(Color(0x2206B6D4), Color(0xFF06B6D4), "COMPLETED")
        AutomationState.ERROR -> Triple(Color(0x22EF4444), StatusRed, "ERROR")
        AutomationState.IDLE -> Triple(Color(0x2264748B), TextMuted, "IDLE")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = label,
            color = dotColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
