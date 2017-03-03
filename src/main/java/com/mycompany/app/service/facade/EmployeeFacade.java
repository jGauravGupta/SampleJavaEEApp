package com.mycompany.app.service.facade;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.inject.Inject;
import com.mycompany.app.domain.Employee;

@Stateless
@Named("employee")
public class EmployeeFacade extends AbstractFacade<Employee, Long> {

    @Inject
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmployeeFacade() {
        super(Employee.class);
    }

}
