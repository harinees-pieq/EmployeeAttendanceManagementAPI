package resources

import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import model.AttendanceData
import model.AttendanceSummary
import model.CheckInRequest
import model.CheckOutRequest
import services.AttendanceService
import model.ReportRequest

@Path("/attendance")
class AttendanceResource(private val attendanceService: AttendanceService) {
    @GET
    fun viewAllAttendance(): List<AttendanceData> {
        return attendanceService.findAll()
    }

    @GET
    @Path("/{id}")
    fun getAttendance(@PathParam("id") id: String): List<AttendanceData> {
        return attendanceService.findById(id)
    }

    @POST
    @Path("/check-in")
    fun checkIn(checkInData: CheckInRequest): Response {
        return try {
            val newRecord = attendanceService.checkIn(checkInData)
            Response.status(Response.Status.CREATED).entity(newRecord).build()
        } catch (e: ClientErrorException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to e.message)).build()
        }
    }

    @POST
    @Path("/check-out")
    fun checkOut(checkOutData: CheckOutRequest): Response {
        return try {
            val updatedRecord = attendanceService.checkOut(checkOutData)
            Response.ok(updatedRecord).build()
        } catch (e: ClientErrorException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to e.message)).build()
        }
    }

    @POST
    @Path("/report-summary")
    fun reportSummary(reportRequest: ReportRequest): List<AttendanceSummary> {
        return attendanceService.attendanceSummary(reportRequest)
    }
}
