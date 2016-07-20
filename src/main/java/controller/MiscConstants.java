package controller;

public interface MiscConstants {
    String DEFAULT_LOCALE = "en";
}

/**
 * Attributes stores in Session and ServletContext
 */
interface ContextAttributeNames {
    String USER = "currentUser";

    String USER_DAO = "userDAO";
    String CREDS_DAO = "credsDAO";
    String LANGUAGE = "language";
    String CONTAINER_AUTH = "authByContainer";
}

/**
 * Page and servlet URLs
 */
interface PageURLs {
    String LOGIN_PAGE = "/login.jsp";
    String MAIN_PAGE = "/main/response.jsp";
    String MAIN_SERVLET = "/main/serv";
}
