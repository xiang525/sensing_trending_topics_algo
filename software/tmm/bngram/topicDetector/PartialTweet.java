package tmm.bngram.topicDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import tmm.Ngram;

public class PartialTweet
{
  private List<Ngram> ngrams;
  private String idTwitter;
  private int idIndex;
  private String text;
  private String user;
  private String date;
  private static final String FIELD_ID = "id";
  private static final String FIELD_TEXT = "text";
  private static final String FIELD_USER = "user";
  private static final String FIELD_TIME_STR = "time_str";
  
  public PartialTweet(IndexReader reader, int numDoc, List<Ngram> ngrams)
    throws CorruptIndexException, IOException
  {
    this.ngrams = new ArrayList();
    this.ngrams.addAll(ngrams);
    Document document = reader.document(numDoc);
    this.idIndex = numDoc;
    this.idTwitter = document.get("id");
    this.text = document.get("text");
    this.user = document.get("user");
    this.date = document.get("time_str");
  }
  
  public String getIdTwitter()
  {
    return this.idTwitter;
  }
  
  public int getIdIndex()
  {
    return this.idIndex;
  }
  
  public String getText()
  {
    return this.text;
  }
  
  public String getUser()
  {
    return this.user;
  }
  
  public String getDate()
  {
    return this.date;
  }
  
  public List<Ngram> getNgrams()
  {
    return this.ngrams;
  }
  
  public boolean equals(Object partialTweet)
  {
    if (this == partialTweet) {
      return true;
    }
    if (!(partialTweet instanceof PartialTweet)) {
      return false;
    }
    PartialTweet partialTweet1 = (PartialTweet)partialTweet;
    if (!this.idTwitter.equals(partialTweet1.getIdTwitter())) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = 17;
    
    result = 31 * result + this.idTwitter.hashCode();
    return result;
  }
}
