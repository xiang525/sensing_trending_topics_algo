package tmm.bngram.topicDetector;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

public class RepresentativeTermsThread
  implements Runnable
{
  private Date currentTime;
  private Date previousTime;
  private IndexReader reader;
  private IndexSearcher searcher;
  private SlotInformation[] slotInformation;
  private int index;
  private Map<String, Integer> termsSimilarity;
  private int pos;
  private static final String FIELD_TEXT = "text";
  
  public RepresentativeTermsThread(Date currentTime, Date previousTime, IndexReader reader, IndexSearcher searcher, SlotInformation[] slotInformation, int index)
  {
    this.currentTime = currentTime;
    this.previousTime = previousTime;
    this.reader = reader;
    this.searcher = searcher;
    this.slotInformation = slotInformation;
    this.index = index;
    this.termsSimilarity = new HashMap();
    this.pos = 0;
  }
  
  public void run()
  {
    this.slotInformation[this.index] = new SlotInformation();
    Query query = new MatchAllDocsQuery();
    FieldCacheRangeFilter<Long> filter = FieldCacheRangeFilter.newLongRange("time", Long.valueOf(this.previousTime.getTime()), Long.valueOf(this.currentTime.getTime()), true, false);
    try
    {
      TopDocs topDocs = this.searcher.search(query, filter, this.reader.maxDoc());
      ScoreDoc[] hits = topDocs.scoreDocs;
      System.out.println("Num. docs slot " + this.index + ": " + hits.length);
      this.slotInformation[this.index].setTotalTweets(topDocs.totalHits);
      for (ScoreDoc hit : hits)
      {
        Terms termVector = this.reader.getTermVector(hit.doc, "text");
        if (termVector != null)
        {
          TermsEnum termsEnum = termVector.iterator(null);
          BytesRef byteRef = null;
          while ((byteRef = termsEnum.next()) != null)
          {
            Term term = new Term("text", new String(byteRef.bytes, byteRef.offset, byteRef.length));
            this.slotInformation[this.index].AddNgramTweet(term, Integer.valueOf(hit.doc));
            if (this.index == 0) {
              GetTermsSimilarity(term.text());
            }
          }
        }
      }
      if (this.index == 0) {
        SimilarityDocument.SetIndexReader(this.termsSimilarity, this.reader);
      }
    }
    catch (IllegalArgumentException e)
    {
      System.err.println("No documents between: " + this.previousTime.toString() + " - " + this.currentTime.toString());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public void GetTermsSimilarity(String term)
  {
    if (this.termsSimilarity.containsKey(term)) {
      return;
    }
    this.termsSimilarity.put(term, Integer.valueOf(this.pos++));
  }
}
