package dao
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import model.EmployeeData

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

    fun findById(id: String): EmployeeData? {
        val sql = """SELECT emp.id, emp.first_name AS firstName, emp.last_name AS lastName, r.name AS role,
                d.name AS department, emp.reporting_to AS reportingTo FROM new_employees AS emp
                JOIN role AS r ON emp.role_id = r.id
                JOIN department AS d ON emp.department_id = d.id
                WHERE emp.id = :id"""
        return jdbi.withHandle<EmployeeData?, Exception> { handle ->
            handle.createQuery(sql).bind("id", id).mapTo<EmployeeData>().findFirst().orElse(null)
        }
    }

    private fun getNextId(): String {
        val sql = """SELECT id FROM new_employees ORDER BY pk DESC LIMIT 1"""
        val lastId = jdbi.withHandle<String?, Exception> { handle ->
            handle.createQuery(sql).mapTo<String>().findFirst().orElse("PIEQ0000")
        }
        val lastNumber = lastId!!.substring(4).toInt()
        val nextNumber = lastNumber + 1
        return "PIEQ%04d".format(nextNumber)
    }

    fun addEmployee(
        firstName: String,
        lastName: String,
        role: String,
        department: String,
        reportingTo: String?
    ): EmployeeData {
        val newId = getNextId()
        val sql = """
        INSERT INTO new_employees (id, first_name, last_name, role_id, department_id, reporting_to)
        VALUES (:id, :firstName, :lastName, 
        (SELECT id FROM role WHERE name = :role),
        (SELECT id FROM department WHERE name = :department), :reportingTo)"""
        jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(sql)
            .bind("id", newId)
            .bind("firstName", firstName)
            .bind("lastName", lastName)
            .bind("role", role)
            .bind("department", department)
            .bind("reportingTo", reportingTo)
            .execute()
        }
        val newEmployee = findById(newId)
        if (newEmployee == null) {
            throw IllegalStateException("Failed to create and retrieve employee. The role or department may be invalid.")
        }
        return newEmployee
    }

    fun deleteEmployee(id: String): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM new_employees WHERE id = :id")
            .bind("id", id).execute()
        }
    }
}