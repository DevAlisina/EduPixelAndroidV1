package com.edupixel.school.ui.presentation

import androidx.compose.ui.graphics.Color

data class StatusInfo(
    val englishLabel: String,
    val rawValue: String,
    val containerColor: Color,
    val contentColor: Color
)

object StatusPresentation {

    fun formatMidtermResult(raw: String?): StatusInfo {
        if (raw.isNullOrBlank()) {
            return StatusInfo(
                englishLabel = "Pending / Not Entered",
                rawValue = "",
                containerColor = Color(0xFF64748B).copy(alpha = 0.2f),
                contentColor = Color(0xFF94A3B8)
            )
        }

        return when (raw.trim()) {
            "موفق" -> StatusInfo(
                englishLabel = "Successful (موفق)",
                rawValue = raw,
                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                contentColor = Color(0xFF10B981)
            )
            "تلاش بیشتر" -> StatusInfo(
                englishLabel = "Needs More Effort (تلاش بیشتر)",
                rawValue = raw,
                containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                contentColor = Color(0xFFF59E0B)
            )
            "معذرتي", "معذرتی" -> StatusInfo(
                englishLabel = "Excused (معذرتی)",
                rawValue = raw,
                containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                contentColor = Color(0xFF3B82F6)
            )
            "غایب" -> StatusInfo(
                englishLabel = "Absent (غایب)",
                rawValue = raw,
                containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                contentColor = Color(0xFFEF4444)
            )
            else -> StatusInfo(
                englishLabel = raw,
                rawValue = raw,
                containerColor = Color(0xFF6B7280).copy(alpha = 0.2f),
                contentColor = Color(0xFFE2E8F0)
            )
        }
    }

    fun formatAnnualResult(raw: String?): StatusInfo {
        if (raw.isNullOrBlank()) {
            return StatusInfo(
                englishLabel = "Pending / In Progress",
                rawValue = "",
                containerColor = Color(0xFF64748B).copy(alpha = 0.2f),
                contentColor = Color(0xFF94A3B8)
            )
        }

        return when (raw.trim()) {
            "ارتقا صنف" -> StatusInfo(
                englishLabel = "Promoted (ارتقا صنف)",
                rawValue = raw,
                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                contentColor = Color(0xFF10B981)
            )
            "تکرار صنف" -> StatusInfo(
                englishLabel = "Repeat Grade (تکرار صنف)",
                rawValue = raw,
                containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                contentColor = Color(0xFFEF4444)
            )
            "مشروط" -> StatusInfo(
                englishLabel = "Conditional (مشروط)",
                rawValue = raw,
                containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                contentColor = Color(0xFFF59E0B)
            )
            "معذرتی", "معذرتي" -> StatusInfo(
                englishLabel = "Excused (معذرتی)",
                rawValue = raw,
                containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                contentColor = Color(0xFF3B82F6)
            )
            "محروم" -> StatusInfo(
                englishLabel = "Disqualified / Banned (محروم)",
                rawValue = raw,
                containerColor = Color(0xFFDC2626).copy(alpha = 0.2f),
                contentColor = Color(0xFFDC2626)
            )
            "سه پارچه" -> StatusInfo(
                englishLabel = "Transferred (سه پارچه)",
                rawValue = raw,
                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                contentColor = Color(0xFF8B5CF6)
            )
            else -> StatusInfo(
                englishLabel = raw,
                rawValue = raw,
                containerColor = Color(0xFF6B7280).copy(alpha = 0.2f),
                contentColor = Color(0xFFE2E8F0)
            )
        }
    }

    fun formatGrade(raw: String?): StatusInfo {
        if (raw.isNullOrBlank()) {
            return StatusInfo(
                englishLabel = "-",
                rawValue = "",
                containerColor = Color(0xFF64748B).copy(alpha = 0.15f),
                contentColor = Color(0xFF94A3B8)
            )
        }

        return when (raw.trim()) {
            "الف" -> StatusInfo(
                englishLabel = "A (الف)",
                rawValue = raw,
                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                contentColor = Color(0xFF10B981)
            )
            "ب" -> StatusInfo(
                englishLabel = "B (ب)",
                rawValue = raw,
                containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f),
                contentColor = Color(0xFF3B82F6)
            )
            "ج" -> StatusInfo(
                englishLabel = "C (ج)",
                rawValue = raw,
                containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                contentColor = Color(0xFFF59E0B)
            )
            "د" -> StatusInfo(
                englishLabel = "D (د)",
                rawValue = raw,
                containerColor = Color(0xFFEA580C).copy(alpha = 0.2f),
                contentColor = Color(0xFFEA580C)
            )
            "هـ", "ه" -> StatusInfo(
                englishLabel = "E / Fail (هـ)",
                rawValue = raw,
                containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                contentColor = Color(0xFFEF4444)
            )
            "اشتباه" -> StatusInfo(
                englishLabel = "Invalid / Error (اشتباه)",
                rawValue = raw,
                containerColor = Color(0xFFEF4444).copy(alpha = 0.25f),
                contentColor = Color(0xFFF87171)
            )
            else -> StatusInfo(
                englishLabel = raw,
                rawValue = raw,
                containerColor = Color(0xFF64748B).copy(alpha = 0.2f),
                contentColor = Color(0xFFE2E8F0)
            )
        }
    }
}
