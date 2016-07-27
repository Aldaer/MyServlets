package controller;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RegisterServletTest {
    @Test
    public void loginValidTest() throws Exception {
        assertThat(RegisterServlet.userLoginValid("вася"), is(true));
        assertThat(RegisterServlet.userLoginValid("вася;"), is(false));

        assertThat(RegisterServlet.userLoginValid("вася петров"), is(false));
        assertThat(RegisterServlet.userLoginValid("вася-петров"), is(true));
        assertThat(RegisterServlet.userLoginValid("вася_петров"), is(true));
        assertThat(RegisterServlet.userLoginValid("вася.петров"), is(true));
    }
}