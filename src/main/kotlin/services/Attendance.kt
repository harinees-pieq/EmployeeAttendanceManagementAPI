package services

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.LocalDateTime

class Attendance(
    @JsonProperty("employeeId") val employeeId: String,
    checkInTime: LocalDateTime = LocalDateTime.now()
) {
    @JsonProperty("checkInDateTime")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    val checkInDateTime: LocalDateTime = checkInTime

    @JsonProperty("checkOutDateTime")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    var checkOutDateTime: LocalDateTime? = null

    @JsonProperty("workingHours")
    var workingHours: String? = null

    fun checkOut(timeOfCheckout: LocalDateTime = LocalDateTime.now()): String? {
        if (this.checkOutDateTime != null) {
            return "Already checked out."
        }
        if (timeOfCheckout.isAfter(LocalDateTime.now())) {
            return "Cannot check out with a future date/time."
        }
        if (!timeOfCheckout.isAfter(checkInDateTime)) {
            return "Check-out time must be after check-in time."
        }
        if (timeOfCheckout.toLocalDate() != checkInDateTime.toLocalDate()) {
            return "Check-out must be on the same date as check-in."
        }

        this.checkOutDateTime = timeOfCheckout

        val duration = Duration.between(checkInDateTime, this.checkOutDateTime)
        val totalMinutes = duration.toMinutes()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        this.workingHours = String.format("%02d:%02d", hours, minutes)

        return null
    }

    fun isWithin(dateFrom: LocalDateTime, dateTo: LocalDateTime): Boolean {
        return !checkInDateTime.isBefore(dateFrom) && !checkInDateTime.isAfter(dateTo)
    }
}