package model

data class EmployeeData(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var role: String = "",
    var department: String = "",
    var reportingTo: String? = null
)