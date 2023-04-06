import java.sql.*;
import java.util.ArrayList;
import org.apache.commons.dbcp2.*;

public class GetTwinderServletMatchesDao {
  private static BasicDataSource dataSource;

  public GetTwinderServletMatchesDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public ArrayList<Integer> getMatches(String swiperId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ArrayList<Integer> intArray = new ArrayList<>();;

    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement("SELECT * FROM TwinderMatches WHERE SwiperId=?");
      preparedStatement.setString(1, swiperId);

      // execute SQL statement
      ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()){
        System.out.println(rs.getString(2));
        String arrayMatches = rs.getString(2);
        // Convert string to array of ints
        String[] stringArray = arrayMatches.substring(1,arrayMatches.length() - 1).split(", ");
        for (int i = 0; i < stringArray.length; i++) {
          intArray.add(Integer.parseInt(stringArray[i]));
        }
      }
      return intArray;

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
    return intArray;
  }

}