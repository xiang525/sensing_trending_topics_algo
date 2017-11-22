package tmm.bngram;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import tmm.BasicConfiguration;
import tmm.ParameterUtilities;
import tmm.Topic;
import tmm.Tweet;
import tmm.bngram.topicDetector.Database;
import tmm.bngram.topicDetector.GlobalParameters;
import tmm.bngram.topicDetector.TrendingTopic;

public class TopicDetector
{
  List<List<Tweet>> prevTimeslotsTweets;

  public TopicDetector(String dataDirectory, String targetFilename)
  {
    Constants.configuration = new Configuration();

    GlobalParameters.database = dataDirectory;
    GlobalParameters.collection = "users";
    GlobalParameters.timeZone = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    GlobalParameters.targetFilename = targetFilename;

    String prevTimeslotsFStr = ParameterUtilities.readProperty(Constants.PREVIOUS_TIMESLOTS_FILES, Constants.configuration.getConfig());
    String[] prevFilenames = prevTimeslotsFStr.split(",");
    GlobalParameters.prevFilenames = new ArrayList(Arrays.asList(prevFilenames));

    GlobalParameters.dfIdf = GlobalParameters.prevFilenames.size();
    GlobalParameters.ngrams = Integer.parseInt(ParameterUtilities.readProperty(Constants.NGRAM_LENGTH, Constants.configuration.getConfig()));
    GlobalParameters.maximumDistanceClusters = Float.parseFloat(ParameterUtilities.readProperty(Constants.MAXIMUM_THRESHOLD_DISTANCE_CLUSTERS, Constants.configuration.getConfig()));
    GlobalParameters.minimumDistanceClusters = Float.parseFloat(ParameterUtilities.readProperty(Constants.MINIMUM_THRESHOLD_DISTANCE_CLUSTERS, Constants.configuration.getConfig()));
    GlobalParameters.top = Integer.parseInt(ParameterUtilities.readProperty(Constants.TOP, Constants.configuration.getConfig()));

    Properties prop = new Properties();
    prop.setProperty("tokenizerOptions", "untokenizable=noneDelete");
    try
    {
      ClassLoader loader = ClassLoader.getSystemClassLoader();

      InputStream in = loader.getResourceAsStream("english.all.3class.distsim.crf.ser");
      GlobalParameters.classifier = CRFClassifier.getClassifier(in);
    }
    catch (IOException ex)
    {
      Logger.getLogger(TopicDetector.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ClassCastException ex)
    {
      Logger.getLogger(TopicDetector.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(TopicDetector.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static Long getMinDate(List<Tweet> items)
  {
    Long min_upload = Long.valueOf(Long.MAX_VALUE);
    for (Tweet tmp_tweet : items) {
      if (tmp_tweet.getUploadTime().longValue() < min_upload.longValue()) {
        min_upload = tmp_tweet.getUploadTime();
      }
    }
    return min_upload;
  }

  private static Long getMaxDate(List<Tweet> items)
  {
    Long max_upload = Long.valueOf(Long.MIN_VALUE);
    for (Tweet tmp_tweet : items) {
      if (tmp_tweet.getUploadTime().longValue() > max_upload.longValue()) {
        max_upload = tmp_tweet.getUploadTime();
      }
    }
    return max_upload;
  }

  public List<Topic> createTopics(List<Tweet> tweets)
  {
    long min_ts = getMinDate(tweets).longValue();
    long max_ts = getMaxDate(tweets).longValue();

    GlobalParameters.topicDate = new Date(min_ts);

    GlobalParameters.period = (int)Math.ceil((max_ts - min_ts) / 60000.0D);

    List<Topic> topics = new ArrayList();

    Database.GetIndexJson();
    topics = TrendingTopic.GetTrending();

    return topics;
  }
}
