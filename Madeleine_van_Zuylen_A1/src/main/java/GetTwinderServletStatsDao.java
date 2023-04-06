import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.dbcp2.*;

public class GetTwinderServletStatsDao {
  private static BasicDataSource dataSource;

  public GetTwinderServletStatsDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public ArrayList<Integer> getStats(String swiperId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ArrayList<Integer> result = new ArrayList<>();
    result.add(0);
    result.add(0);

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement("SELECT Likes, DisLikes FROM TwinderLikesDislikes WHERE SwiperId=?");
      preparedStatement.setString(1, swiperId);

      // execute SQL statement
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        result.set(0, rs.getInt(1));
        result.set(1, rs.getInt(2));
      }
      return result;

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
    return result;
  }

}