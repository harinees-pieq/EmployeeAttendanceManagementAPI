package services

import dao.AttendanceDao
import dao.DepartmentDao
import dao.EmployeeDao
import dao.RoleDao
import model.EmployeeData
import java.lang.IllegalArgumentException
import jakarta.ws.rs.NotFoundException

class EmployeeService(
    private val employeeDao: EmployeeDao,
    private val roleDao: RoleDao,
    private val departmentDao: DepartmentDao,
    private val attendanceDao: AttendanceDao
) {
    fun findAll(): List<EmployeeData> {
        return employeeDao.listAll()
    }

    fun findById(id: String): EmployeeData {
        return employeeDao.findById(id) ?: throw NotFoundException("Employee ID $id not found.")
    }

    fun create(employeeData: EmployeeData): EmployeeData {
        if (employeeData.firstName.isBlank()) throw IllegalArgumentException("First name cannot be blank.")
        if (employeeData.lastName.isBlank()) throw IllegalArgumentException("Last name cannot be blank.")

        roleDao.findByName(employeeData.role)
            ?: throw IllegalArgumentException("Role '${employeeData.role}' does not exist.")

        departmentDao.findByName(employeeData.department)
            ?: throw IllegalArgumentException("Department '${employeeData.department}' does not exist.")

        return employeeDao.addEmployee(
            firstName = employeeData.firstName,
            lastName = employeeData.lastName,
            role = employeeData.role,
            department = employeeData.department,
            reportingTo = employeeData.reportingTo
        )
    }

    fun deleteById(id: String) {
        val rowsDeleted = employeeDao.deleteEmployee(id)
        attendanceDao.deleteByEmployeeId(id)
        if (rowsDeleted == 0) {
            throw NotFoundException("Employee ID $id not found.")
        }
    }
}