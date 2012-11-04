package egap.guice.statements;

import egap.guice.annotations.GuiceAnnotation;
import egap.utils.StringUtils;

/**
 * @author tmajunke
 */
public class BindingDefinition extends GuiceStatement {

	private static final long serialVersionUID = -8478037964657180287L;

	private String boundType;

	private GuiceAnnotation guiceAnnotation;

	private String scopeType;

	private boolean isEagerSingleton;

	public void setBoundType(String boundType) {
		this.boundType = boundType;
	}

	/**
	 * Returns the bound type as fully qualified name. The bound type is the
	 * type you declare with &#64;Inject.
	 */
	public String getBoundType() {
		return boundType;
	}

	public void setGuiceAnnotation(GuiceAnnotation guiceAnnotation) {
		this.guiceAnnotation = guiceAnnotation;
	}

	public boolean isEagerSingleton() {
		return isEagerSingleton;
	}

	public void setEagerSingleton(boolean isEagerSingleton) {
		this.isEagerSingleton = isEagerSingleton;
	}

	/**
	 * Returns the fully qualified scope type or null if there is no scope.
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * Returns "com.google.inject.Singleton" for Scopes.SINGLETON.
	 * </pre>
	 */
	public String getScopeType() {
		return scopeType;
	}

	public void setScopeType(String scopeType) {
		this.scopeType = scopeType;
	}

	/**
	 * Returns the simple name of the scope type or null if there is no scope.
	 */
	public String getScopeTypeSimpleName() {
		return StringUtils.getSimpleName(scopeType);
	}

	/**
	 * Returns the annotation of the binding definition or null if there is no.
	 */
	public GuiceAnnotation getGuiceAnnotation() {
		return guiceAnnotation;
	}

	/**
	 * Delivers the label which is displayed in the quickfix.
	 */
	@Override
	public String getLabel() {
		String guiceModuleName = getTypeName();
		String label = "Goto binding in '" + guiceModuleName + "'";
		String scopeTypeAsString = getScopeTypeSimpleName();

		if (scopeType == null) {
			scopeTypeAsString = "Unscoped";
		}

		label = label + " (Scope:" + scopeTypeAsString + ")";
		return label;
	}

}
