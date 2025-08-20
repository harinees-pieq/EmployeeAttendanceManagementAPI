package model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class AttendanceData(
    val id: Int,
    val employeeId: String,
    val employeeName: String,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    val checkInDateTime: LocalDateTime,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    val checkOutDateTime: LocalDateTime?,
    var workingHours: String?
)