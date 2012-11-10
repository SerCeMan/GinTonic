package de.jaculon.egap.junit;

import java.util.List;

import de.jaculon.egap.helper.ICompilationUnitHelper;
import de.jaculon.egap.helper.IProjectHelper;

public class JUnitTestResolver extends ICompilationUnitResolver {

	public JUnitTestResolver(List<MySourceFolder> srcFoldersToLookInto,
			List<String> testSuffixes,
			List<MyPackage> testPackagePrefixes) {
		setiCompilationUnitHelper(new ICompilationUnitHelper());
		setOpenJavaProjectsResolver(new IProjectHelper());
		setMyPackageHelper(new MyPackageHelper());

		setiTypeNameResolver(new AppendSuffixTypeNameResolver(testSuffixes));

		setFoldersToLookForMatchingCompilationUnit(srcFoldersToLookInto);

		setPackageResolver(new ClassUnderTestPackageResolver(
				testPackagePrefixes));

	}

}
