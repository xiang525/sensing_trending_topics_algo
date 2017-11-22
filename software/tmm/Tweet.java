package tmm;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import tmm.documentpivot.preprocessing.TweetPreprocessor;

public class Tweet
{
  String text;
  String id;
  String uploader;
  Long uploadTime;
  
  public Tweet(String text, String id, String uploader, Long uploadTime)
  {
    this.text = text;
    this.id = id;
    this.uploader = uploader;
    this.uploadTime = uploadTime;
  }
  
  public Tweet(String jsonSource)
  {
    DBObject dbObject = (DBObject)JSON.parse(jsonSource);
    this.id = ((String)dbObject.get("id_str"));
    this.text = ((String)dbObject.get("text"));
    DBObject tmp_obj = (DBObject)dbObject.get("user");
    this.uploader = ((String)tmp_obj.get("screen_name"));
    this.text = ((String)dbObject.get("text"));
    this.uploadTime = parseTwitterDate((String)dbObject.get("created_at"));
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public String getText()
  {
    return this.text;
  }
  
  public Long getUploadTime()
  {
    return this.uploadTime;
  }
  
  public String getUploader()
  {
    return this.uploader;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public void setText(String text)
  {
    this.text = text;
  }
  
  public void setUploadTime(Long uploadTime)
  {
    this.uploadTime = uploadTime;
  }
  
  public void setUploader(String uploader)
  {
    this.uploader = uploader;
  }
  
  public static Long parseTwitterDate(String dateStr)
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    
    dateFormat.setLenient(false);
    Long created = null;
    try
    {
      return Long.valueOf(dateFormat.parse(dateStr).getTime());
    }
    catch (Exception e) {}
    return null;
  }
  
  public List<String> getTerms()
  {
    List<String> tokens = new ArrayList();
    tokens = TweetPreprocessor.Tokenize(this, false, false);
    return tokens;
  }
}
