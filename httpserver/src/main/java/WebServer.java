import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import sun.misc.IOUtils;
import sun.net.httpserver.HttpServerImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class WebServer {
    private static final String TASK_ENDPOINT = "/task";
    private static final String STATUS_ENDPOINT = "/status";

    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int serverport = 8080;
        serverport = args.length == 1 ? Integer.parseInt(args[0]) : serverport;

        WebServer webServer = new WebServer(serverport);
        webServer.startServer();

        System.out.println("Server is listening on port: " + serverport);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(TASK_ENDPOINT);

        statusContext.setHandler(this::handleStatusRequest);
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            sendResponse("123\n".getBytes(), exchange);
            return;
        }

        boolean isDebug = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebug = true;
        }

        long startTime = System.nanoTime();
        byte[] requestBytes = IOUtils.readFully(exchange.getRequestBody(), 0, true) ;
        byte[] responseBytes = calculateResponse(requestBytes);

        long finishTime = System.nanoTime();

        if (isDebug) {
            String debugMessage = String.format("Operation %d ns\n", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        sendResponse(responseBytes, exchange);
    }

    private byte[] calculateResponse(byte[] requestBytes) {
        String body = new String(requestBytes);
        String[] stringNumbers = body.split(",");

        BigInteger result = BigInteger.ONE;
        for (String number : stringNumbers) {
            BigInteger integer = BigInteger.valueOf(Long.parseLong(number));
            result = result.multiply(integer);
        }
        return String.format("Result of the multiplication is: %s", result).getBytes();
    }

    private void handleStatusRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "Server is alive";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] message, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, message.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(message);
        outputStream.flush();
        outputStream.close();
    }
}
