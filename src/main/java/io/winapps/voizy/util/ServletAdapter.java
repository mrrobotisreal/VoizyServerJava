package io.winapps.voizy.util;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.function.BiConsumer;

public class ServletAdapter {
    public static BiConsumer<HttpServletRequest, HttpServletResponse> servletToBiConsumer(HttpServlet servlet) {
        return (req, res) -> {
            try {
                // Call the service method which will dispatch to the appropriate doXXX method
                servlet.service(req, res);
            } catch (Exception e) {
                throw new RuntimeException("Error executing servlet", e);
            }
        };
    }

    public static HttpServlet biConsumerToServlet(BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
        return new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                handler.accept(req, resp);
            }
        };
    }
}
