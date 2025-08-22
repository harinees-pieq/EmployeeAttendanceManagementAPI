package model

import java.util.UUID

data class CheckOutRequest(
    val employeeId: UUID? = null,
    val checkOutDateTime: String? = null
)
