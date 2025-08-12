package dao

import services.Attendance
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class AttendanceList : ArrayList<Attendance>() {
    companion object {
        val attendanceInstance: AttendanceList = AttendanceList()
    }

    fun performCheckIn(employeeId: String, checkInDateTimeString: String?): Map<String, Any?> {
        val employee = EmployeeList.employeeInstance.findById(employeeId)
            ?: return mapOf("error" to "Employee with ID $employeeId not found.", "error_type" to "NOT_FOUND")

        val checkInTime: LocalDateTime
        if (checkInDateTimeString.isNullOrBlank()) {
            checkInTime = LocalDateTime.now()
        } else {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                checkInTime = LocalDateTime.parse(checkInDateTimeString, formatter)
            } catch (e: DateTimeParseException) {
                return mapOf("error" to "Invalid date format. Please use 'dd-MM-yyyy HH:mm'.", "error_type" to "BAD_REQUEST")
            }
        }

        if (checkInTime.isAfter(LocalDateTime.now())) {
            return mapOf("error" to "Check-in time cannot be in the future.", "error_type" to "BAD_REQUEST")
        }

        val newAttendanceRecord = Attendance(employee.id, checkInTime)
        val wasAdded = this.add(newAttendanceRecord)

        return if (wasAdded) {
            mapOf("record" to newAttendanceRecord)
        } else {
            mapOf("error" to "Check-in failed. This employee may already have an active check-in for today.", "error_type" to "CONFLICT")
        }
    }

    override fun add(element: Attendance): Boolean {
        val newElementDate = element.checkInDateTime.toLocalDate()
        val alreadyCheckedInToday = this.any {
            it.employeeId == element.employeeId && it.checkInDateTime.toLocalDate() == newElementDate
        }
        return if (alreadyCheckedInToday) false else super.add(element)
    }

    fun performCheckOut(employeeId: String, checkOutDateTimeString: String?): Map<String, Any?> {
        val record = this.findLast { it.employeeId == employeeId && it.checkOutDateTime == null }
            ?: return mapOf("error" to "No active check-in found for employee $employeeId.", "error_type" to "NOT_FOUND")

        val checkOutTime: LocalDateTime
        if (checkOutDateTimeString.isNullOrBlank()) {
            checkOutTime = LocalDateTime.now()
        } else {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                checkOutTime = LocalDateTime.parse(checkOutDateTimeString, formatter)
            } catch (e: DateTimeParseException) {
                return mapOf(
                    "error" to "Invalid date format for checkOutDateTime. Please use 'dd-MM-yyyy HH:mm'.",
                    "error_type" to "BAD_REQUEST"
                )
            }
        }

        val errorMessage = record.checkOut(checkOutTime)

        return if (errorMessage == null) {
            mapOf("record" to record)
        } else {
            mapOf("error" to errorMessage, "error_type" to "BAD_REQUEST")
        }
    }

    fun getWorkingHoursSummaryByDateStrings(fromString: String?, toStr: String?): Map<String, Any?> {
        if (fromString.isNullOrBlank() || toStr.isNullOrBlank()) {
            return mapOf("error" to "Request body must include 'fromDate' and 'toDate'.", "error_type" to "BAD_REQUEST")
        }

        return try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val fromLocalDate = LocalDate.parse(fromString, formatter)
            val toLocalDate = LocalDate.parse(toStr, formatter)
            val fromDate = fromLocalDate.atStartOfDay()
            val toDate = toLocalDate.atTime(LocalTime.MAX)
            val summary = getWorkingHoursSummaryByDate(from = fromDate, to = toDate)
            mapOf("summary" to summary)
        } catch (e: DateTimeParseException) {
            mapOf("error" to "Invalid Date Format. Please use 'dd-MM-yyyy'.", "error_type" to "BAD_REQUEST")
        }
    }

    fun getWorkingHoursSummaryByDate(from: LocalDateTime, to: LocalDateTime): List<Map<String, Any>> {
        val relevantRecords = this.filter { it.checkOutDateTime != null && it.isWithin(from, to) }
        return relevantRecords.groupBy { it.employeeId }
            .map { (empId, records) ->
                val empName = EmployeeList.employeeInstance.findById(empId)?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"
                val totalMinutes = records.sumOf { Duration.between(it.checkInDateTime, it.checkOutDateTime!!).toMinutes() }
                val formattedHours = String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60)
                mapOf("employeeId" to empId, "employeeName" to empName, "totalHours" to formattedHours)
            }
            .sortedBy { (it["employeeId"] as String).substringAfter("PIEQ").toIntOrNull() ?: 0 }
    }

    fun getAll(): List<Attendance> = this.toList()

    fun deleteRecordsForEmployee(employeeId: String) {
        this.removeIf { it.employeeId == employeeId }
    }
}