import io.dropwizard.core.Configuration
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Environment
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dao.EmployeeList
import services.Employee

class Application : Application<Configuration>(){
    override fun run(configuration: Configuration, environment: Environment) {
        environment.objectMapper.registerKotlinModule()
        environment.objectMapper.registerModule(JavaTimeModule())

        val manager = Employee(
            firstNameInput = "Harinee",
            lastNameInput = "S",
            roleInput = "MANAGER",
            departmentInput = "IT",
            reportingToInput = "0"
        )

        EmployeeList.employeeInstance.add(manager)

        val employeeResource = EmployeeResource()
        environment.jersey().register(employeeResource)

        val attendanceResource = AttendanceResource()
        environment.jersey().register(attendanceResource)
    }
}

fun main(args: Array<String>) {
    Application().run(*args)
}
