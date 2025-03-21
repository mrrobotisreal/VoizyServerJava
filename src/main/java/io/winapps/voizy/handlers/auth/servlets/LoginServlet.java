package io.winapps.voizy.handlers.auth.servlets;

import io.winapps.voizy.handlers.auth.AuthHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class LoginServlet extends HttpServlet {
    private final AuthHandler authHandler;

    public LoginServlet(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        authHandler.login(req, resp);
    }
}
