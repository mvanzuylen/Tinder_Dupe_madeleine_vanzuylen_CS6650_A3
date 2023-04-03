import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.dbcp2.*;

public class Consumer1Dao {
  private static BasicDataSource dataSource;

  public Consumer1Dao() {
    dataSource = DBCPDataSource.getDataSource();
  }
    //public void createNewSwipe(ConcurrentHashMap<Integer, Swipe> map) {
    public void createNewSwipe(Swipe newSwipe) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "INSERT INTO TwinderDB (swiperId, swipeeId, direction) " +
        "VALUES (?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);

      preparedStatement.setString(1, newSwipe.getSwiperId()); //swiperid
      preparedStatement.setString(2, newSwipe.getSwipeeId()); //swipeeid
      preparedStatement.setString(3, newSwipe.getDirection()); //direction
      //preparedStatement.setString(4, newSwipe.getComment()); //comment
      /**
      for(int i = 0; i < map.keySet().size(); i++) {
        preparedStatement.setString(1, map.get(i).getSwiperId()); //swiperid
        preparedStatement.setString(2, map.get(i).getSwipeeId()); //swipeeid
        preparedStatement.setString(3, map.get(i).getDirection()); //direction
        //preparedStatement.setString(4, map.get(i).getComment()); //comment
        preparedStatement.addBatch();
      */
        // execute insert SQL statement
        preparedStatement.executeUpdate();
        //preparedStatement.executeBatch();
     // }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
         conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

/**
  public static void main(String[] args) {
    for (int i = 0; i < 100; i++) {
      Consumer1Dao consumer1Dao = new Consumer1Dao();
      consumer1Dao.createNewSwipe(new Swipe("10", "2", "right", "hello"));
    }
  }
**/
}