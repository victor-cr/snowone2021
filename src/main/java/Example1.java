import org.graalvm.polyglot.Context;

public class Example1 {
    public static void main(String[] args) {
        System.out.println("==== GraalVM example #01 ====");
        System.out.println("Java says: Hello");
        try (Context context = Context.create()) {
            context.eval(
                    "js",
                    /* language=js */
                    "const greetings = 'Hello too';" +
                            "console.log('Javascript says: ' + greetings);");
        }
        System.out.println("=============================");
    }
}
