package com.mycompany.app.controller;

import com.mycompany.app.service.facade.AuthorityFacade;
import com.mycompany.app.service.facade.UserFacade;
import com.mycompany.app.domain.User;
import com.mycompany.app.service.mail.MailService;
import com.mycompany.app.service.UserService;
import com.mycompany.app.controller.dto.ManagedUserDTO;
import com.mycompany.app.controller.dto.UserDTO;
import com.mycompany.app.controller.util.HeaderUtil;
import com.mycompany.app.controller.util.Page;
import com.mycompany.app.controller.util.PaginationUtil;
import com.mycompany.app.security.Secured;
import com.mycompany.app.security.AuthoritiesConstants;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * REST controller for managing users.
 *
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of
 * authorities.</p>
 */
@Path("/api")
public class UserController {

    @Inject
    private Logger log;

    @Inject
    private UserFacade userFacade;

    @Inject
    private MailService mailService;

    @Inject
    private AuthorityFacade authorityFacade;

    @Inject
    private UserService userService;

    @Context
    private HttpServletRequest request;

    /**
     * POST /users : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends
     * an mail with an activation link. The user needs to be activated on
     * creation.
     * </p>
     *
     * @param managedUserDTO the user to create
     * @return the Response with status 201 (Created) and with body the new
     * user, or with status 400 (Bad Request) if the login or email is already
     * in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @Path(value = "/users")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(AuthoritiesConstants.ADMIN)
    public Response createUser(ManagedUserDTO managedUserDTO) throws URISyntaxException {
        log.debug("REST request to save User : {}", managedUserDTO);

        //Lowercase the user login before comparing with database
        if (userFacade.findOneByLogin(managedUserDTO.getLogin().toLowerCase()).isPresent()) {
            return HeaderUtil.createFailureAlert(Response.status(BAD_REQUEST), "userManagement", "userexists", "Login already in use").build();
        } else if (userFacade.findOneByEmail(managedUserDTO.getEmail()).isPresent()) {
            return HeaderUtil.createFailureAlert(Response.status(BAD_REQUEST), "userManagement", "emailexists", "Email already in use").build();
        } else {
            User newUser = userService.createUser(managedUserDTO);
            String baseUrl = request.getScheme()
                    + // "http"
                    "://"
                    + // "://"
                    request.getServerName()
                    + // "myhost"
                    ":"
                    + // ":"
                    request.getServerPort()
                    + // "80"
                    request.getContextPath();
            // "/myContextPath" or "" if deployed in root context

            mailService.sendCreationEmail(newUser, baseUrl);
            return HeaderUtil.createAlert(Response.created(new URI("/resources/api/users/" + newUser.getLogin())),
                    "userManagement.created", newUser.getLogin()).entity(new UserDTO(newUser)).build();
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param managedUserDTO the user to update
     * @return the Response with status 200 (OK) and with body the updated user,
     * or with status 400 (Bad Request) if the login or email is already in use,
     * or with status 500 (Internal Server Error) if the user couldn't be
     * updated
     */
    @Path(value = "/users")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(AuthoritiesConstants.ADMIN)
    public Response updateUser(ManagedUserDTO managedUserDTO) {
        log.debug("REST request to update User : {}", managedUserDTO);
        Optional<User> existingUser = userFacade.findOneByEmail(managedUserDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(managedUserDTO.getId()))) {
            return HeaderUtil.createFailureAlert(Response.status(BAD_REQUEST), "userManagement", "emailexists", "E-mail already in use").build();
        }
        existingUser = userFacade.findOneByLogin(managedUserDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(managedUserDTO.getId()))) {
            return HeaderUtil.createFailureAlert(Response.status(BAD_REQUEST), "userManagement", "userexists", "Login already in use").build();
        }
        return userFacade
                .findOneById(managedUserDTO.getId())
                .map(user -> {
                    user.setLogin(managedUserDTO.getLogin());
                    user.setFirstName(managedUserDTO.getFirstName());
                    user.setLastName(managedUserDTO.getLastName());
                    user.setEmail(managedUserDTO.getEmail());
                    user.setActivated(managedUserDTO.isActivated());
                    user.setLangKey(managedUserDTO.getLangKey());
                    user.setAuthorities(managedUserDTO.getAuthorities().stream().map(authorityFacade::find).collect(toSet()));
                    userFacade.edit(user);
                    return HeaderUtil.createAlert(Response.ok(new ManagedUserDTO(userFacade
                            .find(managedUserDTO.getId()))), "userManagement.updated", managedUserDTO.getLogin())
                            .build();
                })
                .orElseGet(() -> Response.status(INTERNAL_SERVER_ERROR).build());

    }

    /**
     * GET /users : get all users.
     *
     * @param page the pagination information
     * @param size the pagination size information
     * @return the Response with status 200 (OK) and with body all users
     * @throws URISyntaxException if the pagination headers couldn't be
     * generated
     */
    @Path(value = "/users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getAllUsers(@QueryParam("page") int page, @QueryParam("size") int size) throws URISyntaxException {
        List<User> userList = userFacade.getUsersWithAuthorities(page * size, size);
        List<ManagedUserDTO> managedUserDTOs = userList.stream()
                .map(ManagedUserDTO::new)
                .collect(toList());

        ResponseBuilder builder = Response.ok(managedUserDTOs);
        PaginationUtil.generatePaginationHttpHeaders(builder, new Page(page, size, userFacade.count()), "/resources/api/users");
        return builder.build();
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the Response with status 200 (OK) and with body the "login" user,
     * or with status 404 (Not Found)
     */
    @Path(value = "/users/{login}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUser(@PathParam("login") String login) {
        log.debug("REST request to get User : {}", login);
        return userService.getUserWithAuthoritiesByLogin(login)
                .map(ManagedUserDTO::new)
                .map(managedUserDTO -> Response.ok(managedUserDTO).build())
                .orElse(Response.status(NOT_FOUND).build());
    }

    /**
     * DELETE USER :login : remove the "login" User.
     *
     * @param login the login of the user to remove
     * @return the Response with status 200 (OK) or with status 404 (Not Found)
     */
    @Path(value = "/users/{login}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(AuthoritiesConstants.ADMIN)
    public Response deleteUser(@PathParam("login") String login) {
        log.debug("REST request to delete User: {}", login);
        userService.deleteUserInformation(login);
        return HeaderUtil.createAlert(Response.ok(), "userManagement.deleted", login).build();
    }
}
