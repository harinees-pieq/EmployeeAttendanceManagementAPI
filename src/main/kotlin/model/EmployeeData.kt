package model

import java.util.UUID

data class EmployeeData(
    var id: UUID? = null,
    var firstName: String = "",
    var lastName: String = "",
    var role: String = "",
    var department: String = "",
    var reportingTo: UUID? = null
)
