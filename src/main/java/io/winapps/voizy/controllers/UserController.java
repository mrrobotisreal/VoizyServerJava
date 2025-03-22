package io.winapps.voizy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.users.CreateUserRequest;
import io.winapps.voizy.models.users.CreateUserResponse;
import io.winapps.voizy.services.UserService;
import io.winapps.voizy.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void create(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("POST")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            CreateUserRequest createRequest = objectMapper.readValue(req.getInputStream(), CreateUserRequest.class);

            CreateUserResponse response = userService.createUser(createRequest);

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), response);

        } catch (Exception e) {
            logger.error("Error creating user", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating the user: " + e.getMessage());
        }
    }

    public BiConsumer<HttpServletRequest, HttpServletResponse> createUserHandler() {
        return (req, res) -> {
            try {
                create(req, res);
            } catch (IOException e) {
                logger.error("IO error in create user handler", e);
                throw new RuntimeException("Error handling create user request", e);
            }
        };
    }

    public HttpServlet createUserServlet() {
        return new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    create(req, resp);
                } catch (IOException e) {
                    logger.error("IO error in create user servlet", e);
                    throw new RuntimeException("Error handling create user request", e);
                }
            }
        };
    }

//    public HttpServlet createUserServlet() {
//        return new HttpServlet() {
//            @Override
//            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//                create(req, resp);
//            }
//        };
//    }
}
