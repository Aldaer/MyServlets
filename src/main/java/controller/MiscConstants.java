package controller;

@SuppressWarnings("WeakerAccess")
public interface MiscConstants {
    String DEFAULT_LOCALE = "en";

    int MIN_USERNAME_LENGTH = 4;
    int MAX_USERNAME_LENGTH = 50;
    int MIN_PASSWORD_LENGTH = 3;
}

/**
 * Attributes stores in Request, Session and ServletContext
 */
interface AttributeNames {
    class R {
        static String USER_FOUND = "userFound";
        static String REG_ATTEMPT = "regAttempt";
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
 * Request parameter names
 */
interface ParameterNames {
    String LANGUAGE = "language";
    String ACTION = "action";
    String L_USERNAME = "j_username";
    String L_PASSWORD = "j_password";
    String L_PASSWORD2 = "j_password2";
}

/**
 * Page and servlet URLs
 */
interface PageURLs {
    String LOGIN_PAGE = "/WEB-INF/login.jsp";
    String MAIN_PAGE = "/WEB-INF/mainpage.jsp";

    String DETAILS_PAGE = "/main/userdetails";      // actually, /WEB-INF/userdetails.jsp - bound through web.xml

    String MAIN_SERVLET = "/main/serv";
    String LOGIN_SERVLET = "/doLogin";
    String REGISTER_SERVLET = "/doRegister";
}
