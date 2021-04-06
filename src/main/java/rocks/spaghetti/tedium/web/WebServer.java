package rocks.spaghetti.tedium.web;

import fi.iki.elonen.NanoHTTPD;
import rocks.spaghetti.tedium.util.Log;
import rocks.spaghetti.tedium.util.Util;
import rocks.spaghetti.tedium.config.ModConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class WebServer {
    private HTTPd server;
    private boolean isRunning = false;

    public void startServer() throws BindException {
        if (isRunning) {
            Log.error("Local web server is already running!");
            return;
        }

        server = new HTTPd(ModConfig.getWebServerPort());
        isRunning = true;
    }

    public void stopServer() {
        if (!isRunning) {
            Log.error("Local web server is already stopped!");
            return;
        }

        server.stop();
        server = null;
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void registerPath(String path, RequestHandler handler) {
        server.registerPath(path, handler);
    }

    @FunctionalInterface
    public interface RequestHandler {
        NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
    }

    @FunctionalInterface
    public interface StringRequestHandler extends RequestHandler {
        @Override
        default NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse(handle());
        }

        String handle();
    }

    @FunctionalInterface
    public interface BasicRequestHandler extends RequestHandler {
        @Override
        default NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("OK");
        }

        void handle();
    }

    public static NanoHTTPD.Response bytesResponse(byte[] bytes, String contentType) {
        InputStream stream = new ByteArrayInputStream(bytes);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, contentType, stream, bytes.length);
    }

    public static NanoHTTPD.Response stringResponse(String msg) {
        return NanoHTTPD.newFixedLengthResponse(msg);
    }

    private static class HTTPd extends NanoHTTPD {
        private final Map<String, RequestHandler> pathHandlers = new HashMap<>();

        private HTTPd(int port) throws BindException {
            super("localhost", port);

            Logger nanoLog = Logger.getLogger(NanoHTTPD.class.getName());
            for (Handler handler : nanoLog.getHandlers()) {
                nanoLog.removeHandler(handler);
            }

            nanoLog.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    Level level = record.getLevel();
                    String message = record.getMessage();

                    if (level.equals(Level.FINE)) {
                        Log.debug(message);
                    } else if (level.equals(Level.FINER) || level.equals(Level.FINEST)) {
                        Log.trace(message);
                    } else if (level.equals(Level.INFO) || level.equals(Level.CONFIG)) {
                        Log.info(message);
                    } else if (level.equals(Level.WARNING)) {
                        Log.warn(message);
                    } else if (level.equals(Level.SEVERE)) {
                        Log.error(message);
                    }
                }

                @Override
                public void flush() {
                    // do nothing
                }

                @Override
                public void close() throws SecurityException {
                    // do nothing
                }
            });

            try {
                start(SOCKET_READ_TIMEOUT, false);
            } catch (BindException ex) {
                throw ex;
            } catch (IOException e) { Log.catching(e); }

            Log.info("Started local web server on port {}", port);
        }

        @Override
        public void stop() {
            super.stop();
            Log.info("Stopped local web server");
        }

        public void registerPath(String path, RequestHandler handler) {
            pathHandlers.put(path, handler);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();

            if (method != Method.GET) {
                return methodNotAllowed();
            }

            URI requestUri;
            try {
                requestUri = new URI(session.getUri());
            } catch (URISyntaxException e) { return internalServiceError(); }
            String path = requestUri.getPath();

            Log.info("[HTTPd] {} {}", method, path);

            if (path.equals("/")) {
                return newFixedLengthResponse(Util.getResourceAsString("html/index.html"));
            } else if (path.equals("/favicon.ico")) {
                return WebServer.bytesResponse(Util.getResourceAsBytes("html/favicon.ico"), "image/x-icon");
            } else if (pathHandlers.containsKey(path)) {
                return pathHandlers.get(path).handle(session);
            }

            return notFound();
        }

        private Response methodNotAllowed() {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "405 Method Not Allowed");
        }

        private Response internalServiceError() {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "500 Internal Server Error");
        }

        private Response notFound() {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found");
        }
    }
}
