package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500

@Composable
fun FirstTimeGuideOverlay(
    onDismissOrSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissOrSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, MinimalBorder, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121215))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with Title & Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QUICK GUIDANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "FocusLock কীভাবে কাজ করে",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF27272A))
                            .clickable { onDismissOrSkip() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Skip (এড়িয়ে যান)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Zinc400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Guide Item 1: App Blocking & Limits
                GuideTipRow(
                    icon = Icons.Default.AppBlocking,
                    iconTint = CyanAccent,
                    title = "১. অ্যাপস লক এবং ব্যবহারের সময়সীমা",
                    description = "Apps ট্যাবে গিয়ে পছন্দমতো অ্যাপসের সুইচ অন করুন অথবা দৈনিক কত মিনিট ব্যবহার করতে চান (Limit) তা নির্দিষ্ট করুন।"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Guide Item 2: Automated Schedules
                GuideTipRow(
                    icon = Icons.Default.Schedule,
                    iconTint = VioletAccent,
                    title = "২. অটোমেটিক শিডিউল ব্লক",
                    description = "Schedules ট্যাবে গিয়ে পড়াশোনা বা ঘুমের নির্ধারিত সময় তৈরি করুন। ওই সময়ে নির্দিষ্ট অ্যাপস স্বয়ংক্রিয়ভাবে লক হয়ে যাবে।"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Guide Item 3: Direct App Launcher from Inside
                GuideTipRow(
                    icon = Icons.Default.Launch,
                    iconTint = EmeraldSuccess,
                    title = "৩. অ্যাপসের ভেতর থেকে সরাসরি ওপেন",
                    description = "FocusLock অ্যাপসের ভেতরে থাকা 'Open App (ওপেন)' বাটনে ট্যাপ করে যেকোনো ইনস্টলড অ্যাপস সরাসরি চালু করতে পারবেন।"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissOrSkip) {
                        Text(text = "Skip Guide", color = Zinc400, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismissOrSkip,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Got It (বুঝেছি)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideTipRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF18181B))
            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f))
                .border(1.dp, iconTint.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Zinc400,
                lineHeight = 16.sp
            )
        }
    }
}
