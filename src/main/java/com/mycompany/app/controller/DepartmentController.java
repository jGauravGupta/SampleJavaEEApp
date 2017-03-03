package com.mycompany.app.controller;

import com.mycompany.app.domain.Department;
import com.mycompany.app.service.facade.DepartmentFacade;
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
 * REST controller for managing Department.
 */
@Path("/api/department")
@Secured
public class DepartmentController {

    @Inject
    private Logger log;

    @Inject
    private DepartmentFacade departmentFacade;

    /**
     * POST : Create a new department.
     *
     * @param department the department to create
     * @return the Response with status 201 (Created) and with body the new
     * department, or with status 400 (Bad Request) if the department has
     * already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @POST
    public Response createDepartment(Department department) throws URISyntaxException {
        log.debug("REST request to save Department : {}", department);
        departmentFacade.create(department);
        return HeaderUtil.createEntityCreationAlert(Response.created(new URI("/resources/api/department/" + department.getId())),
                "department", department.getId().toString())
                .entity(department).build();
    }

    /**
     * PUT : Updates an existing department.
     *
     * @param department the department to update
     * @return the Response with status 200 (OK) and with body the updated
     * department, or with status 400 (Bad Request) if the department is not
     * valid, or with status 500 (Internal Server Error) if the department
     * couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PUT
    public Response updateDepartment(Department department) throws URISyntaxException {
        log.debug("REST request to update Department : {}", department);
        departmentFacade.edit(department);
        return HeaderUtil.createEntityUpdateAlert(Response.ok(), "department", department.getId().toString())
                .entity(department).build();
    }

    /**
     * GET : get all the departments.
     *
     * @return the Response with status 200 (OK) and the list of departments in
     * body
     *
     */
    @GET
    public List<Department> getAllDepartments() {
        log.debug("REST request to get all Departments");
        List<Department> departments = departmentFacade.findAll();
        return departments;
    }

    /**
     * GET /:id : get the "id" department.
     *
     * @param id the id of the department to retrieve
     * @return the Response with status 200 (OK) and with body the department,
     * or with status 404 (Not Found)
     */
    @Path("/{id}")
    @GET
    public Response getDepartment(@PathParam("id") Long id) {
        log.debug("REST request to get Department : {}", id);
        Department department = departmentFacade.find(id);
        return Optional.ofNullable(department)
                .map(result -> Response.status(Response.Status.OK).entity(department).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * DELETE /:id : remove the "id" department.
     *
     * @param id the id of the department to delete
     * @return the Response with status 200 (OK)
     */
    @Path("/{id}")
    @DELETE
    public Response removeDepartment(@PathParam("id") Long id) {
        log.debug("REST request to delete Department : {}", id);
        departmentFacade.remove(departmentFacade.find(id));
        return HeaderUtil.createEntityDeletionAlert(Response.ok(), "department", id.toString()).build();
    }

}
