package tmm.bngram.topicDetector;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import java.io.File;
import java.io.PrintStream;
import java.util.Date;

public class Database
{
  private static Mongo m;
  private static DB db;
  private static DBCollection coll;
  private static String name;
  
  public static void Connect(String address, int port, String n)
    throws MongoException, Exception
  {
    m = new Mongo(address, port);
    name = n;
    if (m != null) {
      db = m.getDB(name);
    }
  }
  
  public static void Disconnect()
  {
    if (m != null) {
      m.close();
    }
  }
  
  public static DBCursor ExecuteQuery(BasicDBObject query)
  {
    if (query == null) {
      return null;
    }
    return coll.find(query).snapshot();
  }
  
  public static void GetIndex()
    throws MongoException, Exception
  {
    Connect(GlobalParameters.server, GlobalParameters.port, GlobalParameters.database);
    UseCollection(GlobalParameters.collection);
    ProcessQueriesText();
    Disconnect();
  }
  
  private static void ProcessQueriesText()
  {
    if ((GlobalParameters.startTime == null) || (GlobalParameters.stopTime == null) || (GlobalParameters.startTime.compareTo(GlobalParameters.stopTime) >= 0)) {
      return;
    }
    BasicDBObject query = new BasicDBObject();
    System.out.println("Time: " + GlobalParameters.startTime.toString());
    
    File f = new File(Utils.GetIndexFolder());
    if (f.exists()) {
      return;
    }
    query.append("publicationTime", new BasicDBObject("$gt", Long.valueOf(GlobalParameters.startTime.getTime())).append("$lte", Long.valueOf(GlobalParameters.stopTime.getTime())));
    
    DBCursor cur = ExecuteQuery(query);
    TextFileIndexer.SetupIndex();
    if (!TextFileIndexer.CreateIndex(cur))
    {
      System.err.println("Index file not created...");
      return;
    }
    TextFileIndexer.CloseIndex();
  }
  
  public static void GetIndexJson()
    throws NumberFormatException, ArrayIndexOutOfBoundsException
  {
    File f = new File(Utils.GetIndexFolder());
    if (f.exists()) {
      f.mkdirs();
    }
    TextFileIndexer.SetupIndex();
    if (!TextFileIndexer.CreateIndexTimeAggregationCollectionsInfluencersUsers())
    {
      System.err.println("Indexes files not created...");
      return;
    }
    TextFileIndexer.CloseIndex();
  }
  
  public static void UseCollection(String name)
  {
    coll = db.getCollection(name);
  }
}
