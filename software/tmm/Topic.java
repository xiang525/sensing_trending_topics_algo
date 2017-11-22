package tmm;

import java.util.ArrayList;
import java.util.List;

public class Topic
{
  List<Ngram> keywords;
  double score;
  String id;
  String title;
  List<Tweet> relevantTweets;
  
  public Topic()
  {
    this.keywords = new ArrayList();
    this.relevantTweets = new ArrayList();
  }
  
  public List<Ngram> getKeywords()
  {
    return this.keywords;
  }
  
  public double getScore()
  {
    return this.score;
  }
  
  public void setScore(double score)
  {
    this.score = score;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public String getTitle()
  {
    return this.title;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public void setKeywords(List<Ngram> keywords)
  {
    this.keywords = keywords;
  }
  
  public void setTitle(String title)
  {
    this.title = title;
  }
  
  public List<Tweet> getRelevantTweets()
  {
    return this.relevantTweets;
  }
  
  public void setRelevantTweets(List<Tweet> relevantTweets)
  {
    this.relevantTweets = relevantTweets;
  }
}
