import com.sun.net.httpserver.HttpServer;
import net.manub.embeddedkafka.EmbeddedKafka;
import net.manub.embeddedkafka.EmbeddedKafkaConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.graalvm.polyglot.Value;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Example8 {
    private static final String KAFKA_URL = "localhost:6001";
    private static final Producer<String, String> producer;

    static {
        EmbeddedKafka.start(EmbeddedKafkaConfig.defaultConfig());

        Properties props = new Properties(); // create instance for properties to access producer configs

        props.put("bootstrap.servers", KAFKA_URL); //Assign localhost id
        props.put("acks", "all"); //Set acknowledgements for producer requests.
        props.put("retries", 0); //If the request fails, the producer can automatically retry,
        props.put("batch.size", 16384); //Specify buffer size in config
        props.put("linger.ms", 1); //Reduce the no of requests less than 0
        props.put("buffer.memory", 33554432); //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    public static void produce(String topic, Value value) {
        producer.send(new ProducerRecord<>(topic, value.asString()));
    }

    public static void subscribe(String group, String topic, Value callback) {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", KAFKA_URL);
            props.put("group.id", group);
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

            consumer.subscribe(Collections.singletonList(topic));

            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        deferred(() -> callback.execute(record));
                    }
                }
            } catch (InterruptedException e) {
            }
        }, group + "-" + topic).start();
    }

    public static void createTopic(String topic) throws Exception {
        Properties props = new Properties();

        props.put("bootstrap.servers", KAFKA_URL);
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try (final AdminClient adminClient = KafkaAdminClient.create(props)) {
            try {
                // Define topic
                final NewTopic newTopic = new NewTopic(topic, 1, (short) 1);

                // Create topic, which is async call.
                final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));

                // Since the call is Async, Lets wait for it to complete.
                createTopicsResult.values().get(topic).get();
            } catch (InterruptedException | ExecutionException e) {
                if (!(e.getCause() instanceof TopicExistsException)) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                // TopicExistsException - Swallow this exception, just means the topic already exists.
            }
        }
    }

    private static final AtomicInteger ID = new AtomicInteger();
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    private static final ConcurrentMap<String, Runnable> tasks = new ConcurrentHashMap<>();

    private static void deferred(Runnable task) throws InterruptedException {
        String id = String.valueOf(ID.incrementAndGet());
        tasks.put(id, task);
        queue.offer(id);
    }

    public static void set(Value parentPort) throws InterruptedException {
        while (true) parentPort.invokeMember("postMessage", queue.take());
    }

    public static void run(String id) {
        tasks.remove(id).run();
    }
}
