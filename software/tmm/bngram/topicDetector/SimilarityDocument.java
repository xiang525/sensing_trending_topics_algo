package tmm.bngram.topicDetector;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.math3.linear.SparseRealVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class SimilarityDocument
{
  private static IndexReader reader;
  private static Map<String, Integer> terms;
  
  public static void SetIndexReader(Map<String, Integer> termsSlot, IndexReader readerSlot)
    throws IOException
  {
    terms = termsSlot;
    reader = readerSlot;
  }
  
  public static double GetSimilarity(PartialTweet t1, PartialTweet t2)
    throws IOException
  {
    DocVector docVec1 = getDocumentVector(t1);
    DocVector docVec2 = getDocumentVector(t2);
    return getCosineSimilarity(docVec1, docVec2);
  }
  
  private static DocVector getDocumentVector(PartialTweet t)
    throws IOException
  {
    Terms tfvs = reader.getTermVector(t.getIdIndex(), "text");
    DocVector doc1 = new DocVector(terms.size());
    TermsEnum termsEnum = tfvs.iterator(null);
    
    BytesRef byteRef = null;
    while ((byteRef = termsEnum.next()) != null) {
      doc1.setEntry(new String(byteRef.bytes, byteRef.offset, byteRef.length), termsEnum.totalTermFreq(), terms);
    }
    doc1.normalize();
    return doc1;
  }
  
  private static float getCosineSimilarity(DocVector d1, DocVector d2)
  {
    try
    {
      return (float)((float)d1.getVector().dotProduct(d2.getVector()) / (d1.getVector().getNorm() * d2.getVector().getNorm()));
    }
    catch (Exception e) {}
    return 0.0F;
  }
}
