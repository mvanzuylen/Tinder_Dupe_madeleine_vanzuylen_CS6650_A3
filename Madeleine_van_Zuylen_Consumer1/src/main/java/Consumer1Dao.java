import java.sql.*;
import org.apache.commons.dbcp2.*;

public class Consumer1Dao {
  private static BasicDataSource dataSource;

  public Consumer1Dao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public void createNewSwipe(Swipe newSwipe) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "INSERT INTO TwinderDB (swiperId, swipeeId, direction, swipeComment) " +
        "VALUES (?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, newSwipe.getSwiperId());
      preparedStatement.setString(2, newSwipe.getSwipeeId());
      preparedStatement.setString(3, newSwipe.getDirection());
      preparedStatement.setString(4, newSwipe.getComment());

      // execute insert SQL statement
      preparedStatement.executeUpdate();
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