package tmm.bngram.topicDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import tmm.Ngram;

public class Topic
{
  private IndexReader reader;
  private List<Ngram> ngrams;
  private Float score;
  private List<PartialTweet> tweets;
  
  public Topic(IndexReader reader)
  {
    this.reader = reader;
    this.ngrams = new ArrayList();
    this.score = Float.valueOf(0.0F);
    this.tweets = new ArrayList();
  }
  
  public List<PartialTweet> getTweets()
  {
    return this.tweets;
  }
  
  public List<Ngram> getNgram()
  {
    return this.ngrams;
  }
  
  public Float getScore()
  {
    return this.score;
  }
  
  public void AddNgrams(List<Ngram> ngrams)
  {
    if (ngrams.size() == 0) {
      return;
    }
    Iterator<Ngram> iteratorNgrams = ngrams.iterator();
    while (iteratorNgrams.hasNext())
    {
      Ngram ngram = (Ngram)iteratorNgrams.next();
      this.ngrams.add(ngram);
      if (ngram.getScore().floatValue() > this.score.floatValue()) {
        this.score = ngram.getScore();
      }
    }
  }
  
  public void AddTweet(PartialTweet partialTweet)
    throws IOException
  {
    if (this.tweets.contains(partialTweet))
    {
      int index = this.tweets.indexOf(partialTweet);
      PartialTweet partialTweetTemp = (PartialTweet)this.tweets.get(index);
      Iterator<Ngram> iteratorNgrams = partialTweet.getNgrams().iterator();
      while (iteratorNgrams.hasNext())
      {
        Ngram ngram = (Ngram)iteratorNgrams.next();
        if (!partialTweetTemp.getNgrams().contains(ngram)) {
          partialTweetTemp.getNgrams().add(ngram);
        }
      }
      return;
    }
    this.tweets.add(partialTweet);
  }
  
  public void AddTweets(List<Integer> newtweets, List<Ngram> ngrams)
    throws CorruptIndexException, IOException
  {
    if (newtweets.size() == 0) {
      return;
    }
    Iterator<Integer> iteratorTweets = newtweets.iterator();
    while (iteratorTweets.hasNext())
    {
      PartialTweet partialTweet = new PartialTweet(this.reader, ((Integer)iteratorTweets.next()).intValue(), ngrams);
      AddTweet(partialTweet);
    }
  }
  
  public void AddTweets(List<PartialTweet> newtweets)
    throws CorruptIndexException, IOException
  {
    if (newtweets.size() == 0) {
      return;
    }
    Iterator<PartialTweet> iteratorTweets = newtweets.iterator();
    while (iteratorTweets.hasNext()) {
      AddTweet((PartialTweet)iteratorTweets.next());
    }
  }
  
  public String GetTopicText()
  {
    Iterator<Ngram> iteratorNgrams = this.ngrams.iterator();
    List<String> listTerm = new ArrayList();
    while (iteratorNgrams.hasNext())
    {
      Ngram ngram = (Ngram)iteratorNgrams.next();
      String[] termArray = ngram.getTerm().text().split(" ");
      for (int i = 0; i < termArray.length; i++) {
        if ((!listTerm.contains(termArray[i])) && (!termArray[i].equals("_"))) {
          listTerm.add(termArray[i]);
        }
      }
    }
    Iterator<String> listTermIterator = listTerm.iterator();
    String topic = new String();
    while (listTermIterator.hasNext()) {
      topic = topic + (String)listTermIterator.next() + " ";
    }
    return topic.trim();
  }
  
  public List<PartialTweet> GetSortedTweets()
  {
    if (this.tweets.size() == 0) {
      return null;
    }
    List<PartialTweet> tweetsList = new ArrayList(this.tweets);
    Utils.SortByNgramTweets(tweetsList);
    return tweetsList;
  }
  
  public String GetTitle()
  {
    Utils.SortByDfIdf(this.ngrams);
    
    List<String> terms = new ArrayList();
    for (Ngram ngram : this.ngrams)
    {
      if (terms.size() >= 3) {
        break;
      }
      for (String term : ngram.GetNgramTextNoSpaceCapitalize()) {
        if (!terms.contains(term))
        {
          terms.add(term);
          if (terms.size() >= 3) {
            break;
          }
        }
      }
    }
    String title = "";
    for (String term : terms) {
      title = title + term;
    }
    return title;
  }
  
  public boolean equals(Object topic)
  {
    if (this == topic) {
      return true;
    }
    if (!(topic instanceof Topic)) {
      return false;
    }
    Topic topic1 = (Topic)topic;
    
    Iterator<Ngram> iteratorNgram = topic1.ngrams.iterator();
    while (iteratorNgram.hasNext()) {
      if (!this.ngrams.contains(iteratorNgram.next())) {
        return false;
      }
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = 17;
    
    result = 31 * result + (this.ngrams != null ? this.ngrams.hashCode() : 0);
    result = 31 * result + Float.floatToIntBits(this.score.floatValue());
    result = 31 * result + (this.tweets != null ? this.tweets.hashCode() : 0);
    return result;
  }
}
