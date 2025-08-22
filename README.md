# Employee Attendance Management API

## Application.kt

It is the main part of our whole API. When it runs,t setsup JSON support by using jackson, connects to postgreSQL db using jdbi, and also initiates dao, service classes, register REST API for attendance, employees and starts the server using Configuration.

## MyConfiguration.kt

This class extends Drwizard's Configuration class and provides DataSourceFactory which isused to read db connection from the yaml config file.

## Config.yml

This file setup ports to run, configures the postgreSQL db connection and also sets logging.

## EmployeeResource.kt

It has endpoints to get all employees, get one by ID, adding new employee, deleting employee using EmployeeService class for logicand returns proper response.

## EmployeeService.kt

It has logic for managing employees and it uses dao. 

## EmployeeDao.kt

It uses jdbi to perform db operations like list all employees, find by one, generate id, add new employee, delete employee.

## DepartmentDao.kt

It uses jdbi to check if the department given in request to add employee is there in the department table or not.

## RoleDao.kt

It is also similar to DepartmentDao, it checks role table.

## EmployeeData.kt

It holds structure and it is used in EmployeeDao for mapTo, in EmployeeService class to structure data and also in the EmployeeResource file.
