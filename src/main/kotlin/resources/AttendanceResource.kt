package resources

//APIs
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
//models
import model.AttendanceData
import model.CheckInRequest
import model.CheckOutRequest
import model.ReportRequest
//service
import services.AttendanceService

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
            Response.status(Response.Status.OK).entity(newRecord).build()
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
    fun getAttendanceSummary(reportRequest: ReportRequest): Response {
        return try {
            val summary = attendanceService.getSummary(reportRequest)
            Response.ok(summary).build()
        } catch (e: ClientErrorException) {
            Response.status(e.response.status).entity(mapOf("error" to e.message)).build()
        }
    }
}
