package egap.source_formatter;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class JavaSourceFormatter {

	/**
	 * Formatiert den übergebenen Quelltext mit Eclipse's default CodeFormatter.
	 * 
	 * @param code der Quelltext
	 * @return der formatierte Quelltext
	 */
	public static String format(String code){

		@SuppressWarnings("unchecked")
		Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(
				JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

		final TextEdit edit = codeFormatter.format(
				CodeFormatter.K_UNKNOWN,
				code,
				0,
				code.length(),
				0,
				System.getProperty("line.separator")); //$NON-NLS-1$
		if (edit == null) {
			throw new SourceFormatFailedException("cannot format this: " + code);
		}

		IDocument document = new Document(code);
		
		try {
			edit.apply(document);
		} catch (Exception e) {
			throw new SourceFormatFailedException("cannot format this: " + code, e);
		}

		return document.get();
	}

}
