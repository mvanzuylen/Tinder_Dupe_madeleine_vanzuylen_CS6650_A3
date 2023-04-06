import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleToIntFunction;
import org.apache.commons.dbcp2.*;

public class Consumer1Dao {
  private static BasicDataSource dataSource;

  public Consumer1Dao() throws SQLException {
    dataSource = DBCPDataSource.getDataSource();
    //dataSource.setMaxTotal(50);
  }

  public void updateLikesDislikes(ConcurrentHashMap<Integer, ArrayList<Integer>> twinderStats, ConcurrentHashMap<Integer, Integer> batch) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;

    // Need to insert swiper Id into the table for the first time
      String insertQueryStatement = "INSERT INTO TwinderLikesDislikes (swiperId, Likes, Dislikes) " +
          "VALUES (?,?,?)" + "ON DUPLICATE KEY UPDATE Likes=VALUES(Likes), Dislikes=VALUES(Dislikes)";

        try {
          conn = dataSource.getConnection();
          preparedStatement = conn.prepareStatement(insertQueryStatement);

          for (Integer id: batch.keySet()) {
            preparedStatement.setInt(1, batch.get(id)); // swiperid
            preparedStatement.setInt(2, twinderStats.get(batch.get(id)).get(0)); // likes
            preparedStatement.setInt(3, twinderStats.get(batch.get(id)).get(1)); //dislikes
            preparedStatement.addBatch();
          }

          // execute insert SQL statement
          //preparedStatement.executeUpdate();
          preparedStatement.executeBatch();
        } catch (SQLException throwables) {
          throwables.printStackTrace();
        } finally {
          try {
            if (preparedStatement != null) {
              preparedStatement.close();
            }
            if (conn != null) {
              conn.close();
            }
          } catch (SQLException se) {
            se.printStackTrace();
          }
        }
  }

  public void updateMatches(ConcurrentHashMap<Integer, ArrayList<Integer>> twinderMatches, ConcurrentHashMap<Integer, Integer> batchMatches) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;

    // Need to insert swiper Id into the table for the first time
    String insertQueryStatement = "INSERT INTO TwinderMatches (swiperId, matches) " +
        "VALUES (?,?)" + "ON DUPLICATE KEY UPDATE Matches=VALUES(matches)";

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);

      for (Integer id: batchMatches.keySet()) {
        preparedStatement.setInt(1, batchMatches.get(id)); // swiperid
        preparedStatement.setString(2, twinderMatches.get(batchMatches.get(id)).toString()); // matches
        preparedStatement.addBatch();
      }

      // execute insert SQL statement
      //preparedStatement.executeUpdate();
      preparedStatement.executeBatch();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    } finally {
      try {
        if (preparedStatement != null) {
          preparedStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }
/**
  public static void main(String[] args) throws SQLException {
    ArrayList<Integer> test = new ArrayList<>();
    test.add(1);
    test.add(0);
    ArrayList<Integer> test2 = new ArrayList<>();
    test2.add(1);
    test2.add(1);
    for (int i = 0; i < 1; i++) {
      Consumer1Dao consumer1Dao = new Consumer1Dao();
      try {
        consumer1Dao.updateLikesDislikes(10, test);
        consumer1Dao.updateLikesDislikes(10, test2);
        consumer1Dao.updateLikesDislikes(14, test);

      } catch (Exception e) {
        e.printStackTrace();
      }
      //consumer1Dao.createNewSwipe(new Swipe("10", "2", "right", "hello"));
    }
  }
 **/

/**
 public static void main(String[] args) throws SQLException {
  ArrayList<Integer> test = new ArrayList<>();
  test.add(100);
  test.add(3445);
  System.out.println(test.toString());
  ArrayList<Integer> test2 = new ArrayList<>();
  test2.add(100);
  test2.add(3445);
  test2.add(7);
  test2.add(34);
  ConcurrentHashMap<Integer, ArrayList<Integer>> map = new ConcurrentHashMap<>();
  map.put(10, test2);
   map.put(11, test2);
   map.put(12, test2);
   System.out.println(map.toString());
  ConcurrentHashMap<Integer, Integer> map2 = new ConcurrentHashMap<>();
  map2.put(0, 10);
   map2.put(1, 11);
   map2.put(2, 12);


   Consumer1Dao consumer1Dao = new Consumer1Dao();
  try {
    consumer1Dao.updateMatches(map, map2);
  } catch (Exception e) {
    e.printStackTrace();
  }
}
**/
}