package model

data class CheckOutRequest(
    val employeeId: String,
    val checkOutDateTime: String? = null
)