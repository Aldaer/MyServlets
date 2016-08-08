package controller;

@SuppressWarnings("WeakerAccess")
public interface MiscConstants {
    String DEFAULT_LOCALE = "en";

    int MIN_USERNAME_LENGTH = 4;
    int MAX_USERNAME_LENGTH = 50;
    int MIN_PASSWORD_LENGTH = 3;

    Long[] UNREAD_PRIVATE = {0L};
    Long[] READ_PRIVATE = {-1L};
    Long[] ALL_PRIVATE = {-1L, 0L};

    String JSON_TYPE = "application/json";
    String JSON_TIME_FORMAT = "yyyy-MM-dd’T'HH:mm:ss.SSSZ";

}

/**
 * Attributes stores in (R)equest, (S)ession and Servlet(C)ontext
 */
interface AttributeNames {
    class R {
        static final String USER_FOUND = "userFound";
        static final String REG_ATTEMPT = "regAttempt";

        static final String UNREAD_PM = "unreadPM";
        static final String FRIEND_STRING = "friendString";
    }

    class S {
        static final String USER = "currentUser";
    }

    class C {
        static final String USER_DAO = "userDAO";
        static final String CREDS_DAO = "credsDAO";
        static final String MSG_DAO = "messageDAO";
        static final String CONV_DAO = "convDAO";

        static final String LANGUAGE = "language";
        static final String CONTAINER_AUTH = "authByContainer";
    }
}

/**
 * Request parameter names for (L)ogin/Register form
 */
interface ParameterNames {
    String LANGUAGE = "language";
    String USERNAME = "j_username";
    String PASSWORD = "j_password";
    String PASSWORD2 = "j_password2";
}

/**
 * Page URLs and servlet mappings
 */
interface PageURLs {
    String LOGIN_PAGE = "/WEB-INF/login.jsp";
    String MAIN_PAGE = "/WEB-INF/mainpage.jsp";

    String SECURED_AREA = "/main/*";
    String DETAILS_PAGE = "/main/userdetails";          // actually, /WEB-INF/userdetails.jsp - bound through web.xml
    String CONVERSATIONS_PAGE = "/main/conversations";   // actually, /WEB-INF/conversations.jsp - bound through web.xml
    String LOGOUT = "/main/logout";                     // performed by SecurityFilter

    String MAIN_SERVLET = "/main/serv";
    String LOGIN_SERVLET = "/doLogin";
    String REGISTER_SERVLET = "/doRegister";
    String USER_UPDATE_SERVLET = "/main/updateUser";
    String MESSAGE_PROVIDER_SERVLET = "/main/messages";
    String CONVERSATION_PROVIDER_SERVLET = "/main/conversations";
    String MESSAGE_ACTION_SERVLET = "/main/messageAction";
    String USER_SEARCH_SERVLET = "/main/userSearch";
}
