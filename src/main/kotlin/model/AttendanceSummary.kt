package model

import kotlin.time.Duration

data class AttendanceSummary(
    val employeeId: String,
    val employeeName: String,
    val totalWorkingHours: String
)