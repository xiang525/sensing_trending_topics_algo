package tmm.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import tmm.Tweet;

public class DataLoader
{
  public static List<Tweet> loadData(String filename)
  {
    List<Tweet> tweets = new ArrayList();
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(filename));
      
      String id = "";
      String text = "";
      String uploader = "";
      Long uplodadTime = Long.valueOf(0L);
      String line;
      while ((line = br.readLine()) != null)
      {
        Tweet new_tweet = new Tweet(line);
        tweets.add(new_tweet);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return tweets;
  }
}
