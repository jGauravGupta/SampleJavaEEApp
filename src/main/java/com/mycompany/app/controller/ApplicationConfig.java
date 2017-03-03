/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.app.controller;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import javax.ws.rs.core.Application;

/**
 *
 * @author BGH43766
 */
@javax.ws.rs.ApplicationPath("resources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }


    @Override
    public Set<Object> getSingletons() {
        final Set<Object> instances = new HashSet<>();
        instances.add(new JacksonJsonProvider());
        return instances;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.mycompany.app.controller.AccountController.class);
        resources.add(com.mycompany.app.controller.DepartmentController.class);
        resources.add(com.mycompany.app.controller.EmployeeController.class);
        resources.add(com.mycompany.app.controller.UserController.class);
        resources.add(com.mycompany.app.controller.UserJWTController.class);
        resources.add(com.mycompany.app.security.SecurityUtils.class);
        resources.add(com.mycompany.app.security.jwt.JWTAuthenticationFilter.class);
    }
    
}
