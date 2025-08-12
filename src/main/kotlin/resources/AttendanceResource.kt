import dao.AttendanceList
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import model.CheckInRequest
import model.CheckOutRequest
import services.Attendance

@Path("/attendance")
class AttendanceResource {
    @GET
    fun viewAllAttendance(): List<Attendance> {
        return AttendanceList.attendanceInstance.getAll()
    }

    @POST
    @Path("/check-in")
    fun checkIn(request: CheckInRequest): Response {
        val resultMap = AttendanceList.attendanceInstance.performCheckIn(
            request.employeeId,
            request.checkInDateTime
        )

        if (resultMap.containsKey("error")) {
            val errorType = resultMap["error_type"] as? String
            val status = when (errorType) {
                "NOT_FOUND" -> Response.Status.NOT_FOUND
                "CONFLICT" -> Response.Status.CONFLICT
                else -> Response.Status.BAD_REQUEST
            }
            return Response.status(status).entity(mapOf("error" to resultMap["error"])).build()
        }

        return Response.ok(resultMap["record"]).build()
    }

    @POST
    @Path("/check-out")
    fun checkOut(request: CheckOutRequest): Response {
        val resultMap = AttendanceList.attendanceInstance.performCheckOut(
            request.employeeId,
            request.checkOutDateTime
        )

        if (resultMap.containsKey("error")) {
            val errorType = resultMap["error_type"] as? String
            val status = when (errorType) {
                "NOT_FOUND" -> Response.Status.NOT_FOUND
                else -> Response.Status.BAD_REQUEST
            }
            return Response.status(status).entity(mapOf("error" to resultMap["error"])).build()
        }

        return Response.ok(resultMap["record"]).build()
    }

    @POST
    @Path("/report-summary")
    fun attendanceSummary(request: Map<String, String>): Response {
        val fromString = request["fromDate"]
        val toString = request["toDate"]

        val resultMap = AttendanceList.attendanceInstance.getWorkingHoursSummaryByDateStrings(fromString, toString)

        if (resultMap.containsKey("error")) {
            return Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to resultMap["error"])).build()
        }

        return Response.ok(resultMap["summary"]).build()
    }
}