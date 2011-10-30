package egap.guice.annotations;

import egap.utils.StringUtils;

/**
 * 
 * The String Literal of a named expression (e.g for the expression
 * Names.named("jack") "jack" is the String literal. Can be null.
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
	private String name;

	public GuiceNamedAnnotation(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String getTypeToImport() {
		return StringUtils.GUICE_NAMED;
	}

}
