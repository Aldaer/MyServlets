package controller;

import model.dao.UserDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static controller.AttributeNames.USER_DAO;

/**
 * This is the main app initializing listener
 */
@WebListener()
public class FirstServletContextListener implements ServletContextListener /* , HttpSessionListener, HttpSessionAttributeListener*/ {

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed).
         You can initialize servlet context related data here.
      */
        UserDAO userDAO;
        try {
            String daoClass = ResourceBundle.getBundle("config").getString("user_dao_class");
            userDAO = (UserDAO) Class.forName(daoClass).newInstance();
        } catch (MissingResourceException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("User DAO config error", e);
        }
        sce.getServletContext().setAttribute(USER_DAO, userDAO);
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
    }
}
