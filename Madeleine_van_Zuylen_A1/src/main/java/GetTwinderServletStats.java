import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "GetTwinderServletStats", value = "/GetTwinderServletStats")

public class GetTwinderServletStats extends HttpServlet {

  private static GetTwinderServletStatsDao getTwinderServletStatsDao = new GetTwinderServletStatsDao();

  private boolean isUrlValid(String[] urlPath) {

    if ((urlPath[1].equals("stats")) && (Integer.valueOf(urlPath[2]) > 0) && (Integer.valueOf(urlPath[2]) < 50001)){
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
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      String swiperId = urlParts[2];
      ArrayList<String> results = getTwinderServletStatsDao.getMatches(swiperId);

      // Convert results to json string
      Gson gson = new Gson();
      MatchStats matchStats = new MatchStats();
      matchStats.setNumLlikes(Integer.parseInt(results.get(0)));
      matchStats.setNumDislikes(Integer.parseInt(results.get(1)));
      String jsonInString = gson.toJson(matchStats);

      // Return MatchStats
      res.getWriter().write(jsonInString);
      res.setStatus(HttpServletResponse.SC_OK);
    }
    // 404 user not found
  }
}
