package tmm.bngram.topicDetector;

import com.google.gson.internal.Pair;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import tmm.Ngram;
import tmm.bngram.topicMatcher.DyscoComparer;

public class TrendingTopic
{
  private Date time;
  private File file;
  private IndexReader reader;
  private SlotInformation[] slotInformation;
  private MonitorClusters monitorClusters;
  private List<Topic> topics;
  
  TrendingTopic(Date t)
  {
    this.time = t;
    this.file = new File(Utils.GetIndexFolder());
    this.slotInformation = new SlotInformation[GlobalParameters.dfIdf + 1];
    this.topics = new ArrayList();
  }
  
  public SlotInformation[] getSlotInformation()
  {
    return this.slotInformation;
  }
  
  private void GetRepresentativeTerms()
  {
    int cont = 0;
    Date currentTime = this.time;
    
    ExecutorService threadExecutor = Executors.newFixedThreadPool(GlobalParameters.dfIdf + 1);
    try
    {
      IndexSearcher searcher = new IndexSearcher(this.reader);
      while (cont <= GlobalParameters.dfIdf)
      {
        Date previousTime = Utils.previousTime(currentTime, GlobalParameters.period);
        RepresentativeTermsThread representativeTermsThread = new RepresentativeTermsThread(currentTime, previousTime, this.reader, searcher, this.slotInformation, cont);
        threadExecutor.execute(representativeTermsThread);
        currentTime = previousTime;
        cont++;
      }
      threadExecutor.shutdown();
      try
      {
        threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if (this.slotInformation[0].getNgramTweets().size() == 0) {
        return;
      }
      ComputeDfIdf();
      
      Utils.SortByDfIdf(this.slotInformation[0].getRepresentativeNgrams());
      
      RemoveOverlaps();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private void GetRepresentativeTermsBaseline()
  {
    int cont = 0;
    Date currentTime = this.time;
    
    ExecutorService threadExecutor = Executors.newFixedThreadPool(GlobalParameters.dfIdf + 1);
    try
    {
      IndexSearcher searcher = new IndexSearcher(this.reader);
      while (cont <= 1)
      {
        Date previousTime = Utils.previousTime(currentTime, GlobalParameters.period);
        if (cont == 1)
        {
          previousTime = GlobalParameters.startTime;
          currentTime = GlobalParameters.stopTime;
        }
        RepresentativeTermsThread representativeTermsThread = new RepresentativeTermsThread(currentTime, previousTime, this.reader, searcher, this.slotInformation, cont);
        threadExecutor.execute(representativeTermsThread);
        currentTime = previousTime;
        cont++;
      }
      threadExecutor.shutdown();
      try
      {
        threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if (this.slotInformation[0].getNgramTweets().size() == 0) {
        return;
      }
      ComputeDfIdfBaseline();
      
      Utils.SortByDfIdf(this.slotInformation[0].getRepresentativeNgrams());
      
      RemoveOverlaps();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private void ComputeDfIdf()
    throws IOException
  {
    if (this.slotInformation[0].getNgramTweets().size() == 0) {
      return;
    }
    Iterator<Map.Entry<Term, List<Integer>>> terms = Utils.SortByNgramTweets(this.slotInformation[0].getNgramTweets()).entrySet().iterator();
    
    int cont = 0;
    while ((terms.hasNext()) && (cont <= 10000))
    {
      Map.Entry<Term, List<Integer>> term = (Map.Entry)terms.next();
      
      float boost = 1.0F;
      List<List<CoreLabel>> out = GlobalParameters.classifier.classify(((Term)term.getKey()).text());
      for (List<CoreLabel> sentence : out) {
        for (CoreLabel word : sentence)
        {
          String category = (String)word.get(CoreAnnotations.AnswerAnnotation.class);
          if ((category.equals("PERSON")) || (category.equals("LOCATION")) || (category.equals("ORGANIZATION")))
          {
            boost = 1.5F;
            break;
          }
        }
      }
      int numDocs = 0;
      for (int i = 1; i <= GlobalParameters.dfIdf; i++) {
        if ((this.slotInformation[i].getNgramTweets().size() != 0) && (this.slotInformation[i].getNgramTweets().get(term.getKey()) != null)) {
          numDocs += ((List)this.slotInformation[i].getNgramTweets().get(term.getKey())).size();
        }
      }
      Float dfIdft = Float.valueOf((float)((((List)term.getValue()).size() + 1) / (Math.log(numDocs / GlobalParameters.dfIdf + 1.0F) + 1.0D) * boost));
      this.slotInformation[0].AddRepresentativeNgram(new Ngram((Term)term.getKey(), dfIdft));
      cont++;
    }
    System.out.println(this.slotInformation[0].getRepresentativeNgrams().size());
  }
  
  private void ComputeDfIdfBaseline()
    throws IOException
  {
    if (this.slotInformation[0].getNgramTweets().size() == 0) {
      return;
    }
    Iterator<Map.Entry<Term, List<Integer>>> terms = this.slotInformation[0].getNgramTweets().entrySet().iterator();
    while (terms.hasNext())
    {
      Map.Entry<Term, List<Integer>> term = (Map.Entry)terms.next();
      
      float boost = 1.0F;
      List<List<CoreLabel>> out = GlobalParameters.classifier.classify(((Term)term.getKey()).text());
      for (List<CoreLabel> sentence : out) {
        for (CoreLabel word : sentence)
        {
          String category = (String)word.get(CoreAnnotations.AnswerAnnotation.class);
          if ((category.equals("PERSON")) || (category.equals("LOCATION")) || (category.equals("ORGANIZATION")))
          {
            boost = 1.5F;
            break;
          }
        }
      }
      int numDocs = 0;
      for (int i = 1; i <= GlobalParameters.dfIdf; i++) {
        numDocs += this.reader.docFreq((Term)term.getKey());
      }
      Float dfIdft = Float.valueOf((float)((((List)term.getValue()).size() + 1) / (Math.log((numDocs - ((List)term.getValue()).size()) / GlobalParameters.dfIdf + 1.0F) + 1.0D) * boost));
      this.slotInformation[0].AddRepresentativeNgram(new Ngram((Term)term.getKey(), dfIdft));
    }
  }
  
  private void RemoveOverlaps()
    throws IOException
  {
    if (this.slotInformation[0].getRepresentativeNgrams().size() == 0) {
      return;
    }
    List<Ngram> ngramsTemp = new ArrayList();
    Iterator<Ngram> ngramsIterator = this.slotInformation[0].getRepresentativeNgrams().iterator();
    while ((ngramsIterator.hasNext()) && (ngramsTemp.size() < GlobalParameters.top))
    {
      boolean isContained = false;
      Ngram ngram = (Ngram)ngramsIterator.next();
      Iterator<Ngram> ngramsTempIterator = ngramsTemp.iterator();
      while (ngramsTempIterator.hasNext())
      {
        Ngram ngramTemp = (Ngram)ngramsTempIterator.next();
        float distance = GetDistanceDocumentsNgrams(ngram, ngramTemp);
        if ((ngramTemp.getTerm().text().contains(ngram.getTerm().text())) && (distance < GlobalParameters.minimumDistanceClusters))
        {
          isContained = true;
          break;
        }
        if ((ngram.getTerm().text().contains(ngramTemp.getTerm().text())) && (distance < GlobalParameters.minimumDistanceClusters))
        {
          ngramsTempIterator.remove();
          break;
        }
      }
      if (!isContained) {
        ngramsTemp.add(ngram);
      }
    }
    this.slotInformation[0].setRepresentativeNgrams(ngramsTemp);
  }
  
  public float GetDistanceDocumentsNgrams(Ngram ngram1, Ngram ngram2)
    throws IOException
  {
    if ((!this.slotInformation[0].getRepresentativeNgrams().contains(ngram1)) || (!this.slotInformation[0].getRepresentativeNgrams().contains(ngram2))) {
      return 1.0F;
    }
    List<Integer> tweets = new ArrayList(this.slotInformation[0].GetTweets(ngram1.getTerm()));
    tweets.retainAll(this.slotInformation[0].GetTweets(ngram2.getTerm()));
    float distance = 1.0F - tweets.size() / Math.min(this.slotInformation[0].GetTweets(ngram1.getTerm()).size(), this.slotInformation[0].GetTweets(ngram2.getTerm()).size());
    return distance;
  }
  
  private void GetTrendingTopics()
  {
    try
    {
      FSDirectory dir = FSDirectory.open(this.file);
      this.reader = DirectoryReader.open(dir);
      
      System.out.println("Getting representative terms...");
      GetRepresentativeTerms();
      
      System.out.println("Getting table of distances...");
      this.monitorClusters = new MonitorClusters(this.slotInformation, this.reader);
      this.monitorClusters.GetDistanceTopicsTable();
      
      System.out.println("Getting clusters...");
      this.monitorClusters.GetClusters();
      
      System.out.println("Sorting clusters...");
      this.topics = this.monitorClusters.GetClustersRanking();
      if (this.reader != null) {
        this.reader.close();
      }
      if (dir != null) {
        dir.close();
      }
    }
    catch (CorruptIndexException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static List<tmm.Topic> GetTrending()
  {
    List<tmm.Topic> result = new ArrayList();
    if ((GlobalParameters.topicDates == null) && ((GlobalParameters.startTime == null) || (GlobalParameters.stopTime == null) || (GlobalParameters.startTime.compareTo(GlobalParameters.stopTime) >= 0))) {
      return null;
    }
    List<Pair<Topic, List<Date>>> generalTopics = new ArrayList();
    
    GetTrendingTime(GlobalParameters.topicDate, generalTopics);
    for (Pair<Topic, List<Date>> tmp_pair : generalTopics)
    {
      Topic tmp_topic = (Topic)tmp_pair.first;
      
      tmm.Topic new_tmp_topic = new tmm.Topic();
      Set<String> keywords = new HashSet();
      for (Ngram tmp_ngram : tmp_topic.getNgram())
      {
        String[] parts = tmp_ngram.getTerm().toString().split(" ");
        for (int i = 0; i < parts.length; i++) {
          keywords.add(parts[i].replace("text", "").replace("_", "").trim());
        }
      }
      List<Ngram> keywords_new = new ArrayList();
      for (String tmp_s : keywords) {
        keywords_new.add(new Ngram(new Term(tmp_s), null));
      }
      new_tmp_topic.setKeywords(keywords_new);
      result.add(new_tmp_topic);
    }
    return result;
  }
  
  private static void GetTrendingTime(Date currentTime, List<Pair<Topic, List<Date>>> generalTopics)
  {
    Date nextTime = Utils.nextTime(currentTime, GlobalParameters.period);
    TrendingTopic trendingTopic = new TrendingTopic(nextTime);
    trendingTopic.GetTrendingTopics();
    List<Date> date;
    if (GlobalParameters.matcher > 0.0F)
    {
      MatchTopics(trendingTopic, generalTopics, nextTime);
    }
    else
    {
      date = new ArrayList();
      date.add(nextTime);
      for (Topic topic : trendingTopic.topics) {
        generalTopics.add(new Pair(topic, date));
      }
    }
  }
  
  private static void MatchTopics(TrendingTopic trendingTopic, List<Pair<Topic, List<Date>>> generalTopics, Date nextTime)
  {
    try
    {
      for (Topic candidateTopic : trendingTopic.topics)
      {
        boolean matched = false;
        for (Pair<Topic, List<Date>> topic : generalTopics) {
          if (DyscoComparer.CompareDyscos(candidateTopic, (Topic)topic.first) > GlobalParameters.matcher)
          {
            ((Topic)topic.first).AddNgrams(candidateTopic.getNgram());
            ((Topic)topic.first).AddTweets(candidateTopic.getTweets());
            ((List)topic.second).add(nextTime);
            matched = true;
          }
        }
        if (!matched)
        {
          List<Date> dates = new ArrayList();
          dates.add(nextTime);
          generalTopics.add(new Pair(candidateTopic, dates));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
