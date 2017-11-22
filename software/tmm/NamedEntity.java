package tmm;

public class NamedEntity
{
  private String name;
  private NE_TYPE neType;
  private int pos;
  
  public static enum NE_TYPE
  {
    LOCATION,  PERSON,  ORGANIZATION;
    
    private NE_TYPE() {}
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public NE_TYPE getNeType()
  {
    return this.neType;
  }
  
  public void setNeType(NE_TYPE neType)
  {
    this.neType = neType;
  }
  
  public int getPos()
  {
    return this.pos;
  }
  
  public void setPos(int pos)
  {
    this.pos = pos;
  }
}
