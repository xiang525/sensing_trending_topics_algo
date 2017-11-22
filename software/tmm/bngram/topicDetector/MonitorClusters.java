package tmm.bngram.topicDetector;

import com.google.gson.internal.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import tmm.Ngram;

public class MonitorClusters
{
  private SlotInformation[] slotInformation;
  private Map<Topic, Map<Topic, Pair<Float, Float>>> distanceTopicsTable;
  private IndexReader reader;
  
  public MonitorClusters(SlotInformation[] slotInformation, IndexReader reader)
  {
    this.slotInformation = slotInformation;
    this.distanceTopicsTable = new HashMap();
    this.reader = reader;
  }
  
  public void GetDistanceTopicsTable()
    throws IOException
  {
    if (this.slotInformation[0].getRepresentativeNgrams().size() == 0) {
      return;
    }
    Topic[] topics = CreateTopics();
    for (int i = 0; i < Math.min(GlobalParameters.top, this.slotInformation[0].getRepresentativeNgrams().size()) - 1; i++)
    {
      Map<Topic, Pair<Float, Float>> listTopicRow = new HashMap();
      for (int j = i + 1; j < Math.min(GlobalParameters.top, this.slotInformation[0].getRepresentativeNgrams().size()); j++)
      {
        Pair<Float, Float> distance = GetDistanceDocumentsNgrams((Ngram)topics[i].getNgram().get(0), (Ngram)topics[j].getNgram().get(0));
        
        listTopicRow.put(topics[j], distance);
        
        Map<Topic, Pair<Float, Float>> listTopicColumn = new HashMap();
        listTopicColumn.put(topics[i], distance);
        AddDistances(topics[j], listTopicColumn);
      }
      AddDistances(topics[i], listTopicRow);
    }
  }
  
  private Topic[] CreateTopics()
    throws CorruptIndexException, IOException
  {
    Topic[] topics = new Topic[Math.min(GlobalParameters.top, this.slotInformation[0].getRepresentativeNgrams().size())];
    for (int i = 0; i < Math.min(GlobalParameters.top, this.slotInformation[0].getRepresentativeNgrams().size()); i++)
    {
      Topic topic = new Topic(this.reader);
      List<Ngram> ngrams = new ArrayList();
      ngrams.add(this.slotInformation[0].getRepresentativeNgrams().get(i));
      topic.AddNgrams(ngrams);
      topic.AddTweets((List)this.slotInformation[0].getNgramTweets().get(((Ngram)this.slotInformation[0].getRepresentativeNgrams().get(i)).getTerm()), ngrams);
      topics[i] = topic;
    }
    return topics;
  }
  
  private void AddDistances(Topic topic, Map<Topic, Pair<Float, Float>> listTopicRowNew)
  {
    if (!this.distanceTopicsTable.containsKey(topic))
    {
      this.distanceTopicsTable.put(topic, listTopicRowNew);
    }
    else
    {
      Map<Topic, Pair<Float, Float>> listTopicRow = (Map)this.distanceTopicsTable.get(topic);
      listTopicRow.putAll(listTopicRowNew);
    }
  }
  
  public Pair<Float, Float> GetDistanceDocumentsNgrams(Ngram ngram1, Ngram ngram2)
    throws IOException
  {
    if ((!this.slotInformation[0].getRepresentativeNgrams().contains(ngram1)) || (!this.slotInformation[0].getRepresentativeNgrams().contains(ngram2))) {
      return new Pair(Float.valueOf(1.0F), Float.valueOf(0.0F));
    }
    List<Integer> tweets = new ArrayList(this.slotInformation[0].GetTweets(ngram1.getTerm()));
    tweets.retainAll(this.slotInformation[0].GetTweets(ngram2.getTerm()));
    float distance = 1.0F - tweets.size() / Math.min(this.slotInformation[0].GetTweets(ngram1.getTerm()).size(), this.slotInformation[0].GetTweets(ngram2.getTerm()).size());
    return new Pair(Float.valueOf(distance), Float.valueOf(tweets.size()));
  }
  
