package com.huntmate.app.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huntmate.app.ui.components.HuntmateTextField
import com.huntmate.app.ui.components.HuntmateColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.sessionState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HuntmateColors.Navy)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A3A52))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(140.dp)
                .clip(CircleShape)
                .background(HuntmateColors.Amber.copy(alpha = 0.13f))
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, HuntmateColors.Amber.copy(alpha = 0.45f), CircleShape)
                        .background(HuntmateColors.Amber.copy(alpha = 0.12f))
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text("o  Huntmate", color = HuntmateColors.Amber, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Find your next travel partner",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    "Connect with real travelers heading to the same places. Share moments, plan together, explore more.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.58f)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Match by destination", "Share trip stories", "Chat safely").forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuthSegmentedControl(state.authMode, viewModel::toggleAuthMode)
                HuntmateTextField(
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Email address"
                )
                HuntmateTextField(
                    value = state.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Password"
                )
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HuntmateColors.Navy)
                ) {
                    Text(if (state.authMode == AuthMode.SIGN_IN) "Sign in" else "Create account")
                }
                Text(
                    text = if (state.authMode == AuthMode.SIGN_IN) "Don't have an account? Create one" else "Already have an account? Sign in",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = HuntmateColors.MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AuthSegmentedControl(
    mode: AuthMode,
    onToggle: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF2F4F7))
            .padding(4.dp)
    ) {
        AuthSegment("Sign in", selected = mode == AuthMode.SIGN_IN, modifier = Modifier.weight(1f), onClick = {
            if (mode != AuthMode.SIGN_IN) onToggle()
        })
        AuthSegment("Create account", selected = mode == AuthMode.SIGN_UP, modifier = Modifier.weight(1f), onClick = {
            if (mode != AuthMode.SIGN_UP) onToggle()
        })
    }
}

@Composable
private fun AuthSegment(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) HuntmateColors.Navy else Color.Transparent,
            contentColor = if (selected) Color.White else HuntmateColors.MutedText
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
