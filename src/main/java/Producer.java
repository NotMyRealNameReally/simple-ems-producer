import com.tibco.tibjms.TibjmsConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Producer implements Runnable {
    private final Connection connection;
    private final Session session;
    private final MessageProducer messageProducer;
    private int counter;

    public Producer() throws JMSException {
        this.connection = new TibjmsConnectionFactory("tcp://localhost:7222")
                .createConnection("admin", "");
        this.session = connection.createSession();
        this.messageProducer = session.createProducer(session.createTopic("test_java"));
        this.counter = 0;
        connection.start();
    }

    @Override
    public void run() {
        try {
            String message = "test " + counter++;
            messageProducer.send(session.createTextMessage(message));
            System.out.println("message sent: " + message);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            session.close();
            connection.close();
            System.out.println("Connection closed");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Producer producer = new Producer();
            Runtime
                    .getRuntime()
                    .addShutdownHook(new Thread(producer::closeConnection));
            Executors
                    .newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(producer, 0, 5, TimeUnit.SECONDS);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
