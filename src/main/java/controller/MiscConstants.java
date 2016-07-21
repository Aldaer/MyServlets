package controller;

@SuppressWarnings("WeakerAccess")
public interface MiscConstants {
    String DEFAULT_LOCALE = "en";
}

/**
 * Attributes stores in Request, Session and ServletContext
 */
interface AttributeNames {
    class R {
        static String USER_FOUND = "userFound";
    }

    class S {
        static String USER = "currentUser";
    }

    class C {
        static String USER_DAO = "userDAO";
        static String CREDS_DAO = "credsDAO";
        static String LANGUAGE = "language";
        static String CONTAINER_AUTH = "authByContainer";
    }
}

/**
 * Page and servlet URLs
 */
interface PageURLs {
    String LOGIN_PAGE = "/main/login.jsp";
    String MAIN_PAGE = "/main/response.jsp";
    String MAIN_SERVLET = "/main/serv";
}
