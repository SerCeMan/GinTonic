package de.jaculon.egap.guice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.guice.statements.AssistedBindingStatement;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.guice.statements.InstallModuleStatement;
import de.jaculon.egap.guice.statements.JustInTimeBindingStatement;
import de.jaculon.egap.project_builder.EgapBuilder;
import de.jaculon.egap.utils.ICompilationUnitUtils;
import de.jaculon.egap.utils.IPackageFragmentUtils;
import de.jaculon.egap.utils.IProjectUtils;
import de.jaculon.egap.utils.ITypeBindingUtils;
import de.jaculon.egap.utils.ListUtils;
import de.jaculon.egap.utils.Preconditions;
import de.jaculon.egap.utils.SetUtils;
import de.jaculon.egap.utils.StringUtils;


/**
 * The guice index holds the informations about the guice modules as collected
 * during the build process ({@link EgapBuilder}), so they can be accessed
 * faster (e.g by the quickfixes).
 * 
 * @author tmajunke
 */
public class GuiceIndex implements Serializable {

	private static final long serialVersionUID = 2155643314847687772L;

	private static GuiceIndex instance;

	private BuildState buildState = BuildState.INITIAL;

	private boolean fromDisc;

	/**
	 * The guice modules.
	 */
	private ArrayList<GuiceModule> guiceModules = new ArrayList<GuiceModule>(100);

	private GuiceIndex() {
		super();
	}

	public static GuiceIndex get() {
		if (instance == null) {
			instance = new GuiceIndex();
			build();
		}
		return instance;
	}

	public static void set(GuiceIndex instance) {
		GuiceIndex.instance = instance;
	}

	public boolean isFromDisc() {
		return fromDisc;
	}

	public void setFromDisc(boolean fromDisc) {
		this.fromDisc = fromDisc;
	}

	public BuildState getBuildState() {
		return buildState;
	}

	public void setBuildState(BuildState buildState) {
		this.buildState = buildState;
	}

