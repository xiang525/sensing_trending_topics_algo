package tmm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.Term;

public class SlotInformation
  implements JSONable
{
  @SerializedName("timeslotId")
  private String timeslotId;
  @SerializedName("totalTweets")
  private int totalTweets;
  @SerializedName("representativeNgrams")
  private List<Ngram> representativeNgrams;
  @SerializedName("ngramTweets")
  private Map<Term, List<Integer>> ngramTweets;
  
  public SlotInformation()
  {
    this.timeslotId = null;
    this.totalTweets = 0;
    this.representativeNgrams = new ArrayList();
    this.ngramTweets = new HashMap();
  }
  
  public String getTimeslotId()
  {
    return this.timeslotId;
  }
  
  public void setTimeslotId(String timeslotId)
  {
    this.timeslotId = timeslotId;
  }
  
  public int getTotalTweets()
  {
    return this.totalTweets;
  }
  
  public void setTotalTweets(int totalTweets)
  {
    this.totalTweets = totalTweets;
  }
  
  public List<Ngram> getRepresentativeNgrams()
  {
    return this.representativeNgrams;
  }
  
  public void setRepresentativeNgrams(List<Ngram> ngrams)
  {
    this.representativeNgrams = ngrams;
  }
  
  public Map<Term, List<Integer>> getNgramTweets()
  {
    return this.ngramTweets;
  }
  
  public void setNgramTweets(Map<Term, List<Integer>> ngramTweets)
  {
    this.ngramTweets = ngramTweets;
  }
  
  public void AddNgramTweet(Term ngram)
  {
    if (this.ngramTweets.containsKey(ngram)) {
      return;
    }
    this.ngramTweets.put(ngram, new ArrayList());
  }
  
  public void AddNgramTweet(Term ngram, Integer tweet)
  {
    if (!this.ngramTweets.containsKey(ngram)) {
      AddNgramTweet(ngram);
    }
    ((List)this.ngramTweets.get(ngram)).add(tweet);
  }
  
  public List<Integer> GetTweets(Term ngram)
  {
    return (List)this.ngramTweets.get(ngram);
  }
  
  public void AddRepresentativeNgram(Ngram ngram)
  {
    this.representativeNgrams.add(ngram);
  }
  
  public String toJSONString()
  {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    return gson.toJson(this);
  }
}
