package com.mycompany.app.service.facade;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.inject.Inject;
import com.mycompany.app.domain.Department;

@Stateless
@Named("department")
public class DepartmentFacade extends AbstractFacade<Department, Long> {

    @Inject
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DepartmentFacade() {
        super(Department.class);
    }

}
