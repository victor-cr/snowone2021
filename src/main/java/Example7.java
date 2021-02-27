
import com.sun.net.httpserver.HttpServer;
import org.graalvm.polyglot.Value;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class Example7 {
    public static void start(Function<String, String> reader) throws Exception {
        HttpServer server = HttpServer.create();

        server.bind(new InetSocketAddress(8080), 512);

        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/plain");

            try (var out = new PrintWriter(new OutputStreamWriter(exchange.getResponseBody()))) {
                String path = exchange.getRequestURI().getPath().substring(1);

                try {
                    String data = "";

                    if (path.isEmpty()) {
                        data = "Hello from Java";
                    } else { //if (!path.contains("..") && !path.startsWith("/")) {
                        var text = new AtomicReference<String>();
                        deferred(() -> text.set(reader.apply(path)));
                        data = text.get();
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

    private static final AtomicInteger ID = new AtomicInteger();
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    private static final ConcurrentMap<String, Runnable> tasks = new ConcurrentHashMap<>();

    private static void deferred(Runnable task) throws InterruptedException {
        synchronized (task) {
            String id = String.valueOf(ID.incrementAndGet());
            tasks.put(id, task);
            queue.offer(id);
            task.wait();
        }
    }

    public static void set(Value parentPort) throws InterruptedException {
        while (true) parentPort.invokeMember("postMessage", queue.take());
    }

    public static void run(String id) {
        Runnable task = tasks.remove(id);

        synchronized (task) {
            task.run();
            task.notifyAll();
        }
    }
}
