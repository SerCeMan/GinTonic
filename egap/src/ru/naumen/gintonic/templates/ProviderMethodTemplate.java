package ru.naumen.gintonic.templates;

import java.util.*;

import ru.naumen.gintonic.utils.StringUtils;

public class ProviderMethodTemplate
{
  protected static String nl;
  public static synchronized ProviderMethodTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    ProviderMethodTemplate result = new ProviderMethodTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = " " + NL + "" + NL + "@SuppressWarnings(\"unused\")" + NL + "@Provides";
  protected final String TEXT_2 = NL;
  protected final String TEXT_3 = "  " + NL + "private ";
  protected final String TEXT_4 = " provide";
  protected final String TEXT_5 = "(){" + NL + "\t";
  protected final String TEXT_6 = " ";
  protected final String TEXT_7 = " = new ";
  protected final String TEXT_8 = "();" + NL + "\treturn ";
  protected final String TEXT_9 = ";" + NL + "} ";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     Map<String, String> map = (Map<String, String>) argument; 
    stringBuffer.append(TEXT_1);
    stringBuffer.append(TEXT_2);
    stringBuffer.append( map.get("annotation") == null ? "" : map.get("annotation") );
    stringBuffer.append(TEXT_3);
    stringBuffer.append( map.get("type") );
    stringBuffer.append(TEXT_4);
    stringBuffer.append( StringUtils.capitalize(map.get("variablename")) );
    stringBuffer.append(TEXT_5);
    stringBuffer.append( map.get("type") );
    stringBuffer.append(TEXT_6);
    stringBuffer.append( map.get("variablename") );
    stringBuffer.append(TEXT_7);
    stringBuffer.append( map.get("type") );
    stringBuffer.append(TEXT_8);
    stringBuffer.append( map.get("variablename") );
    stringBuffer.append(TEXT_9);
    return stringBuffer.toString();
  }
}
