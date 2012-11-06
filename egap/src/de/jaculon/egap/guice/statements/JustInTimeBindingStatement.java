package de.jaculon.egap.guice.statements;



/**
 * @see http://code.google.com/p/google-guice/wiki/JustInTimeBindings
 *
 * @author tmajunke
 */
public class JustInTimeBindingStatement extends GuiceStatement{

	private static final long serialVersionUID = 4724240897474687500L;

	@Override
	public String getLabel() {
		return "Implicit binding, goto to " + getTypeName();
	}

}
