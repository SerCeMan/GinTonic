package de.jaculon.egap.templates;

public class InstallModuleTemplate
{
  protected static String nl;
  public static synchronized InstallModuleTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    InstallModuleTemplate result = new InstallModuleTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = NL + "install(new ";
  protected final String TEXT_2 = "());";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     String guiceModuleToInstall = (String) argument; 
    stringBuffer.append(TEXT_1);
    stringBuffer.append( guiceModuleToInstall );
    stringBuffer.append(TEXT_2);
    return stringBuffer.toString();
  }
}
