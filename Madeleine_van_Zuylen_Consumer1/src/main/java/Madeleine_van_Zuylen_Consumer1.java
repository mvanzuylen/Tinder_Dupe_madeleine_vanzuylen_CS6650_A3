import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.dbcp2.BasicDataSource;

/**
public class Madeleine_van_Zuylen_Consumer1 {
  private static final int NUMTHREADS = 50;
  private static final String QUEUE_NAME = "TempStore";
  private static ConcurrentHashMap<Integer, Swipe> swipeMap = new ConcurrentHashMap();
  private static Consumer1Dao consumer1Dao = new Consumer1Dao();
  //private static BasicDataSource dataSource = DBCPDataSource.getDataSource();
*/
public class Madeleine_van_Zuylen_Consumer1 {
  private static final int NUMTHREADS = 50;
  private static final int BATCH_SIZE = 50;
  private static final String QUEUE_NAME = "TempStore";
  private static ConcurrentHashMap<Integer, Swipe> swipeMap = new ConcurrentHashMap();
  private static Consumer1Dao consumer1Dao = new Consumer1Dao();

  synchronized public static void updateDB(byte[] body){
    String message = new String(body, StandardCharsets.UTF_8);
    //System.out.println(" [x] Received '" + message + "'");

    String[] messageArray = message.replace("\"", "").split("[,:{}]");
    String direction = messageArray[1];
    String swiperId = messageArray[3];
    String swipeeId = messageArray[5];
    Swipe newSwipe = new Swipe(swiperId, swipeeId, direction);
    /**
    swipeMap.put(swipeMap.keySet().size(), newSwipe);

    if (swipeMap.keySet().size() >= BATCH_SIZE) {
      consumer1Dao.createNewSwipe(swipeMap);
      swipeMap.clear();
    }
     */
    consumer1Dao.createNewSwipe(newSwipe);
  }

  /**
  synchronized public static void updateDB(String message){
    String[] messageArray = message.replace("\"", "").split("[,:{}]");
    String direction = messageArray[1];
    String swiperId = messageArray[3];
    String swipeeId = messageArray[5];
    //String comment = messageArray[7];
    Swipe newSwipe = new Swipe(swiperId, swipeeId, direction);

    swipeMap.put(swipeMap.keySet().size(), newSwipe);
    //System.out.println(swipeMap.toString());
    if (swipeMap.keySet().size() == BATCH_SIZE) {
      //System.out.println(swipeMap.toString());
      consumer1Dao.createNewSwipe(swipeMap);
      swipeMap.clear();
    }
    //consumer1Dao.createNewSwipe(newSwipe);
  }
**/

  public static void main(String[] argv) throws Exception {
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("35.161.68.70");
    //factory.setHost("localhost");
    Connection connection = factory.newConnection();

    for (int i = 0; i < NUMTHREADS; i++) {

      Runnable thread = () -> {
        try {
          Channel channel = connection.createChannel();
          Map<String, Object> args = new HashMap();
          args.put("x-queue-type", "quorum");
          channel.queueDeclare(QUEUE_NAME, true, false, false, args);
          channel.basicQos(BATCH_SIZE);
          channel.queueBind(QUEUE_NAME, "exchange1", "");
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          channel.basicConsume(QUEUE_NAME, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                throws IOException {
                  updateDB(body);
                  channel.basicAck(envelope.getDeliveryTag(), true);
            }
          });
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

  /**
  public static void main(String[] argv) throws Exception {
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("35.89.215.145");
    //factory.setHost("localhost");
    Connection connection = factory.newConnection();

    for (int i = 0; i < NUMTHREADS; i++) {

      Runnable thread = () -> {
        try {
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.queueBind(QUEUE_NAME, "exchange1", "");
          //java.sql.Connection conn = dataSource.getConnection();

          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            updateDB(message);
            //System.out.println(" [x] Received '" + message + "'");
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
   */
}

