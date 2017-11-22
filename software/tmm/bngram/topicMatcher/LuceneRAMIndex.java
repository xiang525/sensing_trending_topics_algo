package tmm.bngram.topicMatcher;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import tmm.bngram.topicDetector.TweetWhitespaceAnalyzer;

public class LuceneRAMIndex
{
  private static RAMDirectory directory;
  private static IndexWriter writer;
  
  public static void SetUpIndex()
    throws CorruptIndexException, LockObtainFailedException, IOException
  {
    directory = new RAMDirectory();
    
    TweetWhitespaceAnalyzer analyzer = new TweetWhitespaceAnalyzer(Version.LUCENE_40);
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
    writer = new IndexWriter(directory, config);
  }
  
  public static boolean CreateIndex(String text1, String text2)
  {
    try
    {
      FieldType fieldType = new FieldType();
      fieldType.setIndexed(true);
      fieldType.setStored(true);
      fieldType.setTokenized(true);
      fieldType.setStoreTermVectors(true);
      fieldType.setStoreTermVectorPositions(true);
      fieldType.freeze();
      Document doc1 = new Document();
      doc1.add(new Field("text", text1, fieldType));
      writer.addDocument(doc1);
      Document doc2 = new Document();
      doc2.add(new Field("text", text2, fieldType));
      writer.addDocument(doc2);
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
  
  public static RAMDirectory getIndex()
  {
    return directory;
  }
  
  public static void CloseIndex()
  {
    try
    {
      if (writer != null) {
        writer.close();
      }
      if (directory != null) {
        directory.close();
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
}
