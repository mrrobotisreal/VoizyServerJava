package io.winapps.voizy;

import io.winapps.voizy.controllers.AuthController;
import io.winapps.voizy.controllers.PostController;
import io.winapps.voizy.controllers.UserController;
import io.winapps.voizy.database.DatabaseManager;
import io.winapps.voizy.middleware.AuthMiddleware;
import io.winapps.voizy.util.ServletAdapter;
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

            UserController userController = new UserController();
            AuthController authController = new AuthController();
            PostController postController = new PostController();
            AuthMiddleware authMiddleware = new AuthMiddleware();

            //-------------------------//
            //       User routes       //
            //-------------------------//
            // CreateUser
            HttpServlet protectedCreateUserServlet = ServletAdapter.biConsumerToServlet(
                    userController.createUserHandler()
            );
            context.addServlet(new ServletHolder(protectedCreateUserServlet), "/users/create");

            // Login
            HttpServlet protectedLoginServlet = ServletAdapter.biConsumerToServlet(
                    authController.loginHandler()
            );
            context.addServlet(new ServletHolder(protectedLoginServlet), "/users/login");

            //-------------------------//
            //       Post routes       //
            //-------------------------//
            // ListPosts
            HttpServlet protectedListPostsServlet = ServletAdapter.biConsumerToServlet(
                    authMiddleware.validateApiKey(postController.listPostsHandler())
            );
            context.addServlet(new ServletHolder(protectedListPostsServlet), "/posts/list");

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

    static HttpServlet createServlet(BiConsumerServlet handler) {
        return new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                handler.accept(req, resp);
            }
        };
    }

    @FunctionalInterface
    interface BiConsumerServlet {
        void accept(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }
}