	private static Job build() {
		final List<IProject> projectsWithEgapNature = IProjectUtils.getOpenProjectsWithNature(EgapPlugin.ID_NATURE);

		/*
		 * The Job has versus the IWorkspaceRunnable approach the nose ahead as
		 * it delivers us with a IProgressMonitor so we can see the progress in
		 * the UI and maybe cancel it.
		 */
		Job job = new Job("Egap - Building Guice module index") {
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				for (IProject project : projectsWithEgapNature) {
					try {
						if (project.isOpen()) {
							IProject iProject = project.getProject();
							iProject.build(
									IncrementalProjectBuilder.FULL_BUILD,
									EgapBuilder.ID,
									null,
									progressMonitor);
						}
					} catch (CoreException e) {
						EgapPlugin.logException(e);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return job;
	}

	/**
	 * Returns the number of guice modules contained in this index.
	 */
	public int getNrOfGuiceModules() {
		return guiceModules.size();
	}

	/**
	 * Rebuilds the index by triggering an asynchronous project builder job.
	 */
	public static void rebuild() {
		set(null);
		get();
	}

	public void addGuiceModuleDontLog(GuiceModule guiceModule) {
		addGuiceModule(guiceModule, false);
	}

	public void addGuiceModule(GuiceModule guiceModule, boolean log) {

		Preconditions.checkNotNull(guiceModule.getTypeName());
		Preconditions.checkNotNull(guiceModule.getPackageFullyQualified());
		Preconditions.checkNotNull(guiceModule.getProjectName());

		guiceModules.add(guiceModule);
		if (log) {
			EgapPlugin.logInfo("Added "
					+ guiceModule.getTypeNameFullyQualified()
					+ " to Guice index " + getIndexInfoShort() + ".");
		}
	}
	
	/**
	 * Removes all modules of the given project from the index.
	 */
	public void removeGuiceModulesByProjectName(String projectName) {
		Preconditions.checkNotNull(projectName);
		List<GuiceModule> guiceModuleInfosToRemove = ListUtils.newArrayListWithCapacity(guiceModules.size());
		for (GuiceModule guiceModule : guiceModules) {
			String guiceModuleProjectName = guiceModule.getProjectName();
			if (guiceModuleProjectName.equals(projectName)) {
				guiceModuleInfosToRemove.add(guiceModule);
			}
		}
		for (GuiceModule guiceModule : guiceModuleInfosToRemove) {
			removeGuiceModule(guiceModule.getTypeNameFullyQualified(), false);
		}
	}
	
	public void removeGuiceModule(String nameFullyQualified, boolean log) {
		for (int i = 0; i < guiceModules.size(); i++) {
			GuiceModule guiceModule = guiceModules.get(i);
			String fullyQualifiedName = guiceModule.getTypeNameFullyQualified();
			if (fullyQualifiedName.equals(nameFullyQualified)) {
				GuiceModule removedModule = guiceModules.remove(i);
				if (log) {
					String message = nameFullyQualified + " from Guice index "
							+ getIndexInfoShort() + ".";
					if (removedModule == null) {
						message = "Tried but not succeeded in removing "
								+ message;
					}
					else {
						message = "Removed " + message;
					}
					EgapPlugin.logInfo(message);

				}
			}
		}
	}

	public void updateGuiceModule(GuiceModule guiceModule) {
		removeGuiceModule(guiceModule.getTypeNameFullyQualified(), false);
		addGuiceModule(guiceModule, false);
		EgapPlugin.logInfo("Updated " + guiceModule.getTypeNameFullyQualified()
				+ " from Guice index " + getIndexInfoShort() + ".");
	}

	/**
	 * Returns the guice modules of the given package and the parent packages as
	 * indicated by the parameter depth. Returns an empty list if no modules
	 * were found.
	 * 
	 * @param depth the number of parent packages to include (0 = the empty list
	 *            is returned, 1 means the modules in the given package, 2 means
	 *            the modules in the given package and the parent package, 3...
	 *            think you got it).
	 */
	public List<GuiceModule> getGuiceModulesInAndBelowPackage(
			IPackageBinding currentPackageBinding) {
		return getGuiceModulesInAndBelowPackage(currentPackageBinding, null, 2);
	}

	public List<GuiceModule> getGuiceModulesInAndBelowPackage(
			IPackageBinding currentPackageBinding, String ignoreModule,
			int depth) {
		IPackageFragment currentPackage = (IPackageFragment) currentPackageBinding.getJavaElement();
		List<IPackageFragment> parentPackages = IPackageFragmentUtils.getParentPackages(
				currentPackage,
				depth);
		parentPackages.add(currentPackage);
		List<GuiceModule> modules = getGuiceModulesInPackage(
				parentPackages,
				ignoreModule);
		return modules;
	}

	/**
	 * Returns the guice modules in the given packages. Returns an empty List if
	 * no modules were found.
	 */
	public List<GuiceModule> getGuiceModulesInPackage(
			List<IPackageFragment> packages, String moduleToIgnore) {
		List<GuiceModule> guiceModulesInGivenPackages = ListUtils.newArrayList();
		for (IPackageFragment packageFragment : packages) {
			if (packageFragment == null) {
				continue;
			}
			String packagePath = packageFragment.getElementName();

			for (GuiceModule guiceModule : guiceModules) {
				String packageFullyQualified = guiceModule.getPackageFullyQualified();
				boolean ignoreModule = guiceModule.getTypeNameFullyQualified().equals(
						moduleToIgnore);

				if (ignoreModule) {
					continue;
				}

				if (packageFullyQualified.equals(packagePath)) {
					guiceModulesInGivenPackages.add(guiceModule);
				}
			}
		}

		return guiceModulesInGivenPackages;
	}

	public List<GuiceStatement> getBindingsByType(ITypeBinding typeToFind) {

		String typeToFindQualifiedName = typeToFind.getQualifiedName();

		/*
		 * We only store the wrappers instead of primitives, so maybe we must
		 * replace the primitive type by the wrapper type.
		 */
		typeToFindQualifiedName = StringUtils.translatePrimitiveToWrapper(typeToFindQualifiedName);

		List<GuiceStatement> bindings = ListUtils.newArrayList();
		for (GuiceModule guiceModule : guiceModules) {

			List<BindingDefinition> bindingStatements = guiceModule.getBindingDefinitions();

			if (bindingStatements == null) {
				continue;
			}

			for (BindingDefinition bindingStatement : bindingStatements) {
				String bindingsBoundType = bindingStatement.getBoundType();
				if (bindingsBoundType.equals(typeToFindQualifiedName)) {
					bindings.add(bindingStatement);
				}
			}

		}

		return bindings;
	}

	/**
	 * Returns the {@link BindingDefinition}s for the given bound type and
	 * annotation type.
	 * 
	 * <h5>Just in time binding</h5>
	 * 
	 * If we could not find an explicit binding then we check the type. If it is
	 * a concrete class the we return a just in time binding. In all other cases we
	 * return null.
	 * 
	 * <h5>MapBinder Statements</h5>
	 * 
	 * If the type to find is a {@link Map}, then we also check if we can find a
	 * suitable MapBinder statement.
	 * 
	 * @param typeToFind the bound type. Cannot be null. Primitives (like "int")
	 *            values are allowed. Provider types are not allowed( e.g
	 *            instead of asking for "Provider&lt;Date&gt;" you must ask for
	 *            "Date").
	 * 
	 * @param annotationTypeToFind the annotationType. Can be null.
	 * @param namedAnnotationLiteralValueToFind the literal value of the named
	 *            annotation. Can be null.
	 * @param packageToLimit if given then only the Guice modules in
	 *            the same package are processed. Can be null.
	 * @return the discovered {@link BindingDefinition}. Can be empty but not
	 *         null if we did not find a binding.
	 */
	public List<GuiceStatement> getBindingsByTypeAndAnnotationLimitToPackage(
			ITypeBinding typeToFind, GuiceAnnotation guiceAnnotationToFind,
			Set<String> packageToLimit) {
		String typeToFindQualifiedName = typeToFind.getQualifiedName();

		/*
		 * We only store the wrappers instead of primitives, so maybe we must
		 * replace the primitive type by the wrapper type.
		 */
		typeToFindQualifiedName = StringUtils.translatePrimitiveToWrapper(typeToFindQualifiedName);

		List<GuiceStatement> bindings = ListUtils.newArrayList();
		for (GuiceModule guiceModule : guiceModules) {

			if (packageToLimit != null) {
				String packageFullyQualified = guiceModule.getPackageFullyQualified();

				if (!packageToLimit.contains(packageFullyQualified)) {
					continue;
				}
			}

			List<BindingDefinition> bindingStatements = guiceModule.getBindingDefinitions();

			if (bindingStatements == null) {
				continue;
			}

			for (BindingDefinition bindingStatement : bindingStatements) {
				String bindingsBoundType = bindingStatement.getBoundType();
				if (!bindingsBoundType.equals(typeToFindQualifiedName)) {
					continue;
				}

				GuiceAnnotation guiceAnnotation = bindingStatement.getGuiceAnnotation();
				if (guiceAnnotationToFind != null) {
					if (guiceAnnotation != null
							&& guiceAnnotation.equals(guiceAnnotationToFind)) {
						bindings.add(bindingStatement);
					}
				}
				else { /* No annotation in field declaration */
					if (guiceAnnotation == null) {
						bindings.add(bindingStatement);
					}
				}
			}

		}

		checkForJustInTimeBindings(guiceAnnotationToFind, typeToFind, bindings);

		return bindings;
	}

	private void checkForJustInTimeBindings(
			GuiceAnnotation guiceAnnotationToFind,
			ITypeBinding typeBindingOfInterfaceType,
			List<GuiceStatement> bindings) {
		if (bindings.isEmpty() && guiceAnnotationToFind == null) {

			/*
			 * No implicit binding if the injected type is parameterized!
			 * 
			 * Example:
			 * 
			 * @Inject private ModulSpez<T> modulSpez;
			 * 
			 * @Inject private DatendateiParser<M15N1OV14>
			 * m15n1oDatendateiParser;
			 */
			boolean isConcreteType = ITypeBindingUtils.isConcreteType(typeBindingOfInterfaceType);
			if (isConcreteType) {
				IJavaElement javaElement = typeBindingOfInterfaceType.getJavaElement();

				if (!(javaElement instanceof IMember)) {
					return;
				}

				IMember member = (IMember) javaElement;
				if (member.isBinary()) {
					/* Not supported ! */
					EgapPlugin.logInfo("Just in time binding not supported for binary type "
							+ typeBindingOfInterfaceType.getQualifiedName());
					return;
				}

				IJavaProject javaProject = javaElement.getJavaProject();

				JustInTimeBindingStatement implicitBinding = new JustInTimeBindingStatement();
				IProject project = javaProject.getProject();
				String projectName = project.getName();
				implicitBinding.setProjectName(projectName);

				boolean isParameterizedType = typeBindingOfInterfaceType.isParameterizedType();

				String typeName = null;
				if (isParameterizedType) {
					ITypeBinding typeDeclaration = typeBindingOfInterfaceType.getTypeDeclaration();
					typeName = typeDeclaration.getName();

				}
				else {
					typeName = typeBindingOfInterfaceType.getName();
				}
				implicitBinding.setTypeName(typeName);

				IPackageBinding packageBinding = typeBindingOfInterfaceType.getPackage();
				String[] packageName = packageBinding.getNameComponents();
				implicitBinding.setPackage(Arrays.asList(packageName));

				ICompilationUnit compilationUnit = member.getCompilationUnit();
				List<String> srcFolderPathComponents = ICompilationUnitUtils.getSrcFolderPathComponents(compilationUnit);
				implicitBinding.setSrcFolderPathComponents(srcFolderPathComponents);
				Integer startPositionOfTopLevelType = ICompilationUnitUtils.getStartPositionOfTopLevelType(compilationUnit);
				implicitBinding.setStartPosition(startPositionOfTopLevelType);
				bindings.add(implicitBinding);

			}
		}
	}

	/**
	 * Synonym for getBindingsByTypeAndAnnotationLimitToPackage(typeToFind,
	 * guiceAnnotationToFind, null);
	 * 
	 * @see #getBindingsByTypeAndAnnotationLimitToPackage(ITypeBinding,
	 *      GuiceAnnotation, Set)
	 */
	public List<GuiceStatement> getBindingsByTypeAndAnnotation(
			ITypeBinding typeToFind, GuiceAnnotation guiceAnnotationToFind) {
		return getBindingsByTypeAndAnnotationLimitToPackage(
				typeToFind,
				guiceAnnotationToFind,
				null);
	}

	public AssistedBindingStatement getAssistedBindingByModelType(
			String boundModelType) {
		for (GuiceModule guiceModule : guiceModules) {
			List<BindingDefinition> bindingStatements = guiceModule.getBindingDefinitions();
			if (bindingStatements == null) {
				continue;
			}
			for (BindingDefinition bindingStatement : bindingStatements) {
				if (bindingStatement instanceof AssistedBindingStatement) {
					AssistedBindingStatement assistedBindingStatement = (AssistedBindingStatement) bindingStatement;
					String modelType = assistedBindingStatement.getModelTypeName();
					if (modelType.equals(boundModelType)) {
						return assistedBindingStatement;
					}
				}
			}
		}
		return null;
	}

	public String getIndexInfoShort() {
		return "(" + getNrOfGuiceModules() + " Guice modules indexed)";
	}

	public String getIndexInfoDetailed() {
		return getGuiceIndexStatistic().getDetailedInfo();
	}

	private GuiceIndexStatistic getGuiceIndexStatistic() {
		GuiceIndexStatistic statistic = new GuiceIndexStatistic();

		statistic.nrOfGuiceModules = guiceModules.size();
		Set<String> projectNames = SetUtils.newHashSet();
		for (GuiceModule guiceModule : guiceModules) {
			List<BindingDefinition> bindingStatements = guiceModule.getBindingDefinitions();
			if (bindingStatements == null) {
				continue;
			}
			statistic.nrOfBindingStatements += bindingStatements.size();

			List<InstallModuleStatement> installedModules = guiceModule.getInstalledModules();
			if (installedModules != null) {
				statistic.nrOfInstallStatements += installedModules.size();
			}
			projectNames.add(guiceModule.getProjectName());
		}
		statistic.nrOfProjects = projectNames.size();
		return statistic;
	}

	public class GuiceIndexStatistic {
		public int nrOfGuiceModules = 0;
		public int nrOfBindingStatements = 0;
		public int nrOfInstallStatements = 0;
		public int nrOfProjects = 0;

		public String getDetailedInfo() {

			GuiceIndexStatistic guiceIndexStatistic = getGuiceIndexStatistic();

			StringBuffer sb = new StringBuffer();
			sb.append("Statistic:");
			sb.append(guiceIndexStatistic.nrOfGuiceModules
					+ " Guice modules in ");
			sb.append(guiceIndexStatistic.nrOfProjects + " projects, ");
			sb.append(guiceIndexStatistic.nrOfBindingStatements
					+ " binding statements, ");
			sb.append(guiceIndexStatistic.nrOfInstallStatements
					+ " install statements.");

			return sb.toString();
		}

	}

}