import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class Example5 {
    public static String read(String fileName) throws IOException {
        try (var reader = new BufferedReader(new FileReader(fileName))) {
            return reader.lines().collect(Collectors.joining("<br/>", "<pre>", "</pre>"));
        }
    }
}
