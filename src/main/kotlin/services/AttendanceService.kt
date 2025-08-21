package services

//dao classes
import dao.AttendanceDao
import dao.EmployeeDao
//exceptions
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
//models
import model.AttendanceData
import model.ReportRequest
import model.CheckInRequest
import model.CheckOutRequest
import model.AttendanceSummary
//date time
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class AttendanceService(
    private val attendanceDao: AttendanceDao,
    private val employeeDao: EmployeeDao
){
    fun findAll(): List<AttendanceData> {
        val records = attendanceDao.listAll()
        records.forEach { record ->
            if (record.checkOutDateTime != null) {
                val duration = Duration.between(record.checkInDateTime, record.checkOutDateTime)
                record.workingHours = String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
            }
        }
        return records
    }

    fun findById(id: String): List<AttendanceData> {
        val records = attendanceDao.findById(id)
        records.forEach { record ->
            if (record.checkOutDateTime != null) {
                val duration = Duration.between(record.checkInDateTime, record.checkOutDateTime)
                record.workingHours = String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
            }
        }
        return records
    }

    fun checkIn(checkInData: CheckInRequest): AttendanceData {
        employeeDao.findById(checkInData.employeeId) ?: throw NotFoundException("Employee not found.")
        val checkInTime = parseDateTime(checkInData.checkInDateTime)
        if (checkInTime.isAfter(LocalDateTime.now())) {
            throw ClientErrorException("Check-in time cannot be in the future.", Response.Status.BAD_REQUEST)
        }
        attendanceDao.findActiveCheckIn(checkInData.employeeId)?.let {
            throw ClientErrorException("Employee already has an active check-in.", Response.Status.CONFLICT)
        }
        attendanceDao.checkIn(checkInData.employeeId, checkInTime)
        return attendanceDao.findActiveCheckIn(checkInData.employeeId)!!
    }

    fun checkOut(checkOutData: CheckOutRequest): AttendanceData {
        employeeDao.findById(checkOutData.employeeId) ?: throw NotFoundException("Employee not found.")
        val activeRecord = attendanceDao.findActiveCheckIn(checkOutData.employeeId)
            ?: throw NotFoundException("No active check-in found for this employee.")
        val checkOutTime = parseDateTime(checkOutData.checkOutDateTime)
        if (checkOutTime.isAfter(LocalDateTime.now())) {
            throw ClientErrorException("Check-out time cannot be in the future.", Response.Status.BAD_REQUEST)
        }
        if (!checkOutTime.isAfter(activeRecord.checkInDateTime)) {
            throw ClientErrorException("Check-out time must be after check-in time.", Response.Status.BAD_REQUEST)
        }
        if (checkOutTime.toLocalDate() != activeRecord.checkInDateTime.toLocalDate()) {
            throw ClientErrorException("Check-out must be on the same day as check-in.", Response.Status.BAD_REQUEST)
        }
        attendanceDao.checkOut(activeRecord.id, checkOutTime)
        val updatedRecord = attendanceDao.findByPK(activeRecord.id)!!
        if (updatedRecord.checkOutDateTime != null) {
            val duration = Duration.between(updatedRecord.checkInDateTime, updatedRecord.checkOutDateTime)
            updatedRecord.workingHours = String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
        }
        return updatedRecord
    }

    private fun parseDateTime(dateTimeString: String?): LocalDateTime {
        return if (dateTimeString.isNullOrBlank()) {
            LocalDateTime.now()
        } else {
            try {
                LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            } catch (e: DateTimeParseException) {
                throw ClientErrorException("Invalid date-time format. Please use 'dd-MM-yyyy HH:mm'.", Response.Status.BAD_REQUEST)
            }
        }
    }

    fun getSummary(reportRequest: ReportRequest): List<AttendanceSummary> {
        val fromDate = parseDate(reportRequest.fromDate)
        val toDate = parseDate(reportRequest.toDate)
        return attendanceDao.getSummaryReport(fromDate, toDate)
    }

    private fun parseDate(dateString: String): LocalDate {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        } catch (e: DateTimeParseException) {
            throw ClientErrorException("Invalid date format. Please use 'dd-MM-yyyy'.", Response.Status.BAD_REQUEST)
        }
    }
}
