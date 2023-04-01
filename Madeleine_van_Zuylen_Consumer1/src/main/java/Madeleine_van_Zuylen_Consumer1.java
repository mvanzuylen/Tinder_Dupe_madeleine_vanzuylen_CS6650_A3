import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class Madeleine_van_Zuylen_Consumer1 {
  private static final int NUMTHREADS = 50;
  private static final String QUEUE_NAME = "TempStore";
  private static final Consumer1Dao consumer1Dao = new Consumer1Dao();

  public static void updateDB(String message){
    String[] messageArray = message.replace("\"", "").split("[,:{}]");
    String direction = messageArray[1];
    String swiperId = messageArray[3];
    String swipeeId = messageArray[5];
    String comment = messageArray[7];
    Swipe newSwipe = new Swipe(swiperId, swipeeId, direction, comment);
    consumer1Dao.createNewSwipe(newSwipe);
  }

  public static void main(String[] argv) throws Exception {
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setHost("35.89.78.12");
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    for (int i = 0; i < NUMTHREADS; i++) {
      Runnable thread = () -> {
        try {
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          //added
          channel.queueBind(QUEUE_NAME, "exchange1", "");

          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            updateDB(message);
           // System.out.println(" [x] Received '" + message + "'");
          };
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        } catch (IOException e){
          e.printStackTrace();
        } finally {
          completed.countDown();
        }
      };
      new Thread(thread).start();
    }
    completed.await();
  }
}

