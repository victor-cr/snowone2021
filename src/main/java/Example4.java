import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;

public class Example4 {
    public static void main(String[] args) {
        System.out.println("==== GraalVM example #04 ====");
        try (Context context = Context.create()) {
            context.eval(
                    "js",
                    /* language=js */
                    "console.log(String(require('fs').readFileSync('src/main/java/Example4.java')))");
        }
        System.out.println("=============================");
    }

    public static void read(String fileName) throws IOException {
        try (var reader = new BufferedReader(new FileReader(fileName))) {
            reader.lines().forEach(System.out::println);
        }
    }

    public static void read(String fileName, Function<String, String> reader) {
        System.out.println(reader.apply(fileName));
    }
}
