package model

import java.util.UUID

data class CheckInRequest(
    val employeeId: UUID? = null,
    val checkInDateTime: String? = null
)
