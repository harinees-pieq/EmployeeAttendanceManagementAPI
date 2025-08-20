package dao
import model.AttendanceData
import jakarta.ws.rs.NotFoundException
import model.AttendanceSummary
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.Jdbi
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
                    WHERE att.employee_id = :employeeId AND att.check_out_date_time IS NULL
                    ORDER BY att.check_in_date_time DESC LIMIT 1"""
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

    fun attendanceSummary(fromDate: String, toDate: String): List<AttendanceSummary> {
        val sql = """SELECT emp.id AS employeeId, CONCAT(emp.first_name, ' ', emp.last_name) AS employeeName,
                SUM(att.check_out_date_time - att.check_in_date_time)::text AS totalWorkingHours
                FROM new_attendance AS att
                JOIN new_employees AS emp ON emp.id = att.employee_id
                WHERE DATE(att.check_in_date_time) BETWEEN :fromDate AND :toDate
                GROUP BY emp.id, employeeName
                ORDER BY emp.id;"""
        return jdbi.withHandle<List<AttendanceSummary>, Exception> { handle ->
            handle.createQuery(sql).bind("fromDate",fromDate).bind("toDate", toDate)
                .mapTo<AttendanceSummary>().list()
        }
    }
}