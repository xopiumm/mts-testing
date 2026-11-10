import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class LocalApi {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8087"));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 128);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.createContext("/api/products/", exchange -> {
            String id = exchange.getRequestURI().getPath().substring("/api/products/".length());
            int status = 200;
            String body;
            if (!exchange.getRequestMethod().equals("GET")) {
                status = 405; body = "{\"error\":\"method not allowed\"}";
            } else if (!id.matches("[1-9]|10")) {
                status = 404; body = "{\"error\":\"not found\"}";
            } else {
                try { Thread.sleep(40); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                body = "{\"id\":" + id + ",\"name\":\"Product " + id + "\",\"price\":" + (100 * Integer.parseInt(id)) + "}";
            }
            byte[] data = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, data.length);
            try (var out = exchange.getResponseBody()) { out.write(data); }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();
        System.out.println("Local API: http://127.0.0.1:" + port + "/api/products/1; workers=8, delay=40ms");
    }
}
