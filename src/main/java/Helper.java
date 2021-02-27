import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Helper {
    private static final AtomicInteger ID = new AtomicInteger();
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1024);
    private static final ConcurrentMap<String, Runnable> tasks = new ConcurrentHashMap<>();

    public static void deferred(Runnable task) {
        String id = String.valueOf(ID.incrementAndGet());
        tasks.put(id, task);
        queue.offer(id);
    }

    @SuppressWarnings("unused")
    public static void init(Value requireRef) {
        requireRef.execute("worker_threads").getMember("Worker").newInstance(
                "Java.type('" + Helper.class.getName() + "').set(require('worker_threads').parentPort)",
                new ProxyObject() {
                    @Override
                    public Object getMember(String key) {
                        return hasMember(key) ? true : null;
                    }

                    @Override
                    public Object getMemberKeys() {
                        return new String[]{"eval"};
                    }

                    @Override
                    public boolean hasMember(String key) {
                        return "eval".equals(key);
                    }

                    @Override
                    public void putMember(String key, Value value) {
                    }
                }
        ).invokeMember("on", "message", (ProxyExecutable) args -> {
            tasks.remove(args[0].asString()).run();
            return null;
        });
    }

    @SuppressWarnings("unused")
    public static void set(Value parentPort) throws InterruptedException {
        while (true) parentPort.invokeMember("postMessage", queue.take());
    }
}
