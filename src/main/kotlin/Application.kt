import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dao.EmployeeDao
import dao.AttendanceDao
import dao.RoleDao
import dao.DepartmentDao
import resources.EmployeeResource
import resources.AttendanceResource
import services.AttendanceService
import services.EmployeeService
import config.MyConfiguration
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Environment
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin

class Application : Application<MyConfiguration>() {
    override fun run(configuration: MyConfiguration, environment: Environment) {
        val kotlinModule = KotlinModule.Builder().build()

        environment.objectMapper.registerModule(kotlinModule)
        environment.objectMapper.registerModule(JavaTimeModule())
        environment.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val jdbi = Jdbi.create(configuration.database.build(environment.metrics(), "postgresql"))
        jdbi.installPlugin(SqlObjectPlugin())
        jdbi.installPlugin(KotlinPlugin())

        val employeeDao = EmployeeDao(jdbi)
        val attendanceDao = AttendanceDao(jdbi)
        val roleDao = RoleDao(jdbi)
        val departmentDao = DepartmentDao(jdbi)

        val employeeService = EmployeeService(employeeDao, roleDao, departmentDao, attendanceDao)
        val attendanceService = AttendanceService(attendanceDao, employeeDao)

        val employeeResource = EmployeeResource(employeeService)
        val attendanceResource = AttendanceResource(attendanceService)

        environment.jersey().register(employeeResource)
        environment.jersey().register(attendanceResource)
    }
}

fun main(args: Array<String>) {
    Application().run(*args)
}