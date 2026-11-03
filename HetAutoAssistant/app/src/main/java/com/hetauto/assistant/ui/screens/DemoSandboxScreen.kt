package com.hetauto.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hetauto.assistant.ui.theme.AccentCyan
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.CardBorder
import com.hetauto.assistant.ui.theme.DarkBackground
import com.hetauto.assistant.ui.theme.PrimaryBlue
import com.hetauto.assistant.ui.theme.StatusGreen
import com.hetauto.assistant.ui.theme.TextPrimary
import com.hetauto.assistant.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DemoSandboxScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var testBalance by remember { mutableStateOf("$1,250.00") }
    var selectedBoxTitle by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var outcomeMessage by remember { mutableStateOf("Ready for automation test cycles") }
    var successfulPassCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DEVELOPER DEMO TEST TARGET",
            color = AccentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Authorized Test Account Dashboard",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "Test Balance: $testBalance",
                    color = StatusGreen,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Target ID: TEST-ENV-ALPHA-01",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Four-Box Test Sandbox Menu", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Navigates to the 4-box automated testing grid", color = TextSecondary, fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        outcomeMessage = "Menu item selected. Navigated to Four-Box Test."
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Four-Box Test", fontSize = 12.sp)
                }
            }
        }

        Text(
            text = "DEMO FOUR-BOX INTERACTIVE GRID",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val boxes = listOf(
            Pair("Box 1: Network Check", "Verify server connectivity"),
            Pair("Box 2: Cache Refresh", "Flush local memory caches"),
            Pair("Box 3: Database Sync", "Sync test records"),
            Pair("Box 4: Security Assert", "Validate token signatures")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DemoTestBox(
                    title = boxes[0].first,
                    subtitle = boxes[0].second,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedBoxTitle = boxes[0].first
                        showConfirmDialog = true
                    }
                )
                DemoTestBox(
                    title = boxes[1].first,
                    subtitle = boxes[1].second,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedBoxTitle = boxes[1].first
                        showConfirmDialog = true
                    }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DemoTestBox(
                    title = boxes[2].first,
                    subtitle = boxes[2].second,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedBoxTitle = boxes[2].first
                        showConfirmDialog = true
                    }
                )
                DemoTestBox(
                    title = boxes[3].first,
                    subtitle = boxes[3].second,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedBoxTitle = boxes[3].first
                        showConfirmDialog = true
                    }
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = AccentCyan)
                    Spacer(Modifier.size(8.dp))
                    Text("Test State Outcome", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.height(6.dp))
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentCyan, strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                        Text("Processing test action...", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    Text(text = outcomeMessage, color = TextPrimary, fontSize = 13.sp)
                }
                Text(
                    text = "Successful passes in this sandbox: $successfulPassCount",
                    color = StatusGreen,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(text = "Confirm Action", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = "Are you sure you want to execute $selectedBoxTitle?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true
                        scope.launch {
                            delay(1000)
                            isLoading = false
                            successfulPassCount++
                            outcomeMessage = "Expected test result appeared: $selectedBoxTitle completed with SUCCESS status."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DemoTestBox(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                Spacer(Modifier.size(4.dp))
                Text("Select Box", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
