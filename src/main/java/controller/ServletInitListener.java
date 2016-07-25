package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.CredentialsDAO;
import model.dao.DatabaseDAO;
import model.dao.GlobalDAO;
import model.dao.databases.dbconnecton.ConnectionPool;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static controller.AttributeNames.C.*;
import static java.util.Optional.ofNullable;

/**
 * This is the main app initializing listener
 */
@Slf4j
@WebListener()
public class ServletInitListener implements ServletContextListener /* , HttpSessionListener, HttpSessionAttributeListener*/ {
    private static final String CONFIG_BUNDLE = "config";
    private static final String CONFIG_DAO_CLASS = "dao_class";
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

        final ServletContext srvContext = sce.getServletContext();
        log.info("Initializing context {}", srvContext.getContextPath());
        log.info("Root real path = {}", srvContext.getRealPath("/"));
        Collections.list(srvContext.getInitParameterNames()).forEach(par -> log.info("{} = {}", par, srvContext.getInitParameter(par)));

        ResourceBundle conf = ResourceBundle.getBundle(CONFIG_BUNDLE);

        GlobalDAO globalDao;
        try {
            String userDaoClass = conf.getString(CONFIG_DAO_CLASS);
            globalDao = (GlobalDAO) Class.forName(userDaoClass).newInstance();
        } catch (MissingResourceException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Global DAO config error", e);
        }

        if (globalDao instanceof DatabaseDAO) {
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
            ((DatabaseDAO) globalDao).useConnectionSource(connectionPool);
        }

        CredentialsDAO credsDao = globalDao.getCredentialsDAO();
        credsDao.useSaltedHash(conf.getString(CONFIG_DATABASE_USE_SHA_DIGEST).toLowerCase().equals("true"));
        // Cancel all unfinished registration attempts
        credsDao.purgeTemporaryUsers(System.currentTimeMillis());

        // Put data access objects and config parameters into servlet context
        srvContext.setAttribute(CREDS_DAO, credsDao);
        srvContext.setAttribute(USER_DAO, globalDao.getUserDAO());
        srvContext.setAttribute(MSG_DAO, globalDao.getMessageDAO());

        Boolean contAuth = conf.getString(CONFIG_CONTAINER_SECURITY).toLowerCase().equals("true");
        srvContext.setAttribute(CONTAINER_AUTH, contAuth);
        log.info("Container-based authentication = {}", contAuth);

        if (conf.containsKey("autologin")) srvContext.setAttribute("AUTOLOGIN", conf.getString("autologin")); // TODO: remove in production
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
        ofNullable(connectionPool).ifPresent(ConnectionPool::close);
    }
}
