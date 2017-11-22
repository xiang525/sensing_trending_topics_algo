package tmm.bngram.topicMatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.linear.SparseRealVector;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public class SimilarityDocument
{
  private static Map<String, Integer> terms;
  
  public static double GetSimilarity(Terms t1, Terms t2)
    throws IOException
  {
    terms = new HashMap();
    int pos = 0;
    TermsEnum termsEnum1 = t1.iterator(null);
    TermsEnum termsEnum2 = t2.iterator(null);
    BytesRef byteRef = null;
    while ((byteRef = termsEnum1.next()) != null)
    {
      terms.put(new String(byteRef.bytes, byteRef.offset, byteRef.length), Integer.valueOf(pos));
      pos++;
    }
    while ((byteRef = termsEnum2.next()) != null)
    {
      String key = new String(byteRef.bytes, byteRef.offset, byteRef.length);
      if (!terms.containsKey(key))
      {
        terms.put(key, Integer.valueOf(pos));
        pos++;
      }
    }
    DocVector docVec1 = getDocumentVector(t1);
    DocVector docVec2 = getDocumentVector(t2);
    return getCosineSimilarity(docVec1, docVec2);
  }
  
  private static DocVector getDocumentVector(Terms tfvs)
    throws IOException
  {
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
