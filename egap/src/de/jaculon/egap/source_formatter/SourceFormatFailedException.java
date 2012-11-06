package de.jaculon.egap.source_formatter;

public class SourceFormatFailedException extends RuntimeException {

	private static final long serialVersionUID = 476055240795527086L;

	public SourceFormatFailedException(String message) {
		super(message);
	}

	public SourceFormatFailedException() {
		super();
	}

	public SourceFormatFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
