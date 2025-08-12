package model

data class CheckInRequest(
    val employeeId: String,
    val checkInDateTime: String? = null
)