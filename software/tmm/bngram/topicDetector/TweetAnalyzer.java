package tmm.bngram.topicDetector;

import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

public class TweetAnalyzer
  extends StopwordAnalyzerBase
{
  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
  
  public TweetAnalyzer(Version matchVersion, Set<?> stopwords)
  {
    super(matchVersion, new CharArraySet(Version.LUCENE_40, stopwords, true));
  }
  
  public TweetAnalyzer(Version matchVersion)
  {
    this(matchVersion, StandardAnalyzer.STOP_WORDS_SET);
  }
  
  protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader)
  {
    Tokenizer source = new TweetTokenizer(this.matchVersion, reader);
    
    TokenStream result = new LowerCaseFilter(this.matchVersion, source);
    
    result = new StopFilter(this.matchVersion, result, this.stopwords);
    if (GlobalParameters.ngrams > 1)
    {
      ShingleFilter sF = new ShingleFilter(result, 2, GlobalParameters.ngrams);
      sF.setOutputUnigrams(true);
      result = sF;
    }
    return new Analyzer.TokenStreamComponents(source, result);
  }
}
