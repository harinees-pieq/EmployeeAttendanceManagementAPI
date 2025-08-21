package dao

//models
import model.AttendanceData
import model.AttendanceSummary
//map query result to kotlin data class
import org.jdbi.v3.core.kotlin.mapTo
//for db operations
import org.jdbi.v3.core.Jdbi
import jakarta.ws.rs.NotFoundException
//date time
import java.time.LocalDate
import java.time.LocalDateTime

class AttendanceDao(private val jdbi: Jdbi) {
    fun listAll(): List<AttendanceData> {
        val sql =  """SELECT att.id, att.employee_id, CONCAT(emp.first_name, ' ', emp.last_name) AS employee_name, 
                   att.check_in_date_time, att.check_out_date_time, 
                   (att.check_out_date_time - att.check_in_date_time) AS working_hours
                    FROM new_attendance AS att 
                    INNER JOIN new_employees AS emp ON att.employee_id = emp.id"""
        return jdbi.withHandle<List<AttendanceData>, Exception> { handle ->
            handle.createQuery(sql).mapTo<AttendanceData>().list()
        }
    }

    fun findById(id: String): List<AttendanceData> {
        val checkEmployeeQuery = "SELECT COUNT(*) FROM new_employees WHERE id = :id"
        val attendanceQuery = """SELECT att.id, att.employee_id, CONCAT(emp.first_name, ' ', emp.last_name) AS employee_name, att.check_in_date_time,
                                att.check_out_date_time, (att.check_out_date_time - att.check_in_date_time) AS working_hours
                                FROM new_attendance AS att INNER JOIN new_employees AS emp 
                                ON att.employee_id = emp.id
                                WHERE att.employee_id = :id;"""
        return jdbi.withHandle<List<AttendanceData>, Exception> { handle ->
            val employeeExists = handle.createQuery(checkEmployeeQuery)
                .bind("id", id).mapTo<Int>().one() > 0
            if (employeeExists) {
                handle.createQuery(attendanceQuery).bind("id", id)
                    .mapTo<AttendanceData>().list()
            } else {
                throw NotFoundException("Employee with ID '$id' not found")
            }
        }
    }

    fun findActiveCheckIn(employeeId: String): AttendanceData? {
        val sql = """SELECT att.id, att.employee_id AS employeeId, CONCAT(emp.first_name, ' ', emp.last_name) AS employeeName, 
                    att.check_in_date_time AS checkInDateTime, att.check_out_date_time AS checkOutDateTime
                    FROM new_attendance AS att
                    JOIN new_employees AS emp ON att.employee_id = emp.id
                    WHERE att.employee_id = :employeeId AND att.check_out_date_time IS NULL"""
        return jdbi.withHandle<AttendanceData?, Exception> { handle ->
            handle.createQuery(sql).bind("employeeId", employeeId).mapTo<AttendanceData>().findFirst().orElse(null)
        }
    }

    fun checkIn(employeeId: String, checkInTime: LocalDateTime): Int {
        val sql = "INSERT INTO new_attendance (employee_id, check_in_date_time) VALUES (:employeeId, :checkInDateTime)"
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(sql)
                .bind("employeeId", employeeId)
                .bind("checkInDateTime", checkInTime)
                .execute()
        }
    }

    fun checkOut(attendanceId: Int, checkOutTime: LocalDateTime): Int {
        val sql = "UPDATE new_attendance SET check_out_date_time = :checkOutTime WHERE id = :id"
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(sql)
                .bind("id", attendanceId)
                .bind("checkOutTime", checkOutTime)
                .execute()
        }
    }

    fun findByPK(id: Int): AttendanceData? {
        val sql = """SELECT att.id, att.employee_id AS employeeId, CONCAT(emp.first_name, ' ', emp.last_name) AS employeeName, 
                   att.check_in_date_time AS checkInDateTime, att.check_out_date_time AS checkOutDateTime
                    FROM new_attendance AS att
                    JOIN new_employees AS emp ON att.employee_id = emp.id
                    WHERE att.id = :id"""
        return jdbi.withHandle<AttendanceData?, Exception> { handle ->
            handle.createQuery(sql).bind("id", id).mapTo<AttendanceData>().findFirst().orElse(null)
        }
    }

    fun deleteByEmployeeId(employeeId: String): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM new_attendance WHERE employee_id = :employeeId")
                .bind("employeeId", employeeId)
                .execute()
        }
    }

    fun getSummaryReport(fromDate: LocalDate, toDate: LocalDate): List<AttendanceSummary> {
        val sql = """SELECT a.employee_id AS employeeId,
                CONCAT(e.first_name, ' ', e.last_name) AS employeeName, 
                TO_CHAR( justify_interval(SUM(a.check_out_date_time - a.check_in_date_time)), 'FMHH24:MI') AS totalHours
                FROM new_attendance a
                JOIN new_employees e ON a.employee_id = e.id
                WHERE DATE(a.check_in_date_time) BETWEEN :fromDate AND :toDate
                AND a.check_out_date_time IS NOT NULL
                GROUP BY a.employee_id, e.first_name, e.last_name
                ORDER BY a.employee_id"""
        return jdbi.withHandle<List<AttendanceSummary>, Exception> { handle ->
            handle.createQuery(sql)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .mapTo<AttendanceSummary>().list()
        }
    }
}
