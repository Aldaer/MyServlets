package controller;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Gets called from registration form in login.jsp
 */
@Slf4j
@WebServlet("/registerUser")
public class RegisterServlet extends HttpServlet {
}
