package egap.guice.statements;

/**
 * A linked binding statement binds an interface to an implementation.
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
public class LinkedBindingStatement extends BindingDefinition {

	private static final long serialVersionUID = -1063810791156265429L;

	private String implType;

	/**
	 * Returns the fully qualified name of the implementation type that the
	 * interface is bound to.
	 */
	public String getImplType() {
		return implType;
	}

	public void setImplType(String implType) {
		this.implType = implType;
	}

}
