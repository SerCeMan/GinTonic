package de.jaculon.egap.guice.annotations;

import de.jaculon.egap.utils.StringUtils;

/**
 * 
 * <pre>
 * <code>
 * @Inject
 *    @TimeBarCloses
 *    private Date timeBarCloses;
 *    </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class GuiceClassAnnotation extends GuiceAnnotation {
	
	private static final long serialVersionUID = 9977899545760793L;
	private String annotationType;

	public GuiceClassAnnotation(String annotationType) {
		super();
		this.annotationType = annotationType;
	}

	public String getFullyQualifiedName() {
		return annotationType;
	}

	public String getName() {
		return StringUtils.getSimpleName(annotationType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((annotationType == null) ? 0 : annotationType.hashCode());
		return result;
	}
	
	@Override
	public String getTypeToImport() {
		return annotationType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuiceClassAnnotation other = (GuiceClassAnnotation) obj;
		if (annotationType == null) {
			if (other.annotationType != null)
				return false;
		}
		else if (!annotationType.equals(other.annotationType))
			return false;
		return true;
	}
	
}
