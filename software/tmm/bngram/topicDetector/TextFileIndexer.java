package tmm.bngram.topicDetector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import tmm.documentpivot.preprocessing.StopWords;

public class TextFileIndexer
{
  private static IndexWriter writer;
  private static FSDirectory dir;
  
  public static void SetupIndex()
  {
    try
    {
      dir = FSDirectory.open(new File(Utils.GetIndexFolder()));
      
      String[] stopwordsLinesAr = StopWords.stopWords.split("\n");
      List<String> stopwordsLines = new ArrayList(Arrays.asList(stopwordsLinesAr));
      Set<String> stopwordsSet = new HashSet(stopwordsLines);
      
      TweetAnalyzer tweetAnalyzer = new TweetAnalyzer(Version.LUCENE_40, stopwordsSet);
      IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, tweetAnalyzer);
      
      writer = new IndexWriter(dir, config);
      writer.deleteAll();
      writer.commit();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static boolean CreateIndex(DBCursor cur)
  {
    try
    {
      Iterator<DBObject> itDBObject = cur.iterator();
      while (itDBObject.hasNext())
      {
        Document doc = new Document();
        DBObject dBObject = (DBObject)itDBObject.next();
        FieldType fieldType = new FieldType();
        fieldType.setIndexed(true);
        fieldType.setStored(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.freeze();
        doc.add(new Field("text", dBObject.get("token").toString(), fieldType));
        doc.add(new StoredField("id", dBObject.get("id").toString()));
        doc.add(new StoredField("user", dBObject.get("author").toString()));
        doc.add(new StoredField("time_str", ((Long)dBObject.get("publicationTime")).longValue()));
        doc.add(new LongField("time", ((Long)dBObject.get("publicationTime")).longValue(), Field.Store.NO));
        writer.addDocument(doc);
      }
      writer.commit();
      return true;
    }
    catch (CorruptIndexException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static boolean CreateIndexTimeAggregationCollections()
  {
    JsonParser parser = new JsonParser();
    try
    {
      List<String> filesIndex = new ArrayList();
      String jsonFile = null;
      for (Date date : GlobalParameters.topicDates)
      {
        Date currentDate = date;
        int cont = 0;
        while (cont <= GlobalParameters.dfIdf)
        {
          jsonFile = Utils.GetJsonFile(currentDate);
          if (filesIndex.contains(jsonFile))
          {
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
            cont++;
          }
          else
          {
            FileReader fr = new FileReader(jsonFile);
            BufferedReader br = new BufferedReader(fr);
            
            String line = null;
            while ((line = br.readLine()) != null)
            {
              JsonElement jsonElement = parser.parse(line);
              Document doc = new Document();
              FieldType fieldType = new FieldType();
              fieldType.setIndexed(true);
              fieldType.setStored(true);
              fieldType.setTokenized(true);
              fieldType.setStoreTermVectors(true);
              fieldType.setStoreTermVectorPositions(true);
              fieldType.freeze();
              JsonObject jsonObject = jsonElement.getAsJsonObject();
              
              doc.add(new Field("text", jsonObject.get("token").getAsString(), fieldType));
              doc.add(new StoredField("id", jsonObject.get("id").getAsString()));
              doc.add(new StoredField("user", jsonObject.get("author").getAsString()));
              String publicationTime = jsonObject.get("publicationTime").getAsString();
              doc.add(new StoredField("time_str", publicationTime));
              Long publicationTimeLong;
              Long publicationTimeLong;
              if (publicationTime.contains("~")) {
                publicationTimeLong = Long.valueOf(publicationTime.indexOf("~", 1) > 0 ? Long.parseLong(publicationTime.substring(1, publicationTime.indexOf("~", 1))) : Long.parseLong(publicationTime.substring(1)));
              } else {
                publicationTimeLong = Long.valueOf(Long.parseLong(publicationTime));
              }
              doc.add(new LongField("time", publicationTimeLong.longValue(), Field.Store.NO));
              writer.addDocument(doc);
            }
            br.close();
            fr.close();
            
            cont++;
            filesIndex.add(jsonFile);
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
          }
        }
      }
      writer.commit();
      return true;
    }
    catch (JsonSyntaxException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static boolean CreateIndexTimeAggregationCollectionsInflucencersKeywords()
  {
    JsonParser parser = new JsonParser();
    try
    {
      List<String> filesIndex = new ArrayList();
      String jsonFile = null;
      for (Date date : GlobalParameters.topicDates)
      {
        Date currentDate = date;
        int cont = 0;
        while (cont <= GlobalParameters.dfIdf)
        {
          jsonFile = Utils.GetJsonFile(currentDate);
          if (filesIndex.contains(jsonFile))
          {
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
            cont++;
          }
          else
          {
            FileReader fr = new FileReader(jsonFile);
            BufferedReader br = new BufferedReader(fr);
            
            String line = null;
            while ((line = br.readLine()) != null)
            {
              JsonElement jsonElement = parser.parse(line);
              Document doc = new Document();
              FieldType fieldType = new FieldType();
              fieldType.setIndexed(true);
              fieldType.setStored(true);
              fieldType.setTokenized(true);
              fieldType.setStoreTermVectors(true);
              fieldType.setStoreTermVectorPositions(true);
              fieldType.freeze();
              JsonObject jsonObject = jsonElement.getAsJsonObject();
              
              doc.add(new Field("text", jsonObject.get("title").getAsString(), fieldType));
              doc.add(new StoredField("id", jsonObject.get("id").getAsString()));
              doc.add(new StoredField("user", jsonObject.get("author").getAsString()));
              doc.add(new LongField("time", jsonObject.get("publicationTime").getAsLong(), Field.Store.NO));
              writer.addDocument(doc);
            }
            br.close();
            fr.close();
            
            cont++;
            filesIndex.add(jsonFile);
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
          }
        }
      }
      writer.commit();
      return true;
    }
    catch (JsonSyntaxException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static boolean CreateIndexTimeAggregationCollectionsInfluencersUsers()
  {
    JsonParser parser = new JsonParser();
    
    List<String> filenames = new ArrayList();
    filenames.add(GlobalParameters.targetFilename);
    filenames.addAll(GlobalParameters.prevFilenames);
    try
    {
      List<String> filesIndex = new ArrayList();
      for (String jsonFile : filenames)
      {
        int cont = 0;
        
        FileReader fr = new FileReader(GlobalParameters.database + jsonFile);
        BufferedReader br = new BufferedReader(fr);
        
        String line = null;
        while ((line = br.readLine()) != null)
        {
          JsonElement jsonElement = parser.parse(line);
          Document doc = new Document();
          FieldType fieldType = new FieldType();
          fieldType.setIndexed(true);
          fieldType.setStored(true);
          fieldType.setTokenized(true);
          fieldType.setStoreTermVectors(true);
          fieldType.setStoreTermVectorPositions(true);
          fieldType.freeze();
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          
          doc.add(new Field("text", jsonObject.get("text").getAsString(), fieldType));
          doc.add(new StoredField("id", jsonObject.get("id_str").getAsString()));
          doc.add(new StoredField("user", ((JsonObject)jsonObject.get("user")).get("screen_name").getAsString()));
          doc.add(new StoredField("time_str", jsonObject.get("created_at").getAsString()));
          try
          {
            Long ll = parseTwitterDate(jsonObject.get("created_at").getAsString());
            
            doc.add(new LongField("time", ll.longValue(), Field.Store.NO));
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          writer.addDocument(doc);
        }
        br.close();
        fr.close();
        
        cont++;
        filesIndex.add(jsonFile);
      }
      writer.commit();
      return true;
    }
    catch (JsonSyntaxException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static boolean CreateIndexTimeAggregationCollectionsInfluencersUsersOld()
  {
    JsonParser parser = new JsonParser();
    try
    {
      List<String> filesIndex = new ArrayList();
      String jsonFile = null;
      for (Date date : GlobalParameters.topicDates)
      {
        Date currentDate = date;
        int cont = 0;
        while (cont <= GlobalParameters.dfIdf)
        {
          jsonFile = Utils.GetJsonFile(currentDate);
          if (filesIndex.contains(jsonFile))
          {
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
            cont++;
          }
          else
          {
            FileReader fr = new FileReader(jsonFile);
            BufferedReader br = new BufferedReader(fr);
            
            String line = null;
            while ((line = br.readLine()) != null)
            {
              JsonElement jsonElement = parser.parse(line);
              Document doc = new Document();
              FieldType fieldType = new FieldType();
              fieldType.setIndexed(true);
              fieldType.setStored(true);
              fieldType.setTokenized(true);
              fieldType.setStoreTermVectors(true);
              fieldType.setStoreTermVectorPositions(true);
              fieldType.freeze();
              JsonObject jsonObject = jsonElement.getAsJsonObject();
              
              doc.add(new Field("text", jsonObject.get("text").getAsString(), fieldType));
              doc.add(new StoredField("id", jsonObject.get("id_str").getAsString()));
              doc.add(new StoredField("user", ((JsonObject)jsonObject.get("user")).get("id_str").getAsString()));
              doc.add(new StoredField("time_str", jsonObject.get("created_at").getAsString()));
              try
              {
                doc.add(new LongField("time", new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy").parse(jsonObject.get("created_at").getAsString()).getTime(), Field.Store.NO));
              }
              catch (ParseException e)
              {
                e.printStackTrace();
              }
              writer.addDocument(doc);
            }
            br.close();
            fr.close();
            
            cont++;
            filesIndex.add(jsonFile);
            currentDate = Utils.previousTime(currentDate, GlobalParameters.period);
          }
        }
      }
      writer.commit();
      return true;
    }
    catch (JsonSyntaxException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (NumberFormatException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public static void CloseIndex()
  {
    try
    {
      if (writer != null) {
        writer.close();
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
}
