package io.winapps.voizy.handlers.users.servlets;

import io.winapps.voizy.handlers.users.UserHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CreateUserServlet extends HttpServlet {
    private final UserHandler userHandler;

    public CreateUserServlet(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        userHandler.createUser(req, resp);
    }
}
