package tmm.bngram.topicDetector;

import java.util.Map;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVectorFormat;
import org.apache.commons.math3.linear.SparseRealVector;

public class DocVector
{
  private SparseRealVector vector;
  
  public DocVector(int size)
  {
    this.vector = new OpenMapRealVector(size);
  }
  
  public void setEntry(String term, long freq, Map<String, Integer> terms)
  {
    if (terms.containsKey(term))
    {
      int pos = ((Integer)terms.get(term)).intValue();
      this.vector.setEntry(pos, freq);
    }
  }
  
  public void normalize()
  {
    double sum = this.vector.getL1Norm();
    this.vector = ((SparseRealVector)this.vector.mapDivide(sum));
  }
  
  public String toString()
  {
    RealVectorFormat formatter = new RealVectorFormat();
    return formatter.format(this.vector);
  }
  
  public SparseRealVector getVector()
  {
    return this.vector;
  }
}
