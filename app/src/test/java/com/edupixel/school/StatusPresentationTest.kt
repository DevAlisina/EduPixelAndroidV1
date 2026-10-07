package com.edupixel.school

import com.edupixel.school.ui.presentation.StatusPresentation
import org.junit.Assert.*
import org.junit.Test

class StatusPresentationTest {

    @Test
    fun testMidtermResultPresentation() {
        val successful = StatusPresentation.formatMidtermResult("موفق")
        assertTrue(successful.englishLabel.startsWith("Successful"))

        val effort = StatusPresentation.formatMidtermResult("تلاش بیشتر")
        assertTrue(effort.englishLabel.startsWith("Needs More Effort"))

        val excused = StatusPresentation.formatMidtermResult("معذرتي")
        assertTrue(excused.englishLabel.startsWith("Excused"))

        val absent = StatusPresentation.formatMidtermResult("غایب")
        assertTrue(absent.englishLabel.startsWith("Absent"))

        val empty = StatusPresentation.formatMidtermResult(null)
        assertEquals("Pending / Not Entered", empty.englishLabel)
    }

    @Test
    fun testAnnualResultPresentation() {
        val promoted = StatusPresentation.formatAnnualResult("ارتقا صنف")
        assertTrue(promoted.englishLabel.startsWith("Promoted"))

        val repeat = StatusPresentation.formatAnnualResult("تکرار صنف")
        assertTrue(repeat.englishLabel.startsWith("Repeat Grade"))

        val conditional = StatusPresentation.formatAnnualResult("مشروط")
        assertTrue(conditional.englishLabel.startsWith("Conditional"))

        val banned = StatusPresentation.formatAnnualResult("محروم")
        assertTrue(banned.englishLabel.startsWith("Disqualified"))

        val transfer = StatusPresentation.formatAnnualResult("سه پارچه")
        assertTrue(transfer.englishLabel.startsWith("Transferred"))
    }

    @Test
    fun testGradePresentation() {
        val gradeA = StatusPresentation.formatGrade("الف")
        assertTrue(gradeA.englishLabel.startsWith("A"))

        val gradeB = StatusPresentation.formatGrade("ب")
        assertTrue(gradeB.englishLabel.startsWith("B"))

        val gradeC = StatusPresentation.formatGrade("ج")
        assertTrue(gradeC.englishLabel.startsWith("C"))

        val gradeD = StatusPresentation.formatGrade("د")
        assertTrue(gradeD.englishLabel.startsWith("D"))

        val gradeE = StatusPresentation.formatGrade("هـ")
        assertTrue(gradeE.englishLabel.startsWith("E"))

        val invalid = StatusPresentation.formatGrade("اشتباه")
        assertTrue(invalid.englishLabel.startsWith("Invalid"))
    }
}
