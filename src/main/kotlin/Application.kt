//json serialize and deserialize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
//data access models
import dao.EmployeeDao
import dao.AttendanceDao
import dao.RoleDao
import dao.DepartmentDao
//end points
import resources.EmployeeResource
import resources.AttendanceResource
import services.AttendanceService
import services.EmployeeService
//configuration
import config.MyConfiguration
//core dropwizard class
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Environment
//jdbi plugin
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin

class Application : Application<MyConfiguration>() {
    override fun run(configuration: MyConfiguration, environment: Environment) {
        //jackson module support
        val kotlinModule = KotlinModule.Builder().build()
        environment.objectMapper.registerModule(kotlinModule)
        environment.objectMapper.registerModule(JavaTimeModule())

        //creates jdbi instance from yml file
        val jdbi = Jdbi.create(configuration.database.build(environment.metrics(), "postgresql"))

        //supports kotlin language features like dataclass
        jdbi.installPlugin(KotlinPlugin())

        //accept jdbi instance and used it to execute queries
        val employeeDao = EmployeeDao(jdbi)
        val attendanceDao = AttendanceDao(jdbi)
        val roleDao = RoleDao(jdbi)
        val departmentDao = DepartmentDao(jdbi)

        //service classes
        val employeeService = EmployeeService(employeeDao, roleDao, departmentDao, attendanceDao)
        val attendanceService = AttendanceService(attendanceDao, employeeDao)

        //RESTAPI
        val employeeResource = EmployeeResource(employeeService)
        val attendanceResource = AttendanceResource(attendanceService)

        //register dropwizard's jersey based webserver and enables API routes
        environment.jersey().register(employeeResource)
        environment.jersey().register(attendanceResource)
    }
}

//when ./gradlew run --> launches dropwizard server using config.yml
fun main(args: Array<String>) {
    Application().run(*args)
}
