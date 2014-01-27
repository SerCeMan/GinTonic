package ru.naumen.gintonic.utils;

import org.eclipse.core.runtime.IPath;

public class IPathUtils {

	public static String getRelativePathToJavaClasspathString(
			IPath projectRelativePath) {
		IPath removeFirstSegments = projectRelativePath.removeFirstSegments(1);
		String pathFileLike = removeFirstSegments.toString();
		String pathValidClasspath = StringUtils.pathToJavaClasspath(pathFileLike);
		return pathValidClasspath;
	}

}
