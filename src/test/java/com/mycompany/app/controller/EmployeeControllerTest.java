package com.mycompany.app.controller;

import com.mycompany.app.service.facade.EmployeeFacade;
import com.mycompany.app.domain.Employee;
import com.mycompany.app.domain.Department;
import static java.util.Collections.singletonMap;
import java.util.List;
import javax.ejb.EJB;
import static javax.ws.rs.client.Entity.json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.arquillian.container.test.api.Deployment;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasContentType;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasStatus;

/**
 * Test class for the EmployeeController REST controller.
 *
 */

public class EmployeeControllerTest extends ApplicationTest {

    private static final String DEFAULT_FIRST_NAME = "AAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBB";
    private static final String DEFAULT_LAST_NAME = "AAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBB";
    private static final String RESOURCE_PATH = "api/employee";

    @Deployment
    public static WebArchive createDeployment() {
        return buildApplication().addClass(Employee.class).addClass(Department.class).addClass(EmployeeFacade.class).addClass(EmployeeController.class);
    }

    private static Employee employee;

    @EJB
    private EmployeeFacade employeeFacade;

    @Test
    @InSequence(1)
    public void createEmployee() throws Exception {

        int databaseSizeBeforeCreate = employeeFacade.findAll().size();

        // Create the Employee
        employee = new Employee();
        employee.setFirstName(DEFAULT_FIRST_NAME);
        employee.setLastName(DEFAULT_LAST_NAME);
        Response response = target(RESOURCE_PATH).post(json(employee));
        assertThat(response, hasStatus(Status.CREATED));
        employee = response.readEntity(Employee.class);

        // Validate the Employee in the database
        List<Employee> employees = employeeFacade.findAll();
        assertThat(employees.size(), is(databaseSizeBeforeCreate + 1));
        Employee testEmployee = employees.get(employees.size() - 1);
        assertThat(testEmployee.getFirstName(), is(DEFAULT_FIRST_NAME));
        assertThat(testEmployee.getLastName(), is(DEFAULT_LAST_NAME));
    }

    @Test
    @InSequence(2)
    public void getAllEmployees() throws Exception {

        int databaseSize = employeeFacade.findAll().size();
        // Get all the employees
        Response response = target(RESOURCE_PATH).get();
        assertThat(response, hasStatus(Status.OK));
        assertThat(response, hasContentType(MediaType.APPLICATION_JSON_TYPE));

        List<Employee> employees = response.readEntity(List.class);
        assertThat(employees.size(), is(databaseSize));
    }

    @Test
    @InSequence(3)
    public void getEmployee() throws Exception {

        // Get the employee
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", employee.getId())).get();
        Employee testEmployee = response.readEntity(Employee.class);
        assertThat(response, hasStatus(Status.OK));
        assertThat(response, hasContentType(MediaType.APPLICATION_JSON_TYPE));
        assertThat(testEmployee.getId(), is(employee.getId()));
        assertThat(testEmployee.getFirstName(), is(DEFAULT_FIRST_NAME));
        assertThat(testEmployee.getLastName(), is(DEFAULT_LAST_NAME));
    }

    @Test
    @InSequence(4)
    public void getNonExistingEmployee() throws Exception {

        // Get the employee
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", Long.MAX_VALUE)).get();
        assertThat(response, hasStatus(Status.NOT_FOUND));
    }

    @Test
    @InSequence(5)
    public void updateEmployee() throws Exception {

        int databaseSizeBeforeUpdate = employeeFacade.findAll().size();

        // Update the employee
        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(employee.getId());
        updatedEmployee.setFirstName(UPDATED_FIRST_NAME);
        updatedEmployee.setLastName(UPDATED_LAST_NAME);

        Response response = target(RESOURCE_PATH).put(json(updatedEmployee));
        assertThat(response, hasStatus(Status.OK));

        // Validate the Employee in the database
        List<Employee> employees = employeeFacade.findAll();
        assertThat(employees.size(), is(databaseSizeBeforeUpdate));
        Employee testEmployee = employees.get(employees.size() - 1);
        assertThat(testEmployee.getFirstName(), is(UPDATED_FIRST_NAME));
        assertThat(testEmployee.getLastName(), is(UPDATED_LAST_NAME));
    }

    @Test
    @InSequence(6)
    public void removeEmployee() throws Exception {

        int databaseSizeBeforeDelete = employeeFacade.findAll().size();

        // Delete the employee
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", employee.getId())).delete();
        assertThat(response, hasStatus(Status.OK));

        // Validate the database is empty
        List<Employee> employees = employeeFacade.findAll();
        assertThat(employees.size(), is(databaseSizeBeforeDelete - 1));
    }

}
