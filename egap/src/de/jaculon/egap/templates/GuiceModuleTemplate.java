package de.jaculon.egap.templates;

public class GuiceModuleTemplate
{
  protected static String nl;
  public static synchronized GuiceModuleTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    GuiceModuleTemplate result = new GuiceModuleTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "import com.google.inject.AbstractModule;" + NL;
  protected final String TEXT_2 = NL + NL + "public class ";
  protected final String TEXT_3 = " extends AbstractModule {" + NL + "\t@Override" + NL + "\tprotected void configure() {" + NL + "\t\t" + NL + "\t}" + NL + "}";
  protected final String TEXT_4 = NL;

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
     String moduleName = (String) argument; 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( moduleName );
    stringBuffer.append(TEXT_3);
    stringBuffer.append(TEXT_4);
    return stringBuffer.toString();
  }
}
