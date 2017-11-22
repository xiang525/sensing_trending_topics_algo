package tmm.bngram.topicDetector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.apache.lucene.index.Term;
import tmm.Ngram;

public class Utils
{
  private static Calendar calendar = (Calendar)GlobalParameters.timeZone.clone();
  
  public static Date previousTime(Date date, int time)
  {
    calendar.setTime(date);
    calendar.add(12, -1 * time);
    return calendar.getTime();
  }
  
  public static Date nextTime(Date date, int time)
  {
    calendar.setTime(date);
    calendar.add(12, time);
    return calendar.getTime();
  }
  
  public static String GetNgramsFile(Date time)
  {
    calendar.setTime(time);
    return "." + File.separator + GlobalParameters.folderResults + File.separator + "ngrams-" + calendar.get(5) + "_" + calendar.get(11) + "_" + calendar.get(12) + "_" + calendar.get(13) + ".csv";
  }
  
  public static String GetTermsFile(Date time)
  {
    calendar.setTime(previousTime(time, GlobalParameters.period));
    return "." + File.separator + GlobalParameters.folderResults + File.separator + "terms-" + calendar.get(5) + "_" + calendar.get(11) + "_" + calendar.get(12) + "_" + calendar.get(13) + ".csv";
  }
  
  public static String GetClustersFile(Date time)
  {
    calendar.setTime(time);
    return "." + File.separator + GlobalParameters.folderResults + File.separator + "clusters-" + calendar.get(5) + "_" + calendar.get(11) + "_" + calendar.get(12) + "_" + calendar.get(13) + ".txt";
  }
  
  public static String GetTopicsFile(Date time)
  {
    calendar.setTime(time);
    return "." + File.separator + GlobalParameters.folderResults + File.separator + calendar.get(5) + "_" + calendar.get(11) + "_" + calendar.get(12) + "_" + calendar.get(13) + ".json";
  }
  
  public static String GetTweetsFile(Date time)
  {
    calendar.setTime(time);
    return "." + File.separator + GlobalParameters.folderResults + File.separator + "tweets-" + calendar.get(5) + "_" + calendar.get(11) + "_" + calendar.get(12) + "_" + calendar.get(13) + ".json";
  }
  
  public static String GetJsonFile(Date time)
  {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    calendar.setTime(time);
    if (GlobalParameters.period == 60) {
      return File.separator + "home" + File.separator + "cmdanca" + File.separator + "Projects" + File.separator + "JsonCollections" + File.separator + GlobalParameters.database + File.separator + GlobalParameters.collection + File.separator + calendar.get(5) + "_" + calendar.get(2) + "_" + calendar.get(1) + "_" + calendar.get(11) + ".json";
    }
    int month = calendar.get(2) + 1;
    return File.separator + "home" + File.separator + "cmdanca" + File.separator + "Projects" + File.separator + "JsonCollections" + File.separator + GlobalParameters.database + File.separator + GlobalParameters.collection + File.separator + calendar.get(5) + "_" + month + "_" + calendar.get(1) + "_" + calendar.get(11) + "_" + calendar.get(12) + ".json";
  }
  
  public static String GetTopicsFile()
  {
    return GlobalParameters.folderResults + File.separator + "topics.json";
  }
  
  public static String GetTweetsFile()
  {
    return GlobalParameters.folderResults + File.separator + "tweets.json";
  }
  
  public static void SortByDfIdf(List<Ngram> ngrams)
  {
    if ((ngrams == null) || (ngrams.size() == 0)) {
      return;
    }
    Collections.sort(ngrams, new Comparator()
    {
      public int compare(Ngram n1, Ngram n2)
      {
        return n2.getScore().compareTo(n1.getScore());
      }
    });
  }
  
  public static void SortByDfIdfClusters(List<Topic> topics)
  {
    if ((topics == null) || (topics.size() == 0)) {
      return;
    }
    Collections.sort(topics, new Comparator()
    {
      public int compare(Topic t1, Topic t2)
      {
        return t2.getScore().compareTo(t1.getScore());
      }
    });
  }
  
  public static void SortByDfIdfNgrams(List<Ngram> ngrams)
  {
    if ((ngrams == null) || (ngrams.size() == 0)) {
      return;
    }
    Collections.sort(ngrams, new Comparator()
    {
      public int compare(Ngram n1, Ngram n2)
      {
        return n2.getScore().compareTo(n1.getScore());
      }
    });
  }
  
  public static void SortByNgramTweets(List<PartialTweet> tweetsList)
  {
    if ((tweetsList == null) || (tweetsList.size() == 0)) {
      return;
    }
    Collections.sort(tweetsList, new Comparator()
    {
      public int compare(PartialTweet t1, PartialTweet t2)
      {
        return Integer.valueOf(t2.getNgrams().size()).compareTo(Integer.valueOf(t1.getNgrams().size()));
      }
    });
  }
  
  public static Map<Term, List<Integer>> SortByNgramTweets(Map<Term, List<Integer>> unsortMap)
  {
    List<Map.Entry<Term, List<Integer>>> list = new LinkedList(unsortMap.entrySet());
    
    Collections.sort(list, new Comparator()
    {
      public int compare(Map.Entry<Term, List<Integer>> o1, Map.Entry<Term, List<Integer>> o2)
      {
        return new Integer(((List)o2.getValue()).size()).compareTo(Integer.valueOf(((List)o1.getValue()).size()));
      }
    });
    Map<Term, List<Integer>> sortedMap = new LinkedHashMap();
    for (Iterator<Map.Entry<Term, List<Integer>>> it = list.iterator(); it.hasNext();)
    {
      Map.Entry<Term, List<Integer>> entry = (Map.Entry)it.next();
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }
  
  public static String GetIndexFolder()
  {
    SimpleDateFormat formatter = new SimpleDateFormat();
    formatter.setTimeZone(calendar.getTimeZone());
    
    return GlobalParameters.database + "-" + GlobalParameters.collection + "-" + GlobalParameters.period + "-period-" + GlobalParameters.ngrams + "-grams" + File.separator + "index" + (GlobalParameters.stopTime != null ? "-" + formatter.format(GlobalParameters.stopTime) : "");
  }
  
  public static String GetInfluencersFileName(String influencers)
  {
    String influencersFile = influencers;
    if ((influencers != null) && (influencersFile.contains(File.separator))) {
      influencersFile = influencersFile.substring(influencersFile.lastIndexOf(File.separator) + 1);
    }
    if ((influencers != null) && (influencersFile.contains("."))) {
      influencersFile = influencersFile.substring(0, influencersFile.lastIndexOf("."));
    }
    return influencersFile;
  }
  
  public static String GetTimeFile()
  {
    return GlobalParameters.folderResults + File.separator + "time.txt";
  }
}
