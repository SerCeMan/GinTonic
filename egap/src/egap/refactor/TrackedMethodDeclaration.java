package egap.refactor;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

public class TrackedMethodDeclaration{

	private final MethodDeclaration methodDeclaration;
	private final ITrackedNodePosition track;

	public TrackedMethodDeclaration(MethodDeclaration methodDeclaration,
			ITrackedNodePosition track) {
				this.methodDeclaration = methodDeclaration;
				this.track = track;
	}
	
	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public int getStartPosition() {
		return track.getStartPosition();
	}

	public int getLength() {
		return track.getLength();
	}

}
