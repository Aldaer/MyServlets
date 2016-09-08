package controller;

import lombok.extern.slf4j.Slf4j;
import model.dao.CredentialsDAO;
import model.dao.DatabaseDAO;
import model.dao.GlobalDAO;
import model.dao.databases.dbconnecton.ConnectionPool;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static controller.AttributeNames.C.*;
import static java.util.Optional.ofNullable;

/**
 * This is the main app initializing listener
 */
@Slf4j
@WebListener
public class ServletInitListener implements ServletContextListener /* , HttpSessionListener, HttpSessionAttributeListener*/ {
    public static final String CONFIG_BUNDLE = "config";
    private static final String CONFIG_DATABASE_USE_SHA_DIGEST = "sha_digest";
    private static final String CONFIG_CONTAINER_SECURITY = "container_security";           // "true" = container-based; "false" = own

    private ConnectionPool cPool = null;                                                    // To close on shutdown

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed).
         You can initialize servlet context related data here.
      */
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
        GlobalDAO globalDao = (GlobalDAO) context.getBean("globalDao");
        if (globalDao instanceof DatabaseDAO) {
            Supplier<Connection> connectionSource = ((DatabaseDAO) globalDao).getCurrentConnectionSource();
            if (connectionSource instanceof ConnectionPool) cPool = (ConnectionPool) connectionSource;
        }

        final ServletContext srvContext = sce.getServletContext();
        log.info("Initializing context {}", srvContext.getContextPath());
        log.info("Root real path = {}", srvContext.getRealPath("/"));
        Collections.list(srvContext.getInitParameterNames()).forEach(par -> log.info("{} = {}", par, srvContext.getInitParameter(par)));

        ResourceBundle conf = ResourceBundle.getBundle(CONFIG_BUNDLE);

        CredentialsDAO credsDao = globalDao.getCredentialsDAO();
        credsDao.useSaltedHash(conf.getString(CONFIG_DATABASE_USE_SHA_DIGEST).toLowerCase().equals("true"));
        // Cancel all unfinished registration attempts
        credsDao.purgeTemporaryUsers(System.currentTimeMillis());

        // Put data access objects and config parameters into servlet context
        srvContext.setAttribute(CREDS_DAO, credsDao);
        srvContext.setAttribute(USER_DAO, globalDao.getUserDAO());
        srvContext.setAttribute(MSG_DAO, globalDao.getMessageDAO());
        srvContext.setAttribute(CONV_DAO, globalDao.getConversationDAO());


        Boolean contAuth = conf.getString(CONFIG_CONTAINER_SECURITY).toLowerCase().equals("true");
        srvContext.setAttribute(CONTAINER_AUTH, contAuth);
        log.info("Container-based authentication = {}", contAuth);

        if (conf.containsKey("autologin")) srvContext.setAttribute("AUTOLOGIN", conf.getString("autologin")); // TODO: remove in production
        if (conf.containsKey("show_legacy") && conf.getString("show_legacy").toLowerCase().equals("true"))
            srvContext.setAttribute("showLegacy", Boolean.TRUE);
    }

    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context
         (the Web application) is undeployed or
         Application Server shuts down.
      */
        ofNullable(cPool).ifPresent(ConnectionPool::close);
    }
}
