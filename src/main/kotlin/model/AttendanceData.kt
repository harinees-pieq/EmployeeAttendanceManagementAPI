package model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AttendanceData(
    val id: Int,
    val employeeId: UUID,
    val employeeName: String,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    val checkInDateTime: LocalDateTime,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    val checkOutDateTime: LocalDateTime?,
    var workingHours: String?
)
