package io.winapps.voizy;

import io.winapps.voizy.controllers.AuthController;
import io.winapps.voizy.controllers.UserController;
import io.winapps.voizy.database.DatabaseManager;
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

            UserController userController = new UserController();
            AuthController authController = new AuthController();

            context.addServlet(new ServletHolder(userController.createUserServlet()), "/users/create");
            context.addServlet(new ServletHolder(authController.loginServlet()), "/users/login");

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
}
