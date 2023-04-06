import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "GetTwinderServletMatches", value = "/GetTwinderServletMatches")

public class GetTwinderServletMatches extends HttpServlet {

  private static GetTwinderServletMatchesDao getTwinderServletMatchesDao = new GetTwinderServletMatchesDao();

  private boolean isUrlValid(String[] urlPath) {

    if ((urlPath[1].equals("matches")) && (Integer.valueOf(urlPath[2]) > 0) && (Integer.valueOf(urlPath[2]) < 50001)){
     return true;
    }
    return false;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      // 400 Bad inputs (URL not valid)
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      String swiperId = urlParts[2];
      // Returns up to 100 matches in array format
      ArrayList<Integer> results = getTwinderServletMatchesDao.getMatches(swiperId);
      Gson gson = new Gson();
      Matches matches = new Matches();
      matches.setMatchList(results);
      String jsonInString = gson.toJson(matches);

      // Return matches
      //res.getWriter().write("Get matches works!");
      //res.getWriter().write(results.toString());
      res.getWriter().write(jsonInString);
      res.setStatus(HttpServletResponse.SC_OK);
    }
    // 404 user not found?
  }
}
