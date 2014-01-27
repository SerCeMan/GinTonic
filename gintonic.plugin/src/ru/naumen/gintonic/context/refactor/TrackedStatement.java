package ru.naumen.gintonic.context.refactor;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

public class TrackedStatement {

	private final Statement statement;
	private final ITrackedNodePosition track;

	public TrackedStatement(Statement statement,
			ITrackedNodePosition track) {
				this.statement = statement;
				this.track = track;
	}

	public Statement getStatement() {
		return statement;
	}

	public int getStartPosition() {
		return track.getStartPosition();
	}

	public int getLength() {
		return track.getLength();
	}

}
