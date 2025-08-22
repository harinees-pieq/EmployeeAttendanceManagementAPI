package model

import java.util.UUID

data class AttendanceSummary(
    val employeeId: UUID,
    val employeeName: String,
    val totalHours: String
)
