package resources

//APIs
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
//model
import model.EmployeeData
//service
import services.EmployeeService
//exception
import java.lang.IllegalArgumentException

@Path("/employees")
class EmployeeResource(private val employeeService: EmployeeService) {
    @GET
    fun getAllEmployees(): List<EmployeeData> {
        return employeeService.findAll()
    }

    @GET
    @Path("/{id}")
    fun getEmployee(@PathParam("id") id: String): EmployeeData {
        return employeeService.findById(id)
    }

    @POST
    fun addEmployee(employeeData: EmployeeData): Response {
        return try {
            val createdEmployee = employeeService.create(employeeData)
            Response.status(Response.Status.OK).entity(createdEmployee).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to e.message)).build()
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteEmployee(@PathParam("id") id: String): Response {
        employeeService.deleteById(id)
        return Response.ok(mapOf("success" to "Employee ID $id deleted successfully.")).build()
    }
}
