package egap.guice.statements;


/**
 * A linked binding adds a type as receiver of the interface binding.
 * 
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bind(new TypeLiteral&lt;IPianoPlayer&lt;Bar&gt;&gt;() {
 * }).to(MaxThePianoPlayer.class);
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class LinkedBindingStatement extends BindingStatement {

	private static final long serialVersionUID = -1063810791156265429L;

	private String implType;

	public String getImplType() {
		return implType;
	}

	public void setImplType(String implType) {
		this.implType = implType;
	}

}
