import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Example3 {
    public static void main(String[] args) {
        System.out.println("==== GraalVM example #03 ====");
//        greet(Value.asValue("Hello"));
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            var greetings = new G(null);
            context.getBindings("js").putMember("greetings", greetings);
            var value = context.eval(
                    "js",
                    /* language=js */
                    "greetings.text('aaa');").asString();
            System.out.println(value);
        }
        System.out.println("=============================");
    }

    public static void greet(Value greetings) {
        System.out.println("Java says: " + greetings.getMember("abc"));
    }

    public static class G {
        private String text;

        public G(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }

        public String text(String text) {
            return this.text == null ? text : this.text;
        }
    }
}
