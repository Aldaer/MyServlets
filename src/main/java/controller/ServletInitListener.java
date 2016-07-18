package controller;

import dbconnecton.ConnectionPool;
import lombok.extern.slf4j.Slf4j;
import model.dao.UserDAO;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static controller.AttributeNames.CONTAINER_AUTH;
import static controller.AttributeNames.USER_DAO;
import static model.dao.DaoGeneral.*;

/**
 * This is the main app initializing listener
 */
@Slf4j
@WebListener()
public class ServletInitListener implements ServletContextListener /* , HttpSessionListener, HttpSessionAttributeListener*/ {
    private ConnectionPool connectionPool;

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed).
         You can initialize servlet context related data here.
      */
        ResourceBundle conf = ResourceBundle.getBundle("config");
        String uri = conf.getString(CONFIG_DATABASE_URI);
        String drv = conf.getString(CONFIG_DATABASE_DRIVER);
        log.info("Creating connection pool with driver {}, uri {}", drv, uri);
        String un = conf.getString(CONFIG_DATABASE_USER);
        String pwd = conf.getString(CONFIG_DATABASE_PASSWORD);
        connectionPool = ConnectionPool.builder()
                .withDriver(drv)
                .withUrl(uri)
                .withUserName(un)
                .withPassword(pwd)
                .withLogger(LoggerFactory.getLogger(ConnectionPool.class))
                .create();

        UserDAO userDAO;
        try {
            String daoClass = conf.getString(CONFIG_DATABASE_USER_DAO);
            userDAO = (UserDAO) Class.forName(daoClass).newInstance();
            userDAO.useConnectionSource(connectionPool);
            userDAO.useSaltedHash(Boolean.valueOf(conf.getString(CONFIG_DATABASE_USE_SHA_DIGEST)));
        } catch (MissingResourceException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("User DAO config error", e);
        }

        sce.getServletContext().setAttribute(CONTAINER_AUTH, conf.getString(CONFIG_CONTAINER_SECURITY));

        // Put data access objects into servlet context
        sce.getServletContext().setAttribute(USER_DAO, userDAO);
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
        connectionPool.close();
    }
}
