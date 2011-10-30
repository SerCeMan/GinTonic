package egap.guice.statements;

import egap.guice.GuiceModule;
import egap.guice.annotations.GuiceAnnotation;

/**
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bindConstant().
 *  annotatedWith(SoftwareVersion.class).
 *   to(&quot;1.1.0&quot;);
 * </code>
 * </pre>
 * 
 * @author tmajunke
 * 
 */
public class ConstantBindingStatement extends LinkedBindingStatement {
	private static final long serialVersionUID = 7297763244222135900L;
}
