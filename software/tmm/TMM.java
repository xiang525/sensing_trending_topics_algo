package tmm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.Term;
import tmm.data.DataLoader;

public class TMM
{
  public static void main(String[] args)
  {
    BasicConfiguration mainConfig = new MainConfiguration();
    mainConfig.getConfig();
    MainConstants.TOPIC_DETECTIONS_METHODS usedTopicDetectionMethod = MainConstants.TOPIC_DETECTIONS_METHODS.valueOf(ParameterUtilities.readProperty(MainConstants.TOPIC_DETECTION_METHOD, mainConfig.getConfig()));
    
    System.out.println("Chosen topic detection methods is : " + usedTopicDetectionMethod);
    
    String dataDirectory = ParameterUtilities.readProperty(MainConstants.TWEETS_DIRECTORY, mainConfig.getConfig());
    if (!dataDirectory.endsWith(File.separator)) {
      dataDirectory = dataDirectory + File.separator;
    }
    String tweetsFile = ParameterUtilities.readProperty(MainConstants.TWEETS_FILE, mainConfig.getConfig());
    List<Tweet> tweets = DataLoader.loadData(dataDirectory + tweetsFile);
    
    System.out.println("Data directory is : " + dataDirectory);
    System.out.println("Tweets file is : " + tweetsFile);
    System.out.println("No of tweets : " + tweets.size());
    
    List<Topic> topics = new ArrayList();
    switch (usedTopicDetectionMethod)
    {
    case LDA: 
      tmm.lda.TopicDetector topicDetectorLDA = new tmm.lda.TopicDetector();
      topics = topicDetectorLDA.createTopics(tweets);
      break;
    case BNGRAM: 
      tmm.bngram.TopicDetector topicDetectorBNgram = new tmm.bngram.TopicDetector(dataDirectory, tweetsFile);
      topics = topicDetectorBNgram.createTopics(tweets);
      break;
    case DOC_PIVOT: 
      tmm.documentpivot.TopicDetector topicDetectorDocPivot = new tmm.documentpivot.TopicDetector();
      topics = topicDetectorDocPivot.createTopics(tweets);
      break;
    case GRAPH_BASED: 
      tmm.graphbased.TopicDetector topicDetectorGraph = new tmm.graphbased.TopicDetector();
      topics = topicDetectorGraph.createTopics(tweets);
      break;
    case SOFT_FIM: 
      tmm.sfim.TopicDetector topicDetectorSFIM = new tmm.sfim.TopicDetector();
      topics = topicDetectorSFIM.createTopics(tweets);
    }
    String showSTDstr = ParameterUtilities.readProperty(MainConstants.SHOW_TOPICS_ON_STD, mainConfig.getConfig());
    Boolean showTopicsOnStd = Boolean.valueOf(false);
    if (showSTDstr.trim().equals("1")) {
      showTopicsOnStd = Boolean.valueOf(true);
    }
    if (showTopicsOnStd.booleanValue())
    {
      System.out.println("No of topics: " + topics.size());
      System.out.println("---------------");
      for (int i = 0; i < topics.size(); i++)
      {
        Topic tmp_topic = (Topic)topics.get(i);
        List<Ngram> keywords = tmp_topic.getKeywords();
        System.out.println("");
        System.out.println("Topic: " + (i + 1));
        for (int j = 0; j < keywords.size(); j++) {
          System.out.print(((Ngram)keywords.get(j)).getTerm().toString().replace(":", "") + " ");
        }
        System.out.println();
      }
    }
    String saveFileStr = ParameterUtilities.readProperty(MainConstants.SAVE_TOPICS_ON_FILE, mainConfig.getConfig());
    Boolean saveFiles = Boolean.valueOf(false);
    if (saveFileStr.trim().equals("1")) {
      saveFiles = Boolean.valueOf(true);
    }
    if (saveFiles.booleanValue())
    {
      String targetFilename = ParameterUtilities.readProperty(MainConstants.RESULTS_FILE, mainConfig.getConfig());
      String targetDirectory = ParameterUtilities.readProperty(MainConstants.RESULTS_DIRECTORY, mainConfig.getConfig());
      Integer nTopicsToSave = Integer.valueOf(Integer.parseInt(ParameterUtilities.readProperty(MainConstants.N_TOPICS_TO_SAVE, mainConfig.getConfig())));
      if (nTopicsToSave.intValue() > topics.size()) {
        nTopicsToSave = Integer.valueOf(topics.size());
      }
      File tmpF = new File(targetDirectory);
      if (!tmpF.exists()) {
        tmpF.mkdirs();
      }
      try
      {
        BufferedWriter bw = new BufferedWriter(new FileWriter(targetDirectory + targetFilename));
        for (int i = 0; i < nTopicsToSave.intValue(); i++)
        {
          Topic tmp_topic = (Topic)topics.get(i);
          List<Ngram> keywords = tmp_topic.getKeywords();
          for (int j = 0; j < keywords.size(); j++) {
            bw.append(((Ngram)keywords.get(j)).getTerm().toString().replace(":", "") + " ");
          }
          bw.newLine();
        }
        bw.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}
