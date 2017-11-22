package tmm.bngram.topicDetector;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.lucene.util.Version;

public final class TweetTokenizer
  extends Tokenizer
{
  private final TweetTokenizerImpl scanner;
  public static final int ALPHANUM = 0;
  public static final int APOSTROPHE = 1;
  public static final int ACRONYM = 2;
  public static final int COMPANY = 3;
  public static final int EMAIL = 4;
  public static final int HOST = 5;
  public static final int NUM = 6;
  public static final int CJ = 7;
  public static final int URL = 8;
  public static final int HASHTAG = 9;
  public static final int MENTION = 10;
  public static final int PERCENTAGE = 11;
  /**
   * @deprecated
   */
  public static final int ACRONYM_DEP = 12;
  public static final String[] TOKEN_TYPES = { "<ALPHANUM>", "<APOSTROPHE>", "<ACRONYM>", "<COMPANY>", "<EMAIL>", "<HOST>", "<NUM>", "<CJ>", "<ACRONYM_DEP>", "<URL>", "<HASHTAG>", "<MENTION>", "<PERCENTAGE>" };
  private boolean replaceInvalidAcronym;
  private int maxTokenLength = 255;
  private LinkedList<AttributeSource.State> tokensList;
  private CharTermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private PositionIncrementAttribute posIncrAtt;
  private TypeAttribute typeAtt;
  
  public void setMaxTokenLength(int length)
  {
    this.maxTokenLength = length;
  }
  
  public int getMaxTokenLength()
  {
    return this.maxTokenLength;
  }
  
  public TweetTokenizer(Version matchVersion, Reader input)
  {
    super(input);
    this.scanner = new TweetTokenizerImpl(input);
    init(input, matchVersion);
  }
  
  public TweetTokenizer(Version matchVersion, AttributeSource source, Reader input)
  {
    super(source, input);
    
    this.scanner = new TweetTokenizerImpl(input);
    init(input, matchVersion);
  }
  
  public TweetTokenizer(Version matchVersion, AttributeSource.AttributeFactory factory, Reader input)
  {
    super(factory, input);
    this.scanner = new TweetTokenizerImpl(input);
    init(input, matchVersion);
  }
  
  private void init(Reader input, Version matchVersion)
  {
    if (matchVersion.onOrAfter(Version.LUCENE_40)) {
      this.replaceInvalidAcronym = true;
    } else {
      this.replaceInvalidAcronym = false;
    }
    this.input = input;
    this.termAtt = ((CharTermAttribute)addAttribute(CharTermAttribute.class));
    this.offsetAtt = ((OffsetAttribute)addAttribute(OffsetAttribute.class));
    this.posIncrAtt = ((PositionIncrementAttribute)addAttribute(PositionIncrementAttribute.class));
    this.typeAtt = ((TypeAttribute)addAttribute(TypeAttribute.class));
    this.tokensList = new LinkedList();
  }
  
  public final boolean incrementToken()
    throws IOException
  {
    clearAttributes();
    int posIncr = 1;
    if (this.tokensList.size() > 0)
    {
      restoreState((AttributeSource.State)this.tokensList.remove());
      return true;
    }
    for (;;)
    {
      int tokenType = this.scanner.getNextToken();
      if (tokenType == -1) {
        return false;
      }
      if (this.scanner.yylength() <= this.maxTokenLength)
      {
        this.posIncrAtt.setPositionIncrement(posIncr);
        this.scanner.getText(this.termAtt);
        int start = this.scanner.yychar();
        this.offsetAtt.setOffset(correctOffset(start), correctOffset(start + this.termAtt.length()));
        if (tokenType == 12)
        {
          if (this.replaceInvalidAcronym)
          {
            this.typeAtt.setType(TweetTokenizerImpl.TOKEN_TYPES[5]);
            this.termAtt.setLength(this.termAtt.length() - 1);
          }
          else
          {
            this.typeAtt.setType(TweetTokenizerImpl.TOKEN_TYPES[2]);
          }
        }
        else {
          this.typeAtt.setType(TweetTokenizerImpl.TOKEN_TYPES[tokenType]);
        }
        if (this.tokensList.size() > 0) {
          restoreState((AttributeSource.State)this.tokensList.remove());
        }
        return true;
      }
      posIncr++;
    }
  }
  
  public final void end()
  {
    int finalOffset = correctOffset(this.scanner.yychar() + this.scanner.yylength());
    this.offsetAtt.setOffset(finalOffset, finalOffset);
  }
  
  public void reset()
    throws IOException
  {
    super.reset();
    this.scanner.yyreset(this.input);
  }
  
  /**
   * @deprecated
   */
  public boolean isReplaceInvalidAcronym()
  {
    return this.replaceInvalidAcronym;
  }
  
  /**
   * @deprecated
   */
  public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym)
  {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
  }
}
