package com.example.presentation.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.engine.UsageStatsHelper
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalBorderSubtle
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) } // 4-step wizard
    val totalSteps = 4

    var hasUsagePermission by remember { mutableStateOf(UsageStatsHelper.hasUsageStatsPermission(context)) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Permission result
    }

    LaunchedEffect(Unit) {
        hasUsagePermission = UsageStatsHelper.hasUsageStatsPermission(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Step Indicator & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..totalSteps).forEach { step ->
                    Box(
                        modifier = Modifier
                            .size(if (step == currentStep) 24.dp else 10.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (step == currentStep) CyanAccent
                                else if (step < currentStep) EmeraldSuccess
                                else Color(0xFF27272A)
                            )
                    )
                    if (step < totalSteps) Spacer(modifier = Modifier.width(6.dp))
                }
            }

            TextButton(
                onClick = { onComplete() },
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip Instructions (এড়িয়ে যান)",
                    fontSize = 12.sp,
                    color = Zinc400,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Center Step Content Animation
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboarding_step_animation",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
        ) { step ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    when (step) {
                        1 -> Step1Welcome()
                        2 -> Step2Permissions(
                            context = context,
                            hasUsagePermission = hasUsagePermission,
                            onGrantUsage = {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            },
                            onGrantAccessibility = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                            onGrantNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                        3 -> Step3AppBlockingInstructions()
                        4 -> Step4SchedulesAndSecurityInstructions()
                    }
                }
            }
        }

        // Bottom Wizard Controls (Back / Next / Finish)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back (পূর্ববর্তী)", fontSize = 13.sp)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (currentStep < totalSteps) "Next (পরবর্তী)" else "Get Started (শুরু করুন)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStep < totalSteps) Icons.Default.ArrowForward else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun Step1Welcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(Color(0xFF18181B))
                .border(1.dp, MinimalBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.focus_lock_icon_1787572031797),
                contentDescription = "FocusLock Logo",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to FocusLock",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "আপনার ফোনের অতিরিক্ত ব্যবহার রোধ করতে ও কাজের মনোযোগ বাড়াতে FocusLock এ আপনাকে স্বাগতম।",
            fontSize = 13.sp,
            color = Zinc400,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoBoxCard(
            title = "স্মার্ট অ্যাপ ট্র্যাকিং",
            desc = "দৈনিক ও মাসিক কত সময় কোন অ্যাপ ব্যবহার করছেন তা নিখুঁতভাবে দেখতে পারবেন।",
            icon = Icons.Default.DataUsage,
            accent = CyanAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBoxCard(
            title = "অটোমেটিক শিডিউল লক",
            desc = "নির্দিষ্ট সময়ের জন্য ফেসবুক, গেমস ও অন্যান্য অ্যাপস স্বয়ংক্রিয়ভাবে লক করে রাখুন।",
            icon = Icons.Default.Schedule,
            accent = VioletAccent
        )
    }
}

@Composable
private fun Step2Permissions(
    context: Context,
    hasUsagePermission: Boolean,
    onGrantUsage: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onGrantNotification: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            text = "প্রয়োজনীয় পারমিশন সেটআপ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "FocusLock সঠিকভাবে অ্যাপ লক এবং ব্যবহারের সময় গণনা করার জন্য নিচের পারমিশনগুলো প্রদান করুন।",
            fontSize = 13.sp,
            color = Zinc400,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        PermissionCard(
            title = "১. Usage Access (ইউসেজ অ্যাক্সেস)",
            description = "আপনার অ্যাপ ব্যবহারের সময়সীমা গণনা করার জন্য প্রয়োজনীয়।",
            icon = Icons.Default.DataUsage,
            isGranted = hasUsagePermission,
            onGrantClick = onGrantUsage
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "২. Accessibility Service (লক প্রোটেকশন)",
            description = "লক করা অ্যাপ খুললেই সাথে সাথে ব্লকিং স্ক্রিন দেখানোর জন্য প্রয়োজন।",
            icon = Icons.Default.AccessibilityNew,
            isGranted = false,
            onGrantClick = onGrantAccessibility
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionCard(
            title = "৩. Notification Alert (নোটিফিকেশন)",
            description = "সঠিক সময়ে রিমাইন্ডার এবং সুরক্ষা স্ট্যাটাস অ্যালার্ট পাঠাতে সাহায্য করবে।",
            icon = Icons.Default.Notifications,
            isGranted = false,
            onGrantClick = onGrantNotification
        )
    }
}

