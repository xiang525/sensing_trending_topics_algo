package tmm.bngram.topicMatcher;

import java.io.IOException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import tmm.bngram.topicDetector.PartialTweet;
import tmm.bngram.topicDetector.Topic;

public class DyscoComparer
{
  private static final String FIELD_TEXT = "text";
  
  private static String GetBagWords(Topic topic)
  {
    StringBuilder termsDysco1 = new StringBuilder();
    for (PartialTweet item : topic.getTweets()) {
      termsDysco1.append(item.getText() + "\n");
    }
    return termsDysco1.toString();
  }
  
  public static double CompareDyscos(Topic topic1, Topic topic2)
    throws CorruptIndexException, LockObtainFailedException, IOException
  {
    String bagWords1 = GetBagWords(topic1);
    String bagWords2 = GetBagWords(topic2);
    if ((bagWords1 == null) || (bagWords2 == null)) {
      return 0.0D;
    }
    LuceneRAMIndex.SetUpIndex();
    if (!LuceneRAMIndex.CreateIndex(bagWords1, bagWords2)) {
      return 0.0D;
    }
    IndexReader reader = DirectoryReader.open(LuceneRAMIndex.getIndex());
    ScoreDoc[] hits = GetIndexDocs(reader);
    double similarity = SimilarityDocument.GetSimilarity(reader.getTermVector(hits[0].doc, "text"), reader.getTermVector(hits[1].doc, "text"));
    reader.close();
    LuceneRAMIndex.CloseIndex();
    return similarity;
  }
  
  private static ScoreDoc[] GetIndexDocs(IndexReader reader)
    throws IOException
  {
    Query query = new MatchAllDocsQuery();
    IndexSearcher searcher = new IndexSearcher(reader);
    TopDocs topDocs = searcher.search(query, reader.maxDoc());
    return topDocs.scoreDocs;
  }
}
