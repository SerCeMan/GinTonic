package de.jaculon.egap.junit;

import java.util.List;

public class ClassUnderTestResolver extends ICompilationUnitResolver {

	public ClassUnderTestResolver(List<MySourceFolder> srcFolders,
			List<String> testSuffixes,
			List<MyPackage> testPackagePrefixes) {
		setiCompilationUnitHelper(new ICompilationUnitHelper());
		setiTypeNameResolver(new ClassUnderTestTypeNameResolver(testSuffixes));
		setFoldersToLookForMatchingCompilationUnit(srcFolders);
		setOpenJavaProjectsResolver(new OpenJavaProjectsResolver());
		setPackageResolver(new ClassUnderTestPackageResolver(
				testPackagePrefixes));
		setiPackageFragmentHelper(new MyPackageFragmentHelper());
	}

}
