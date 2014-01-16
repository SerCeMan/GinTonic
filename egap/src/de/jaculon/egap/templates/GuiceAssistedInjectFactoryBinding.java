package de.jaculon.egap.templates;

import de.jaculon.egap.quickfix.assisted_inject.ProposalCreateBindingForAssistedFactory;

public class GuiceAssistedInjectFactoryBinding
{
  protected static String nl;
  public static synchronized GuiceAssistedInjectFactoryBinding create(String lineSeparator)
  {
    nl = lineSeparator;
    GuiceAssistedInjectFactoryBinding result = new GuiceAssistedInjectFactoryBinding();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = NL;
  protected final String TEXT_2 = NL + NL + "bind(";
  protected final String TEXT_3 = ".class).toProvider(" + NL + "\t\t\t\tFactoryProvider.newFactory(" + NL + "\t\t\t\t\t\t";
  protected final String TEXT_4 = ".class," + NL + "\t\t\t\t\t\t";
  protected final String TEXT_5 = ".class));";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
     ProposalCreateBindingForAssistedFactory proposal = (ProposalCreateBindingForAssistedFactory) argument; 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( proposal.getFactoryTypeName() );
    stringBuffer.append(TEXT_3);
    stringBuffer.append( proposal.getFactoryTypeName());
    stringBuffer.append(TEXT_4);
    stringBuffer.append( proposal.getModelTypeName()  );
    stringBuffer.append(TEXT_5);
    return stringBuffer.toString();
  }
}
