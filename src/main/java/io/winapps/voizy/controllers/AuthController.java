package io.winapps.voizy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.winapps.voizy.models.auth.LoginRequest;
import io.winapps.voizy.models.auth.LoginResponse;
import io.winapps.voizy.services.AuthService;
import io.winapps.voizy.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!req.getMethod().equals("POST")) {
            res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid request method");
            return;
        }

        try {
            LoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), LoginRequest.class);

            if (loginRequest.getEmail() == null && loginRequest.getUsername() == null) {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Missing required body params. Either 'email' or 'username' must be provided.");
                return;
            }

            LoginResponse loginResponse = authService.login(loginRequest);

            if (!loginResponse.isPasswordCorrect()) {
                logger.warn("INVALID PASSWORD ATTEMPTED for username: {}, email: {}",
                        loginRequest.getUsername(), loginRequest.getEmail());
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Email, Username, or Password is incorrect.");
                return;
            }

            res.setContentType("application/json");
            objectMapper.writeValue(res.getOutputStream(), loginResponse);

        } catch (Exception e) {
            logger.error("Error processing login request", e);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error logging in: " + e.getMessage());
        }
    }

    public BiConsumer<HttpServletRequest, HttpServletResponse> loginHandler() {
        return (req, res) -> {
            try {
                login(req, res);
            } catch (IOException e) {
                logger.error("IO error in login handler", e);
                throw new RuntimeException("Error handling login request", e);
            }
        };
    }

    public HttpServlet loginServlet() {
        return new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    login(req, resp);
                } catch (IOException e) {
                    logger.error("IO error in login servlet", e);
                    throw new RuntimeException("Error handling login request", e);
                }
            }
        };
    }

//    public HttpServlet loginServlet() {
//        return new HttpServlet() {
//            @Override
//            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//                login(req, resp);
//            }
//        };
//    }
}
