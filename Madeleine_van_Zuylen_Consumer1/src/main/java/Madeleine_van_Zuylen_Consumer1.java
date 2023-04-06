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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.dbcp2.BasicDataSource;

public class Madeleine_van_Zuylen_Consumer1 {
  private static final int NUMTHREADS = 50;
  private static final int BATCH_SIZE = 25;
  private static final String QUEUE_NAME = "TempStore";
  private static ConcurrentHashMap<Integer, ArrayList<Integer>> twinderStats = new ConcurrentHashMap();
  private static ConcurrentHashMap<Integer, Integer> batchMap = new ConcurrentHashMap();
  private static ConcurrentHashMap<Integer, ArrayList<Integer>> twinderMatches = new ConcurrentHashMap();
  private static ConcurrentHashMap<Integer, Integer> batchMatches = new ConcurrentHashMap();
  private static Consumer1Dao consumer1Dao;

  static {
    try {
      consumer1Dao = new Consumer1Dao();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  synchronized public static void updateDB(byte[] body) throws SQLException {
    String message = new String(body, StandardCharsets.UTF_8);
    //System.out.println(" [x] Received '" + message + "'");
    ArrayList<Integer> stats = new ArrayList<>();
    stats.add(0, 0);
    stats.add(1, 0);

    String[] messageArray = message.replace("\"", "").split("[,:{}]");
    String direction = messageArray[1].trim();
    Integer swiperId = Integer.parseInt(messageArray[3]);
    String swipeeId = messageArray[5];

    if (twinderStats.containsKey(swiperId)){
      int likes;
      int dislikes;
      if (direction.equals("right")){
        likes = twinderStats.get(swiperId).get(0) + 1;
        dislikes = twinderStats.get(swiperId).get(1);
      } else {
        likes = twinderStats.get(swiperId).get(0);
        dislikes = twinderStats.get(swiperId).get(1) + 1;
      }
      stats.set(0,  likes);
      stats.set(1,  dislikes);
      twinderStats.put(swiperId, stats);
    } else {
        if (direction.equals("right")){
          stats.set(0, 1);
          stats.set(1, 0);
        } else {
          stats.set(0, 0);
          stats.set(1, 1);
          }
        twinderStats.put(swiperId, stats);
    }

    // Send batch to consumerDao when there are 25 updates to be made
    batchMap.put(batchMap.keySet().size(), swiperId);
     if (batchMap.keySet().size() >= BATCH_SIZE) {
       consumer1Dao.updateLikesDislikes(twinderStats, batchMap);
       batchMap.clear();
     }
  }

  synchronized public static void updateMatchesDB(byte[] body) throws SQLException {
    String message = new String(body, StandardCharsets.UTF_8);
    //System.out.println(" [x] Received Matches'" + message + "'");
    ArrayList<Integer> matches = new ArrayList<>();

    String[] messageArray = message.replace("\"", "").split("[,:{}]");
    String direction = messageArray[1].trim();
    Integer swiperId = Integer.parseInt(messageArray[3]);
    Integer swipeeId = Integer.parseInt(messageArray[5]);

    // Only add right swipes to matches
    if (direction.equals("right")) {
      if (twinderMatches.containsKey(swiperId)) {
        matches = twinderMatches.get(swiperId);
        matches.add(swipeeId);
      } else if (!twinderMatches.containsKey(swiperId)) {
        matches.add(swipeeId);
      }
      twinderMatches.put(swiperId, matches);
      batchMatches.put(batchMatches.keySet().size(), swiperId);

      // Send batch to consumerDao when there are 25 updates to be made
      if (batchMatches.keySet().size() >= BATCH_SIZE) {
        consumer1Dao.updateMatches(twinderMatches, batchMatches);
        batchMatches.clear();
      }
    }
  }

  public static void main(String[] argv) throws Exception {
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.219.161.238");
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
              try {
                updateDB(body);
                updateMatchesDB(body);
              } catch (SQLException throwables) {
                throwables.printStackTrace();
              }
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
}

