package services

import com.fasterxml.jackson.annotation.JsonProperty
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicInteger

class Employee(
    @JsonProperty("firstName") firstNameInput: String,
    @JsonProperty("lastName") lastNameInput: String,
    @JsonProperty("role") roleInput: String,
    @JsonProperty("department") departmentInput: String,
    @JsonProperty("reportingTo") reportingToInput: String
) {
    var firstName: String
    var lastName: String
    var role: Role
    var department: Department
    var reportingTo: String
    var id: String

    private companion object {
        var idCounter = AtomicInteger(0)
    }

    init {
        validateFirstName(firstNameInput)?.let { throw IllegalArgumentException(it) }
        validateLastName(lastNameInput)?.let { throw IllegalArgumentException(it) }
        validateRole(roleInput)?.let { throw IllegalArgumentException(it) }
        validateDepartment(departmentInput)?.let { throw IllegalArgumentException(it) }
        validateReportingTo(reportingToInput)?.let { throw IllegalArgumentException(it) }

        this.firstName = firstNameInput
        this.lastName = lastNameInput
        this.role = Role.valueOf(roleInput.uppercase())
        this.department = Department.valueOf(departmentInput.uppercase())
        this.reportingTo = reportingToInput

        this.id = "PIEQ%04d".format(idCounter.incrementAndGet())
    }

    private fun validateRole(roleStr: String): String? {
        return try {
            Role.valueOf(roleStr.trim().uppercase())
            null
        } catch (e: Exception) {
            "Invalid role. Must be one of: ${Role.entries.joinToString()}"
        }
    }

    private fun validateDepartment(deptStr: String): String? {
        return try {
            Department.valueOf(deptStr.trim().uppercase())
            null
        } catch (e: Exception) {
            "Invalid department. Must be one of: ${Department.entries.joinToString()}"
        }
    }

    private fun validateFirstName(name: String): String? =
        if (name.isBlank()) "First name cannot be blank." else null

    private fun validateLastName(name: String): String? =
        if (name.isBlank()) "Last name cannot be blank." else null

    private fun validateReportingTo(value: String): String? =
        if (value.isBlank()) "Reporting manager ID cannot be blank." else null

    override fun toString(): String {
        return "ID: $id | Name: $firstName $lastName | services.Role: $role | services.Department: $department | Reports to: $reportingTo"
    }
}