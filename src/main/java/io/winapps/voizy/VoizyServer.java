package io.winapps.voizy;

import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.handlers.auth.AuthHandler;
import io.winapps.voizy.handlers.auth.servlets.LoginServlet;
import io.winapps.voizy.handlers.users.UserHandler;
import io.winapps.voizy.handlers.users.servlets.CreateUserServlet;
import io.winapps.voizy.middleware.AuthMiddleware;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VoizyServer {
    private static final Logger logger = LoggerFactory.getLogger(VoizyServer.class);

    public static void main(String[] args) {
        boolean useHttps = !"false".equalsIgnoreCase(System.getenv("USE_HTTPS"));
        int port = determinePort(args);

        try {
            DatabaseManager.initMySQL();
            // Later init Redis as well

            Server server = configureServer(useHttps, port);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            AuthHandler authHandler = new AuthHandler();
            UserHandler userHandler = new UserHandler();
            AuthMiddleware authMiddleware = new AuthMiddleware();

            registerServlets(context, authHandler, userHandler, authMiddleware);

            server.start();
            logger.info("Server started on port {}", port);
            logger.info("Using {}", useHttps ? "HTTPS" : "HTTP");
            server.join();
        } catch (Exception e) {
            logger.error("Server initialization error", e);
            System.exit(1);
        } finally {
            DatabaseManager.close();
        }
    }

    private static int determinePort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // Ignore and use default or env variable
            }
        }

        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                // Ignore and use default
            }
        }

        boolean useHttps = !"false".equalsIgnoreCase(System.getenv("USE_HTTPS"));
        return useHttps ? 443 : 8282;
    }

    private static Server configureServer(boolean useHttps, int port) {
        Server server = new Server();

        if (useHttps) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSecureScheme("https");
            httpConfig.setSecurePort(443);
            httpConfig.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath("/etc/letsencrypt/voizy.me/keystore.jks");
            sslContextFactory.setKeyStorePassword(System.getenv("KEYSTORE_PASSWORD"));

            ServerConnector sslConnector = new ServerConnector(
                    server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(httpConfig)
            );
            sslConnector.setPort(443);

            server.addConnector(sslConnector);
        } else {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(port);
            server.addConnector(connector);
        }

        return server;
    }

    private static void registerServlets(ServletContextHandler context,
                                         AuthHandler authHandler,
                                         UserHandler userHandler,
                                         AuthMiddleware authMiddleware) {
        context.addServlet(new ServletHolder(createServlet(userHandler::createUser)), "/users/create");
        context.addServlet(new ServletHolder(createServlet(authHandler::login)), "/users/login");
    }

    private static HttpServlet createServlet(BiConsumerServlet handler) {
        return new HttpServlet() {
            @Override
            public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                handler.accept(req, resp);
            }
        };
    }

    @FunctionalInterface
    interface BiConsumerServlet {
        void accept(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }
}
