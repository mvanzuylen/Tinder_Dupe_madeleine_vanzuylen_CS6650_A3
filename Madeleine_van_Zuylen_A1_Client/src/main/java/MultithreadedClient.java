import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import io.swagger.client.model.SwipeDetails;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MultithreadedClient {

  final static private int NUMTHREADS = 5; //100
  private static int NUMPOSTS = 50000; //500000
  //private static String url = "http://35.163.67.34:8080/Madeleine_van_Zuylen_A1_war/twinder"; // One servlet
  //private static String url = "http://mvz-alb-1104176885.us-west-2.elb.amazonaws.com:8080/Madeleine_van_Zuylen_A1_war/twinder"; // Load Balancer
  private static String url = "http://localhost:8080/Madeleine_van_Zuylen_A1_war_exploded/twinder/"; // Localhost
  private static String urlMatches = "http://localhost:8080/Madeleine_van_Zuylen_A1_war_exploded/get_matches/"; // Localhost
  private static String urlStats = "http://localhost:8080/Madeleine_van_Zuylen_A1_war_exploded/get_stats/"; // Localhost
  private static AtomicInteger numSuccessfulRequests = new AtomicInteger(0);
  private static AtomicInteger numUnSuccessfulRequests = new AtomicInteger(0);
  private static String postCsv = "posts.csv";
  private static List<String[]> postData = new ArrayList<>();
  private static List<Long> latecyData = new ArrayList<>();
  private static List<Integer> endTimeData = new ArrayList<>();
  private static List<Long> getLatencyData = new ArrayList<>();
  private static AtomicInteger countSuccessfulMatches = new AtomicInteger(0);
  private static AtomicInteger countSuccessfulStats = new AtomicInteger(0);


  public static String convertToCSV(String[] data) {
    return Stream.of(data)
        .collect(Collectors.joining(","));
  }

  synchronized public static void addPostData(long threadStartTime, String post, long latency, int responseCode) {
    File csvOutputFile = new File(postCsv);
    postData.add(new String[]
        {Long.toString(threadStartTime), post, Long.toString(latency), Integer.toString(responseCode) });
    latecyData.add(latency);
    endTimeData.add((int) Math.floor((threadStartTime + latency)/1000));
  }

  public static void writeToCsv(String csvFile) throws IOException {
    File csvOutputFile = new File(postCsv);
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      postData.stream()
          .map(MultithreadedClient::convertToCSV)
          .forEach(pw::println);
    }
  }

 // Calculates the 99th percentile of latencies:
  public static long percentile99(List<Long> latencies) {
    int index = (int) Math.ceil(99 / 100.0 * latencies.size());
    return latencies.get(index-1);
  }

  // Calculate metrics from Task 3:
  public static void calculateMetrics(String csvFile, long totalTime){
    Collections.sort(latecyData);
    int sum = latecyData.stream().mapToInt(Long::intValue).sum();
    System.out.println("");
    System.out.println("Task 3: ");
    System.out.println("Mean Response Time (ms): " + (double) sum/latecyData.size());
    System.out.println("Median Response Time (ms): " + Long.toString(latecyData.get(latecyData.size()/2)));
    System.out.println("Throughput (requests/sec): " + (double) latecyData.size()/totalTime * 1000);
    System.out.println("p99 Response Time (ms): " + percentile99(latecyData));
    System.out.println("Max Response Time (ms): " + Long.toString(latecyData.get(latecyData.size() - 1)));
    System.out.println("Min Response Time (ms): " + Long.toString(latecyData.get(0)));
  }

  // Creates arraylist with number of seconds (index of array) and counts of the number of finished threads (value)
  public static List<Integer> generatePlotArray(List<Integer> endTimeData){
    Collections.sort(endTimeData);
    int numSeconds = endTimeData.get(endTimeData.size()-1) - endTimeData.get(0);
    List<Integer> plotArray = new ArrayList<Integer>(Collections.nCopies( numSeconds + 1, 0));

    for (int i = 0; i < endTimeData.size(); i++){
      int value =  endTimeData.get(i);
      value = value - endTimeData.get(0);
      int oldValue = plotArray.get(value);
      plotArray.set(value, oldValue + 1);
    }
    return plotArray;
  }

  public static String getRandomNumber(int min, int max) {
    return  Integer.toString((int) (Math.random() * (max + 1 - min)) + min);
  }

  private static int getAverage(List<Long> array){
    int total = 0;
    int average;
    for(int i = 0; i < getLatencyData.size(); i++) {
      total += array.get(i);
    }
    average = total / array.size();
    return average;
  }

  public static void main(String[] args) throws InterruptedException, IOException {


    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    CountDownLatch completed2 = new CountDownLatch(1);


    long startTime = System.currentTimeMillis();

      for (int i = 0; i < NUMTHREADS; i++) {

        Runnable thread = () -> {
          SwipeApi swipeApi = new SwipeApi();
          ApiClient apiClient = swipeApi.getApiClient();
          apiClient.setBasePath(url);
          swipeApi.setApiClient(apiClient);

        try {
          // For loop for number of posts/threads
          for (int j = 0; j < NUMPOSTS/NUMTHREADS; j++) {
            // Set swipe details
            String[] values = DataGeneration.getValues();
            SwipeDetails swipeDetails = new SwipeDetails();
            swipeDetails.setSwiper(values[1]);
            swipeDetails.setSwipee(values[2]);
            swipeDetails.setComment(values[3]);
            ApiResponse<Void> response;

            long threadStartTime = System.currentTimeMillis();
            int responseCode = 0;
            // Try 5x (if there is a failure)
            for (int z = 0; z < 5; z++) {
              response = swipeApi.swipeWithHttpInfo(swipeDetails, values[0]);
              // if response code is 200 and break this for loop and if not, keep trying
              responseCode = response.getStatusCode();
              if (responseCode == 200) {
                numSuccessfulRequests.incrementAndGet();
                break;
              } else {
                numUnSuccessfulRequests.incrementAndGet();
              }
            }
            long threadEndTime = System.currentTimeMillis();
            long latency = threadEndTime - threadStartTime;
            addPostData(threadStartTime, "POST", latency, responseCode);
          }
        } catch (ApiException e) {
          e.printStackTrace();
        } finally {
          completed.countDown();
        }
      };
      new Thread(thread).start();
  }
    // POST threads
    //completed.await();

    // GetThread
    Runnable thread2 = () -> {
        boolean on = true;

        // Matches API
        MatchesApi matchApi = new MatchesApi();
        ApiClient apiClientMatches;
        apiClientMatches = new ApiClient();
        matchApi = new MatchesApi(apiClientMatches);
        apiClientMatches.setBasePath(urlMatches);

        // Stats API
        StatsApi statsApi = new StatsApi();
        ApiClient apiClientStats;
        apiClientStats = new ApiClient();
        statsApi = new StatsApi(apiClientStats);
        apiClientStats.setBasePath(urlStats);

      try {
        ApiResponse<Matches> responseMatch;
        ApiResponse<MatchStats> responseStat;
        while (completed.getCount() > 0){
          for (int get = 0; get < 5; get++) {
            String swiperId = getRandomNumber(1, 50000);
            long threadStartTime2 = System.currentTimeMillis();
            if (on) {
              responseMatch = matchApi.matchesWithHttpInfo(swiperId);
              if (responseMatch.getStatusCode() == 200) {
                countSuccessfulMatches.incrementAndGet();
              }
              //System.out.println(responseMatch.getData().toString());
              //System.out.println(countSuccessfulMatches.toString());
              on = false;
            } else {
              responseStat = statsApi.matchStatsWithHttpInfo(swiperId);
              if (responseStat.getStatusCode() == 200) {
                countSuccessfulStats.incrementAndGet();
              }
              //System.out.println(responseStat.getData().toString());
              //System.out.println(countSuccessfulStats.toString());
              on = true;
            }
            long threadEndTime2 = System.currentTimeMillis();
            getLatencyData.add(threadEndTime2 - threadStartTime2);
          }
          TimeUnit.SECONDS.sleep(1);
        }
        } catch (ApiException | InterruptedException e) {
          e.printStackTrace();
        } finally {
          completed2.countDown();
      }
    };
      new Thread(thread2).start();

    // POST threads
    completed.await();

    // GET threads
    completed2.await();
    //System.out.println("latency size: " + getLatencyData.size());
    //System.out.println("latency array: " + getLatencyData.toString());
    System.out.println("successful matches: " + countSuccessfulMatches.get());
    System.out.println("successful stats: " + countSuccessfulStats.get());

    // Get Information
    System.out.println("GET Stats: ");
    System.out.println("Min GET latency (ms): " + Collections.min(getLatencyData));
    System.out.println("Max GET latency (ms): " + Collections.max(getLatencyData));
    System.out.println("Mean GET latency (ms): " + getAverage(getLatencyData));
    System.out.println("Latency array size: " + getLatencyData.size());
    System.out.println("--------------------------------------------------------------------");

    // POST Information
    System.out.println("POST Stats: ");
    long endTime = System.currentTimeMillis();
    int requests = numSuccessfulRequests.get() + numUnSuccessfulRequests.get();
    long numRequests = requests;
    long totalTime = endTime - startTime;
    System.out.println("Task 2: ");
    System.out.println("Number of Requests:" + " " + Integer.toString(requests));
    System.out.println("Number of Successful Requests:" + " " + Integer.toString(numSuccessfulRequests.get()));
    System.out.println("Number of Unsuccessful Requests:" + " " + Integer.toString(numUnSuccessfulRequests.get()));
    System.out.println("Total Time:" + " " + String.valueOf(totalTime) + " " + "Milliseconds");
    System.out.println("Throughput:" + " " + ((double) numRequests/(endTime - startTime))*1000 + " " + "Number of requests/Second");
    writeToCsv(postCsv);
    calculateMetrics(postCsv, totalTime);
    System.out.println("Array to generate plot: " + generatePlotArray(endTimeData));
  }
}
