package egap.guice.statements;

import egap.guice.GuiceModule;
import egap.guice.annotations.GuiceAnnotation;

/**
 * 
 * <h5>Example:</h5>
 * 
 * <pre>
 * <code>
 * bind(Date.class).
 *   annotatedWith(TimeBarCloses.class).toInstance(
 * 	 new Date(0, 0, 0, 11, 0, 0));
 * </code>
 * </pre>
 * 
 * @author tmajunke
 */
public class InstanceBindingStatement extends LinkedBindingStatement {
	private static final long serialVersionUID = -1063810791156265429L;
}
