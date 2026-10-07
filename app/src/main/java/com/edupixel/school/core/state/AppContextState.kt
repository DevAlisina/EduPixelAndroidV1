package com.edupixel.school.core.state

import com.edupixel.school.data.remote.models.AcademicYear
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state holding the current active school and active class context.
 */
object AppContextState {
    private val _selectedSchool = MutableStateFlow<School?>(null)
    val selectedSchool: StateFlow<School?> = _selectedSchool.asStateFlow()

    private val _selectedClass = MutableStateFlow<SchoolClass?>(null)
    val selectedClass: StateFlow<SchoolClass?> = _selectedClass.asStateFlow()

    private val _selectedYear = MutableStateFlow<AcademicYear?>(null)
    val selectedYear: StateFlow<AcademicYear?> = _selectedYear.asStateFlow()

    fun setSelectedSchool(school: School?) {
        _selectedSchool.value = school
        // Clear class if it belonged to different school
        if (_selectedClass.value?.schoolId != school?.id) {
            _selectedClass.value = null
        }
    }

    fun setSelectedClass(schoolClass: SchoolClass?) {
        _selectedClass.value = schoolClass
    }

    fun setSelectedYear(year: AcademicYear?) {
        _selectedYear.value = year
    }
}
