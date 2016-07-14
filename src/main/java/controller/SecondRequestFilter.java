package controller;

import lombok.extern.log4j.Log4j2;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * My first attempt on HTTP Filters
 */
@Log4j2
@WebFilter(filterName = "Filter2nd")
public class SecondRequestFilter extends HttpFilter {
    private int n = 0;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.trace("(2)Filtering request {}", ++n);
        super.doFilter(req, res, chain);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);

        log.debug("Initializing filter 2");
        log.debug("Filter name = " + config.getFilterName());
    }
}
