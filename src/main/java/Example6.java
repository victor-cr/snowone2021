import com.sun.net.httpserver.HttpServer;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.function.Function;

public class Example6 {
    public static void start(Function<String, String> reader) throws Exception {
        HttpServer server = HttpServer.create();

        server.bind(new InetSocketAddress(8080), 512);

        server.createContext("/", exchange -> {
            try (var out = new PrintWriter(new OutputStreamWriter(exchange.getResponseBody()))) {
                String path = exchange.getRequestURI().getPath().substring(1);

                try {
                    String data = "";

                    if (path.isEmpty()) {
                        data = "Hello from Java";
                    } else { //if (!path.contains("..") && !path.startsWith("/")) {
                        data = reader.apply(path);
                    }

                    exchange.sendResponseHeaders(200, data.length());
                    out.print(data);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(500, 0);
                    e.printStackTrace(out);
                }
            }
        });

        server.start();
    }


}
