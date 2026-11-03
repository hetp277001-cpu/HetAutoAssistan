package com.hetauto.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.CardBorder
import com.hetauto.assistant.ui.theme.TextPrimary
import com.hetauto.assistant.ui.theme.TextSecondary

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = TextPrimary,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = highlightColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
