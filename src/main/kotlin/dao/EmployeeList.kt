package dao

import model.EmployeeData
import services.Employee
import java.lang.IllegalArgumentException

class EmployeeList : ArrayList<Employee>() {
    companion object {
        val employeeInstance: EmployeeList = EmployeeList()
    }

    fun addEmployee(employeeData: EmployeeData): Map<String, Any?> {
        return try {
            val newEmployee = Employee(
                firstNameInput = employeeData.firstName,
                lastNameInput = employeeData.lastName,
                roleInput = employeeData.role,
                departmentInput = employeeData.department,
                reportingToInput = employeeData.reportingTo
            )
            this.add(newEmployee)
            mapOf("employee" to newEmployee)
        } catch (e: IllegalArgumentException) {
            mapOf("error" to e.message, "error_type" to "BAD_REQUEST")
        }
    }

    fun findById(id: String): Employee? {
        return this.find { it.id == id }
    }

    fun listAll(): List<Employee> {
        return this.toList()
    }

    fun deleteEmployee(empId: String): Map<String, Any?> {
        val employee = this.findById(empId)
            ?: return mapOf("error" to "Employee ID not found.", "error_type" to "NOT_FOUND")
        this.remove(employee)
        AttendanceList.attendanceInstance.deleteRecordsForEmployee(empId)

        return mapOf("success" to "Employee ID ${employee.id} deleted successfully.")
    }
}