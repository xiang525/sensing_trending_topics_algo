package tmm;

import java.util.Properties;

public class ParameterUtilities
{
  public static String readProperty(String propertyName, Properties props)
  {
    String property = props.getProperty(propertyName).trim();
    if (property == null) {
      throw new IllegalStateException("Property " + propertyName + " has not been defined");
    }
    return property;
  }
}