  public void GetClusters()
    throws IOException
  {
    if (this.distanceTopicsTable.size() == 0) {
      return;
    }
    do
    {
      Pair<Topic, Topic> clustersMinimum = GetClustersMinimumDistance();
      if (clustersMinimum == null) {
        break;
      }
      Topic newTopic = new Topic(this.reader);
      newTopic.AddNgrams(((Topic)clustersMinimum.first).getNgram());
      newTopic.AddNgrams(((Topic)clustersMinimum.second).getNgram());
      newTopic.AddTweets(((Topic)clustersMinimum.first).getTweets());
      newTopic.AddTweets(((Topic)clustersMinimum.second).getTweets());
      
      AddTopicTable(clustersMinimum, newTopic);
      
      UpdateDistanceClusterAdded(clustersMinimum, newTopic);
      
      this.distanceTopicsTable.remove(clustersMinimum.first);
      this.distanceTopicsTable.remove(clustersMinimum.second);
    } while (this.distanceTopicsTable.size() > 1);
  }
  
  private Pair<Topic, Topic> GetClustersMinimumDistance()
    throws IOException
  {
    float minimumDistance = 1.0F;
    float commonDocuments = 0.0F;
    Topic clusterCandidateA = null;Topic clusterCandidateB = null;
    
    Iterator<Map.Entry<Topic, Map<Topic, Pair<Float, Float>>>> distanceTableIterator = this.distanceTopicsTable.entrySet().iterator();
    while (distanceTableIterator.hasNext())
    {
      Map.Entry<Topic, Map<Topic, Pair<Float, Float>>> distanceTableEntry = (Map.Entry)distanceTableIterator.next();
      Iterator<Map.Entry<Topic, Pair<Float, Float>>> rowTableIterator = ((Map)distanceTableEntry.getValue()).entrySet().iterator();
      while (rowTableIterator.hasNext())
      {
        Map.Entry<Topic, Pair<Float, Float>> distanceTopics = (Map.Entry)rowTableIterator.next();
        float distanceNgrams = GetDistanceWordsTerms((Topic)distanceTableEntry.getKey(), (Topic)distanceTopics.getKey());
        if (((((Float)((Pair)distanceTopics.getValue()).first).floatValue() < minimumDistance) || ((((Float)((Pair)distanceTopics.getValue()).first).floatValue() == minimumDistance) && (((Float)((Pair)distanceTopics.getValue()).second).floatValue() > commonDocuments))) && ((((Float)((Pair)distanceTopics.getValue()).first).floatValue() < GlobalParameters.minimumDistanceClusters) || ((((Float)((Pair)distanceTopics.getValue()).first).floatValue() < GlobalParameters.maximumDistanceClusters) && (distanceNgrams < GlobalParameters.maximumDistanceTerms)) || (distanceNgrams == 0.0F)))
        {
          clusterCandidateA = (Topic)distanceTableEntry.getKey();
          clusterCandidateB = (Topic)distanceTopics.getKey();
          minimumDistance = ((Float)((Pair)distanceTopics.getValue()).first).floatValue();
          commonDocuments = ((Float)((Pair)distanceTopics.getValue()).second).floatValue();
        }
      }
    }
    if ((clusterCandidateA == null) && (clusterCandidateB == null)) {
      return null;
    }
    return new Pair(clusterCandidateA, clusterCandidateB);
  }
  
