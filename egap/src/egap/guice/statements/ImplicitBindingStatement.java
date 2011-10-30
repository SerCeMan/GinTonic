package egap.guice.statements;



/**
 * @author tmajunke
 */
public class ImplicitBindingStatement extends GuiceStatement{

	private static final long serialVersionUID = 4724240897474687500L;

	@Override
	public String getLabel() {
		return "Implicit binding, goto to " + getTypeName();
	}
	
}