@Composable
private fun Step3AppBlockingInstructions() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            text = "অ্যাপ ব্লক ও লিমিট নির্দেশিকা",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "সহজ উপায়ে কীভাবে অ্যাপ ব্লক করবেন এবং সরাসরি চালু করবেন তা নিচে দেখে নিন:",
            fontSize = 13.sp,
            color = Zinc400,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        InfoBoxCard(
            title = "১. সুইচ অন করে ইনস্ট্যান্ট ব্লক",
            desc = "Apps ফিল্টারে গিয়ে যেকোনো অ্যাপের পাসের সুইচে ট্যাপ করলে তা সাথে সাথে ব্লক হয়ে যাবে।",
            icon = Icons.Default.AppBlocking,
            accent = CyanAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBoxCard(
            title = "২. ডেইলি ইউসেজ লিমিট সেট করুন",
            desc = "Slider দিয়ে অ্যাপভেদে ৩০ মিনিট, ১ ঘন্টা বা ২ ঘন্টা লিমিট সেট করতে পারবেন। সীমা পার হলেই লক হয়ে যাবে।",
            icon = Icons.Default.Lock,
            accent = VioletAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBoxCard(
            title = "৩. অ্যাপের ভেতর থেকেই সরাসরি ওপেন",
            desc = "FocusLock অ্যাপের প্রতিটি কার্ডে থাকা Play/Open (ওপেন) বাটনে ক্লিক করে সরাসরি অন্য অ্যাপস ওপেন করতে পারবেন।",
            icon = Icons.Default.Launch,
            accent = EmeraldSuccess
        )
    }
}

@Composable
private fun Step4SchedulesAndSecurityInstructions() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            text = "শিডিউল ও পিন লক সুরক্ষা",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "কাজের সময় ও ঘুমের সময় অ্যাপ লক করা এবং নিজের সেট করা নিয়ম সুরক্ষিত রাখুন।",
            fontSize = 13.sp,
            color = Zinc400,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        InfoBoxCard(
            title = "১. সময়ভিত্তিক শিডিউল রুল",
            desc = "Schedules সেকশনে প্রতিদিনের নির্দিষ্ট সময়সূচি ঠিক করুন, যেমন রাত ১০টা থেকে সকাল ৬টা পর্যন্ত সামাজিক যোগাযোগ মাধ্যম লক রাখা।",
            icon = Icons.Default.Schedule,
            accent = VioletAccent
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBoxCard(
            title = "২. পিন কোড সিকিউরিটি",
            desc = "Settings থেকে পিন লক চালু করুন যাতে অন্য কেউ অথবা আপনি নিজে হুট করে নিয়ম পরিবর্তন বা অ্যাপ আনইনস্টল করতে না পারেন।",
            icon = Icons.Default.Security,
            accent = CyanAccent
        )
    }
}

@Composable
private fun InfoBoxCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MinimalBorderSubtle, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f))
                    .border(1.dp, accent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = desc, fontSize = 12.sp, color = Zinc400, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isGranted) Color(0x3310B981) else MinimalBorderSubtle,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) EmeraldSuccess.copy(alpha = 0.12f) else CyanAccent.copy(alpha = 0.12f))
                    .border(1.dp, if (isGranted) Color(0x2210B981) else Color(0x2206B6D4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldSuccess else CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Zinc400,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Text(
                    text = "Ready ✓",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldSuccess
                )
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27272A),
                        contentColor = CyanAccent
                    )
                ) {
                    Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