  private void AddTopicTable(Pair<Topic, Topic> clustersMinimum, Topic newTopic)
    throws CorruptIndexException, IOException
  {
    Map<Topic, Pair<Float, Float>> newTopicDistances = new HashMap();
    Iterator<Map.Entry<Topic, Pair<Float, Float>>> topicsIterator = ((Map)this.distanceTopicsTable.get(clustersMinimum.first)).entrySet().iterator();
    while (topicsIterator.hasNext())
    {
      Map.Entry<Topic, Pair<Float, Float>> topicEntry = (Map.Entry)topicsIterator.next();
      if (!((Topic)topicEntry.getKey()).equals(clustersMinimum.second))
      {
        float distance = (((Float)((Pair)topicEntry.getValue()).first).floatValue() + ((Float)((Pair)((Map)this.distanceTopicsTable.get(clustersMinimum.second)).get(topicEntry.getKey())).first).floatValue()) / 2.0F;
        float commonDocuments = (((Float)((Pair)topicEntry.getValue()).second).floatValue() + ((Float)((Pair)((Map)this.distanceTopicsTable.get(clustersMinimum.second)).get(topicEntry.getKey())).second).floatValue()) / 2.0F;
        newTopicDistances.put(topicEntry.getKey(), new Pair(Float.valueOf(distance), Float.valueOf(commonDocuments)));
      }
    }
    this.distanceTopicsTable.put(newTopic, newTopicDistances);
  }
  
  private void UpdateDistanceClusterAdded(Pair<Topic, Topic> clustersMinimum, Topic newTopic)
  {
    Iterator<Map.Entry<Topic, Map<Topic, Pair<Float, Float>>>> distanceTableIterator = this.distanceTopicsTable.entrySet().iterator();
    while (distanceTableIterator.hasNext())
    {
      Map.Entry<Topic, Map<Topic, Pair<Float, Float>>> distanceTableEntry = (Map.Entry)distanceTableIterator.next();
      if ((!((Topic)distanceTableEntry.getKey()).equals(clustersMinimum.first)) && (!((Topic)distanceTableEntry.getKey()).equals(clustersMinimum.second)) && (!((Topic)distanceTableEntry.getKey()).equals(newTopic)))
      {
        float distance = (((Float)((Pair)((Map)distanceTableEntry.getValue()).get(clustersMinimum.first)).first).floatValue() + ((Float)((Pair)((Map)distanceTableEntry.getValue()).get(clustersMinimum.second)).first).floatValue()) / 2.0F;
        float commonDocuments = (((Float)((Pair)((Map)distanceTableEntry.getValue()).get(clustersMinimum.first)).second).floatValue() + ((Float)((Pair)((Map)distanceTableEntry.getValue()).get(clustersMinimum.second)).second).floatValue()) / 2.0F;
        ((Map)distanceTableEntry.getValue()).put(newTopic, new Pair(Float.valueOf(distance), Float.valueOf(commonDocuments)));
        ((Map)distanceTableEntry.getValue()).remove(clustersMinimum.first);
        ((Map)distanceTableEntry.getValue()).remove(clustersMinimum.second);
      }
    }
  }
  
  private float GetDistanceWordsTerms(Topic topic1, Topic topic2)
  {
    List<String> listTerms1 = Arrays.asList(topic1.GetTopicText().split(" "));
    List<String> listTerms2 = Arrays.asList(topic2.GetTopicText().split(" "));
    int cont = 0;
    Iterator<String> iteratorWords = listTerms1.iterator();
    while (iteratorWords.hasNext()) {
      if (listTerms2.contains(iteratorWords.next())) {
        cont++;
      }
    }
    return 1.0F - cont / Math.min(listTerms1.size(), listTerms2.size());
  }
  
  public List<Topic> GetClustersRanking()
    throws IOException
  {
    List<Topic> topics = new ArrayList();
    Iterator<Map.Entry<Topic, Map<Topic, Pair<Float, Float>>>> distanceTopicsTableIterator = this.distanceTopicsTable.entrySet().iterator();
    while (distanceTopicsTableIterator.hasNext()) {
      topics.add(((Map.Entry)distanceTopicsTableIterator.next()).getKey());
    }
    Utils.SortByDfIdfClusters(topics);
    return topics;
  }
}
