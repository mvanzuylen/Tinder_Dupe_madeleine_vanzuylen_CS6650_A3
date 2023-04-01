import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.dbcp2.*;

public class GetTwinderServletMatchesDao {
  private static BasicDataSource dataSource;

  public GetTwinderServletMatchesDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public ArrayList<String> getMatches(String swiperId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ArrayList<String> result = new ArrayList<String>();

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement("SELECT * FROM TwinderDB WHERE SwiperId=? and direction = 'right' LIMIT 100");
      preparedStatement.setString(1, swiperId);

      // execute SQL statement
      ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()){
        result.add(rs.getString(2));
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