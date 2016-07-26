package controller;

@SuppressWarnings("WeakerAccess")
public interface MiscConstants {
    String DEFAULT_LOCALE = "en";

    int MIN_USERNAME_LENGTH = 4;
    int MAX_USERNAME_LENGTH = 50;
    int MIN_PASSWORD_LENGTH = 3;

    Long UNREAD_PRIVATE = 0L;
}

/**
 * Attributes stores in (R)equest, (S)ession and Servlet(C)ontext
 */
interface AttributeNames {
    class R {
        static final String USER_FOUND = "userFound";
        static final String REG_ATTEMPT = "regAttempt";

        static final String UNREAD_PM = "unreadPM";
    }

    class S {
        static final String USER = "currentUser";
    }

    class C {
        static final String USER_DAO = "userDAO";
        static final String CREDS_DAO = "credsDAO";
        static final String MSG_DAO = "messageDAO";

        static final String LANGUAGE = "language";
        static final String CONTAINER_AUTH = "authByContainer";
    }
}

/**
 * Request parameter names: general, as well as
 * specific for (L)ogin/Register and (U)pdateUser forms
 */
interface ParameterNames {
    String LANGUAGE = "language";
    class L {
        static final String USERNAME = "j_username";
        static final String PASSWORD = "j_password";
        static final String PASSWORD2 = "j_password2";
    }
    class U {
        static final String FULLNAME = "fullname";
        static final String EMAIL = "email";
    }
}

/**
 * Page URLs and servlet mappings
 */
interface PageURLs {
    String LOGIN_PAGE = "/WEB-INF/login.jsp";
    String MAIN_PAGE = "/WEB-INF/mainpage.jsp";

    String SECURED_AREA = "/main/*";
    String DETAILS_PAGE = "/main/userdetails";      // actually, /WEB-INF/userdetails.jsp - bound through web.xml
    String LOGOUT = "/main/logout";                 // performed by SecurityFilter

    String MAIN_SERVLET = "/main/serv";
    String LOGIN_SERVLET = "/doLogin";
    String REGISTER_SERVLET = "/doRegister";
    String USER_UPDATE_SERVLET = "/main/updateUser";
    String MESSAGE_SERVLET = "/main/messages";
}
