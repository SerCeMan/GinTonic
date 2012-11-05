package egap.guice.annotations;

import egap.utils.StringUtils;

/**
 *
 * A @Named annotation:
 *
 * <pre>
 * <code>
 * @Inject
 * @Named("jack") private IPianoPlayer<Bar> jackThePianoPlayer;
 * </code>
 * </pre>
 *
 * @author tmajunke
 */
public class GuiceNamedAnnotation extends GuiceAnnotation {

	private static final long serialVersionUID = -877849429883614596L;
	private String literal;

	public GuiceNamedAnnotation(String literal) {
		super();
		this.literal = literal;
	}

	/**
	 * Returns the literal value for the named annotation e.g for the expression
	 * Names.named("jack") "jack" is the String literal.
	 *
	 * <pre>
	 * <code>
	 * @Inject
	 * @Named("jack") private IPianoPlayer<Bar> jackThePianoPlayer;
	 * </code>
	 * </pre>
	 */
	public String getLiteral() {
		return literal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((literal == null) ? 0 : literal.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuiceNamedAnnotation other = (GuiceNamedAnnotation) obj;
		if (literal == null) {
			if (other.literal != null)
				return false;
		}
		else if (!literal.equals(other.literal))
			return false;
		return true;
	}

	@Override
	public String getTypeToImport() {
		return StringUtils.GUICE_NAMED;
	}

}
