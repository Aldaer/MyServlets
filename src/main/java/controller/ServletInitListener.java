package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.CredentialsDAO;
import model.dao.DatabaseDAO;
import model.dao.GlobalDAO;
import model.dao.UserDAO;
import model.dao.databases.dbconnecton.ConnectionPool;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static controller.ContextAttributeNames.*;

/**
 * This is the main app initializing listener
 */
@Slf4j
@WebListener()
public class ServletInitListener implements ServletContextListener /* , HttpSessionListener, HttpSessionAttributeListener*/ {
    private static final String CONFIG_BUNDLE = "config";
    private static final String CONFIG_DATABASE_DAO = "dao_class";
    private static final String CONFIG_DATABASE_URI = "database_uri";
    private static final String CONFIG_DATABASE_DRIVER = "database_driver";
    private static final String CONFIG_DATABASE_USER = "username";
    private static final String CONFIG_DATABASE_PASSWORD = "password";
    private static final String CONFIG_DATABASE_USE_SHA_DIGEST = "sha_digest";
    private static final String CONFIG_CONTAINER_SECURITY = "container_security";           // "true" = container-based; "false" = own

    private ConnectionPool connectionPool;

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed).
         You can initialize servlet context related data here.
      */
        ResourceBundle conf = ResourceBundle.getBundle(CONFIG_BUNDLE);
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

        GlobalDAO globalDao;
        try {
            String userDaoClass = conf.getString(CONFIG_DATABASE_DAO);
            globalDao = (GlobalDAO) Class.forName(userDaoClass).newInstance();
        } catch (MissingResourceException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Global DAO config error", e);
        }
        if (globalDao instanceof DatabaseDAO) ((DatabaseDAO) globalDao).useConnectionSource(connectionPool);

        CredentialsDAO credsDao = globalDao.instantiateCredentialsDAO();
        credsDao.useSaltedHash(conf.getString(CONFIG_DATABASE_USE_SHA_DIGEST).toLowerCase().equals("true"));
        UserDAO uDao = globalDao.instantiateUserDAO();

        // Put data access objects and config parameters into servlet context
        final ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute(USER_DAO, uDao);
        servletContext.setAttribute(CREDS_DAO, credsDao);

        servletContext.setAttribute(CONTAINER_AUTH, conf.getString(CONFIG_CONTAINER_SECURITY));

    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
        connectionPool.close();
    }
}
