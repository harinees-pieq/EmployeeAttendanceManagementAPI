import dao.EmployeeList
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import model.EmployeeData
import services.Employee

@Path("/employees")
class EmployeeResource {
    @GET
    fun getAllEmployees(): List<Employee> {
        return EmployeeList.employeeInstance.listAll()
    }

    @GET
    @Path("/{id}")
    fun getEmployee(@PathParam("id") id: String): Employee? {
        return EmployeeList.employeeInstance.findById(id)
    }

    @POST
    fun addEmployee(employeeData: EmployeeData): Response {
        val resultMap = EmployeeList.employeeInstance.addEmployee(employeeData)

        return if (resultMap.containsKey("error")) {
            Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to resultMap["error"])).build()
        } else {
            Response.ok(resultMap["employee"]).build()
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteEmployee(@PathParam("id") id: String): Response {
        val resultMap = EmployeeList.employeeInstance.deleteEmployee(id)

        return if (resultMap.containsKey("error")) {
            Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to resultMap["error"])).build()
        } else {
            Response.status(Response.Status.ACCEPTED).entity(mapOf("success" to resultMap["success"])).build()
        }
    }
}