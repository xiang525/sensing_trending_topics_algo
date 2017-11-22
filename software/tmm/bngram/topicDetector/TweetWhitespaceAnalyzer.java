package tmm.bngram.topicDetector;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class TweetWhitespaceAnalyzer
  extends Analyzer
{
  private Version version;
  
  public TweetWhitespaceAnalyzer(Version matchVersion)
  {
    this.version = matchVersion;
  }
  
  protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader)
  {
    Tokenizer source = new WhitespaceTokenizer(this.version, reader);
    
    TokenStream result = new LowerCaseFilter(this.version, source);
    
    String[] c = { "." };
    Set<String> sw = new HashSet(Arrays.asList(c));
    
    CharArraySet stopwords = new CharArraySet(this.version, sw, true);
    
    result = new StopFilter(this.version, result, stopwords);
    if (GlobalParameters.ngrams > 1)
    {
      ShingleFilter sF = new ShingleFilter(result, 2, GlobalParameters.ngrams);
      sF.setOutputUnigrams(true);
      result = sF;
    }
    return new Analyzer.TokenStreamComponents(source, result);
  }
}
