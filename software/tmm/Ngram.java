package tmm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.index.Term;

public class Ngram
{
  private Term term;
  private Float score;
  
  public Ngram()
  {
    this.score = Float.valueOf(0.0F);
  }
  
  public Ngram(Term term, Float score)
  {
    this.term = term;
    this.score = score;
  }
  
  public Term getTerm()
  {
    return this.term;
  }
  
  public void setTerm(Term term)
  {
    this.term = term;
  }
  
  public Float getScore()
  {
    return this.score;
  }
  
  public void setScore(Float score)
  {
    this.score = score;
  }
  
  public List<String> GetNgramTextNoSpaceCapitalize()
  {
    List<String> termList = new ArrayList();
    String[] terms = this.term.text().split(" ");
    Pattern pattern = Pattern.compile("^[@#]?(\\w+)$");
    for (int i = 0; i < terms.length; i++) {
      if (!terms[i].equals("_"))
      {
        Matcher matcher = pattern.matcher(terms[i]);
        if (matcher.find()) {
          termList.add(Character.toUpperCase(matcher.group(1).charAt(0)) + matcher.group(1).substring(1));
        }
      }
    }
    return termList;
  }
  
  public boolean equals(Object ngram)
  {
    if (this == ngram) {
      return true;
    }
    if (!(ngram instanceof Ngram)) {
      return false;
    }
    Ngram ngram1 = (Ngram)ngram;
    
    return ngram1.getTerm().compareTo(getTerm()) == 0;
  }
  
  public int hashCode()
  {
    int result = 17;
    
    result = 31 * result + (this.term != null ? this.term.hashCode() : 0);
    return result;
  }
  
  public String toString()
  {
    return "Ngram [term=" + this.term + ", score=" + this.score.floatValue() + "]";
  }
}
