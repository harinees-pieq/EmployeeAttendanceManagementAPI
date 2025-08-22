package services

//dao classes
import dao.AttendanceDao
import dao.DepartmentDao
import dao.EmployeeDao
import dao.RoleDao
//model
import model.EmployeeData
//exceptions
import java.lang.IllegalArgumentException
import jakarta.ws.rs.NotFoundException
// Import UUID
import java.util.UUID

class EmployeeService(
    private val employeeDao: EmployeeDao,
    private val roleDao: RoleDao,
    private val departmentDao: DepartmentDao,
    private val attendanceDao: AttendanceDao
) {
    fun findAll(): List<EmployeeData> {
        return employeeDao.listAll()
    }

    fun findById(id: UUID): EmployeeData {
        return employeeDao.findById(id) ?: throw NotFoundException("Employee ID $id not found.")
    }

    fun create(employeeData: EmployeeData): EmployeeData {
        //validations
        if (employeeData.firstName.isBlank()) throw IllegalArgumentException("First name cannot be blank.")
        if (employeeData.lastName.isBlank()) throw IllegalArgumentException("Last name cannot be blank.")
        roleDao.findByName(employeeData.role)
            ?: throw IllegalArgumentException("Role '${employeeData.role}' does not exist.")
        departmentDao.findByName(employeeData.department)
            ?: throw IllegalArgumentException("Department '${employeeData.department}' does not exist.")

        val employeeToCreate = employeeData.copy(id = UUID.randomUUID())
        return employeeDao.addEmployee(employeeToCreate)
    }

    fun deleteById(id: UUID) {
        attendanceDao.deleteByEmployeeId(id.toString())
        val rowsDeleted = employeeDao.deleteEmployee(id)
        if (rowsDeleted == 0) {
            throw NotFoundException("Employee ID $id not found.")
        }
    }
}
