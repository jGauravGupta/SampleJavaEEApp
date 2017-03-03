package com.mycompany.app.controller;

import com.mycompany.app.service.facade.DepartmentFacade;
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
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasContentType;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasStatus;

/**
 * Test class for the DepartmentController REST controller.
 *
 */
public class DepartmentControllerTest extends ApplicationTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String RESOURCE_PATH = "api/department";

    @Deployment
    public static WebArchive createDeployment() {
        return buildApplication().addClass(Employee.class).addClass(Department.class).addClass(DepartmentFacade.class).addClass(DepartmentController.class);
    }

    private static Department department;

    @EJB
    private DepartmentFacade departmentFacade;

    @Test
    @InSequence(1)
    public void createDepartment() throws Exception {

        int databaseSizeBeforeCreate = departmentFacade.findAll().size();

        // Create the Department
        department = new Department();
        department.setName(DEFAULT_NAME);
        Response response = target(RESOURCE_PATH).post(json(department));
        assertThat(response, hasStatus(Status.CREATED));
        department = response.readEntity(Department.class);

        // Validate the Department in the database
        List<Department> departments = departmentFacade.findAll();
        assertThat(departments.size(), is(databaseSizeBeforeCreate + 1));
        Department testDepartment = departments.get(departments.size() - 1);
        assertThat(testDepartment.getName(), is(DEFAULT_NAME));
    }

    @Test
    @InSequence(2)
    public void getAllDepartments() throws Exception {

        int databaseSize = departmentFacade.findAll().size();
        // Get all the departments
        Response response = target(RESOURCE_PATH).get();
        assertThat(response, hasStatus(Status.OK));
        assertThat(response, hasContentType(MediaType.APPLICATION_JSON_TYPE));

        List<Department> departments = response.readEntity(List.class);
        assertThat(departments.size(), is(databaseSize));
    }

    @Test
    @InSequence(3)
    public void getDepartment() throws Exception {

        // Get the department
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", department.getId())).get();
        Department testDepartment = response.readEntity(Department.class);
        assertThat(response, hasStatus(Status.OK));
        assertThat(response, hasContentType(MediaType.APPLICATION_JSON_TYPE));
        assertThat(testDepartment.getId(), is(department.getId()));
        assertThat(testDepartment.getName(), is(DEFAULT_NAME));
    }

    @Test
    @InSequence(4)
    public void getNonExistingDepartment() throws Exception {

        // Get the department
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", Long.MAX_VALUE)).get();
        assertThat(response, hasStatus(Status.NOT_FOUND));
    }

    @Test
    @InSequence(5)
    public void updateDepartment() throws Exception {

        int databaseSizeBeforeUpdate = departmentFacade.findAll().size();

        // Update the department
        Department updatedDepartment = new Department();
        updatedDepartment.setId(department.getId());
        updatedDepartment.setName(UPDATED_NAME);

        Response response = target(RESOURCE_PATH).put(json(updatedDepartment));
        assertThat(response, hasStatus(Status.OK));

        // Validate the Department in the database
        List<Department> departments = departmentFacade.findAll();
        assertThat(departments.size(), is(databaseSizeBeforeUpdate));
        Department testDepartment = departments.get(departments.size() - 1);
        assertThat(testDepartment.getName(), is(UPDATED_NAME));
    }

    @Test
    @InSequence(6)
    public void removeDepartment() throws Exception {

        int databaseSizeBeforeDelete = departmentFacade.findAll().size();

        // Delete the department
        Response response = target(RESOURCE_PATH + "/{id}", singletonMap("id", department.getId())).delete();
        assertThat(response, hasStatus(Status.OK));

        // Validate the database is empty
        List<Department> departments = departmentFacade.findAll();
        assertThat(departments.size(), is(databaseSizeBeforeDelete - 1));
    }

}
