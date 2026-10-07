package com.edupixel.school.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edupixel.school.ui.presentation.StatusInfo

@Composable
fun StatusBadge(
    statusInfo: StatusInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusInfo.containerColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = statusInfo.englishLabel,
            color = statusInfo.contentColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        )
    }
}
