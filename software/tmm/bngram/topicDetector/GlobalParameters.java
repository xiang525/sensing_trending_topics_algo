package tmm.bngram.topicDetector;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GlobalParameters
{
  public static String targetFilename;
  public static List<String> prevFilenames;
  public static String server;
  public static int port;
  public static String database;
  public static String collection;
  public static Calendar timeZone;
  public static Date startTime;
  public static Date stopTime;
  public static int period = 1;
  public static int dfIdf = 1;
  public static int ngrams = 1;
  public static float maximumDistanceTerms = 0.0F;
  public static float minimumDistanceClusters = 0.0F;
  public static float maximumDistanceClusters = 0.0F;
  public static int top = 0;
  public static float matcher = 0.0F;
  public static List<Date> topicDates = new ArrayList();
  public static Date topicDate = new Date();
  public static String folderResults;
  public static AbstractSequenceClassifier classifier;
}
