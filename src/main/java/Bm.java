import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Bm {
    private static final Value PROMISE = Context.getCurrent().getBindings("js").getMember("Promise");

    @SuppressWarnings("unused")
    public static List<String> readDirectoryTreeSync(String root) throws FileNotFoundException {
        File file = new File(root);

        if (!file.exists()) {
            throw new FileNotFoundException("File " + file.getPath() + " does not exist");
        }

        List<String> result = new ArrayList<>();
        Deque<File> stack = new ArrayDeque<>();

        stack.push(file);

        while (!stack.isEmpty()) {
            File f = stack.pop();
            File[] files = f.listFiles();

            result.add(f.getPath());

            if (files != null) {
                Arrays.stream(files).forEach(stack::push);
            }
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static Value readDirectoryTreeAsync(String root) {
        File file = new File(root);

        if (!file.exists()) {
            return PROMISE.invokeMember("reject", new FileNotFoundException("File " + file.getPath() + " does not exist"));
        }

        return PROMISE.newInstance((ProxyExecutable) arguments -> readDirectoryTree(new File(root)).whenComplete((files, error) -> {
            if (error != null) {
                Helper.deferred(() -> arguments[1].executeVoid(error));
            } else {
                Helper.deferred(() -> arguments[0].executeVoid(files.stream().map(File::getPath).collect(Collectors.toList())));
            }
        }));
    }

    @SuppressWarnings("unused")
    public static int readFileSync(String fileName, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];

        int result = 0;

        try (InputStream in = new FileInputStream(fileName)) {
            for (int size = 0; size >= 0; ) {
                size = in.read(buf, 0, buf.length);

                for (int i = 0; i < size; i++) {
                    if (buf[i] == 0) {
                        result++;
                    }
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static Value readFileAsync(String fileName) {
        return PROMISE.newInstance((ProxyExecutable) arguments -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                AtomicInteger zeroes = new AtomicInteger();

                AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(fileName), StandardOpenOption.READ);

                channel.read(buffer, 0L, 0L, new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, Long attachment) {
                        if (result > 0) {
                            long position = attachment + result;
                            byte[] content = buffer.array();
                            for (int i = 0, len = result; i < len; i++) {
                                if (content[i] == 0) {
                                    zeroes.incrementAndGet();
                                }
                            }
                            buffer.clear();
                            channel.read(buffer, position, position, this);
                        } else {
                            close(() -> arguments[0].executeVoid(zeroes.get()));
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Long attachment) {
                        close(() -> arguments[1].executeVoid(exc));
                    }

                    private void close(Runnable action) {
                        try {
                            channel.close();
                            Helper.deferred(action);
                        } catch (IOException error) {
                            Helper.deferred(() -> arguments[1].executeVoid(error));
                        }
                    }
                });
            } catch (IOException error) {
                arguments[1].executeVoid(error);
            }
            return null;
        });
    }

    @SuppressWarnings("unused")
    public static Value matrixSync(int matrixSize) {
        Random rnd = new Random();
        int length = matrixSize * matrixSize;

        double[] left = new double[length];
        double[] right = new double[length];
        double[] result = new double[length];

        for (int i = 0; i < matrixSize; i++) {
            left[i] = rnd.nextDouble() * length;
            right[i] = rnd.nextDouble() * length;
        }

        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {

                int offset = row * matrixSize;
                int x = row * matrixSize + col;

                for (int i = 0; i < matrixSize; i++) {
                    result[x] += left[offset + i] * right[i * matrixSize + col];
                }
            }
        }

        return Value.asValue(result);
    }

    private static CompletableFuture<List<File>> readDirectoryTree(File root) {
        return CompletableFuture.supplyAsync(root::listFiles).thenComposeAsync(files -> {
            if (root.isDirectory() && files != null) {
                List<CompletableFuture<List<File>>> children = Arrays.stream(files).map(Bm::readDirectoryTree).collect(Collectors.toList());

                return CompletableFuture.allOf(children.toArray(new CompletableFuture[0])).thenComposeAsync(x -> {
                    try {
                        List<File> result = new ArrayList<>();

                        result.add(root);

                        for (CompletableFuture<List<File>> child : children) {
                            result.addAll(child.get());
                        }

                        return CompletableFuture.completedFuture(result);
                    } catch (Exception e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
            } else {
                return CompletableFuture.completedFuture(Collections.singletonList(root));
            }
        });

//        File[] files = root.listFiles();
//
//        if (root.isDirectory() && files != null) {
//            return CompletableFuture.supplyAsync(() -> {
//                List<CompletableFuture<List<File>>> result = Arrays.stream(files).map(Bm::readDirectoryTree).collect(Collectors.toList());
//
//                return CompletableFuture.allOf(result.toArray(new CompletableFuture[0]))
//                        .thenCompose(x -> result.stream().map(f -> f.thenApply(Collection::stream)).reduce(
//                                CompletableFuture.completedFuture(Stream.of(root)),
//                                (l, r) -> l.thenCombine(r, Stream::concat)
//                        )).thenApply(s -> s.collect(Collectors.toList()));
//
//            });
//        } else {
//            return CompletableFuture.completedFuture(Collections.singletonList(root));
//        }
    }
}
