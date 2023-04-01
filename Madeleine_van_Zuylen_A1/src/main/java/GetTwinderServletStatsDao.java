import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.dbcp2.*;

public class GetTwinderServletStatsDao {
  private static BasicDataSource dataSource;

  public GetTwinderServletStatsDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public ArrayList<String> getMatches(String swiperId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ArrayList<String> result = new ArrayList<>();
    result.add("0");
    result.add("0");

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement("SELECT direction, COUNT(SwipeeId) FROM TwinderDB WHERE SwiperId=? GROUP BY direction");
      preparedStatement.setString(1, swiperId);

      // execute SQL statement
      ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()){
        if (rs.getString(1).equals("right")){
          result.set(0, rs.getString(2));
        }
        if (rs.getString(1).equals("left")){
          result.set(1, rs.getString(2));
        }
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