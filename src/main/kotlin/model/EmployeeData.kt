package model

import com.fasterxml.jackson.annotation.JsonProperty

data class EmployeeData(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("role") val role: String,
    @JsonProperty("department") val department: String,
    @JsonProperty("reportingTo") val reportingTo: String
)