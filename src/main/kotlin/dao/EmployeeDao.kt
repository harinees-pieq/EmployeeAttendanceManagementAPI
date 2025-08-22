package dao
//APIs
import org.jdbi.v3.core.Jdbi
//map to kotlin data class
import org.jdbi.v3.core.kotlin.mapTo
//model
import model.EmployeeData
import java.util.UUID

class EmployeeDao(private val jdbi: Jdbi) {
    fun listAll(): List<EmployeeData> {
        val sql = """SELECT emp.id, emp.first_name AS firstName, emp.last_name AS lastName, r.name AS role,
                d.name AS department, emp.reporting_to AS reportingTo FROM new_employees AS emp
                JOIN role AS r ON emp.role_id = r.id
                JOIN department AS d ON emp.department_id = d.id
                ORDER BY emp.id"""
        return jdbi.withHandle<List<EmployeeData>, Exception> { handle ->
            handle.createQuery(sql).mapTo<EmployeeData>().list()
        }
    }

    fun findById(id: UUID?): EmployeeData? {
        val sql = """SELECT emp.id, emp.first_name AS firstName, emp.last_name AS lastName,
                    r.name AS role, d.name AS department, emp.reporting_to AS reportingTo
                    FROM new_employees AS emp
                    JOIN role AS r ON emp.role_id = r.id
                    JOIN department AS d ON emp.department_id = d.id
                    WHERE emp.id = :id"""
        return jdbi.withHandle<EmployeeData?, Exception> { handle ->
            handle.createQuery(sql).bind("id", id).mapTo<EmployeeData>().findFirst().orElse(null)
        }
    }

    fun addEmployee(employee: EmployeeData): EmployeeData {
        val sql = """INSERT INTO new_employees (id, first_name, last_name, role_id, department_id, reporting_to)
                    VALUES (:id, :firstName, :lastName, 
                    (SELECT id FROM role WHERE name = :role),
                    (SELECT id FROM department WHERE name = :department), 
                    :reportingTo)"""
        jdbi.useHandle<Exception> { handle ->
            handle.createUpdate(sql)
                .bindBean(employee)
                .execute()
        }
        return employee
    }

    fun deleteEmployee(id: UUID): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM new_employees WHERE id = :id")
                .bind("id", id).execute()
        }
    }
}
