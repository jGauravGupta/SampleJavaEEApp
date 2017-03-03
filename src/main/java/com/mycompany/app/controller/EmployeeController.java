package com.mycompany.app.controller;

import com.mycompany.app.domain.Employee;
import com.mycompany.app.service.facade.EmployeeFacade;
import com.mycompany.app.controller.util.HeaderUtil;
import com.mycompany.app.security.Secured;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST controller for managing Employee.
 */
@Path("/api/employee")
@Secured
public class EmployeeController {

    @Inject
    private Logger log;

    @Inject
    private EmployeeFacade employeeFacade;

    /**
     * POST : Create a new employee.
     *
     * @param employee the employee to create
     * @return the Response with status 201 (Created) and with body the new
     * employee, or with status 400 (Bad Request) if the employee has already an
     * ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @POST
    public Response createEmployee(Employee employee) throws URISyntaxException {
        log.debug("REST request to save Employee : {}", employee);
        employeeFacade.create(employee);
        return HeaderUtil.createEntityCreationAlert(Response.created(new URI("/resources/api/employee/" + employee.getId())),
                "employee", employee.getId().toString())
                .entity(employee).build();
    }

    /**
     * PUT : Updates an existing employee.
     *
     * @param employee the employee to update
     * @return the Response with status 200 (OK) and with body the updated
     * employee, or with status 400 (Bad Request) if the employee is not valid,
     * or with status 500 (Internal Server Error) if the employee couldn't be
     * updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PUT
    public Response updateEmployee(Employee employee) throws URISyntaxException {
        log.debug("REST request to update Employee : {}", employee);
        employeeFacade.edit(employee);
        return HeaderUtil.createEntityUpdateAlert(Response.ok(), "employee", employee.getId().toString())
                .entity(employee).build();
    }

    /**
     * GET : get all the employees.
     *
     * @return the Response with status 200 (OK) and the list of employees in
     * body
     *
     */
    @GET
    public List<Employee> getAllEmployees() {
        log.debug("REST request to get all Employees");
        List<Employee> employees = employeeFacade.findAll();
        return employees;
    }

    /**
     * GET /:id : get the "id" employee.
     *
     * @param id the id of the employee to retrieve
     * @return the Response with status 200 (OK) and with body the employee, or
     * with status 404 (Not Found)
     */
    @Path("/{id}")
    @GET
    public Response getEmployee(@PathParam("id") Long id) {
        log.debug("REST request to get Employee : {}", id);
        Employee employee = employeeFacade.find(id);
        return Optional.ofNullable(employee)
                .map(result -> Response.status(Response.Status.OK).entity(employee).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * DELETE /:id : remove the "id" employee.
     *
     * @param id the id of the employee to delete
     * @return the Response with status 200 (OK)
     */
    @Path("/{id}")
    @DELETE
    public Response removeEmployee(@PathParam("id") Long id) {
        log.debug("REST request to delete Employee : {}", id);
        employeeFacade.remove(employeeFacade.find(id));
        return HeaderUtil.createEntityDeletionAlert(Response.ok(), "employee", id.toString()).build();
    }

}
