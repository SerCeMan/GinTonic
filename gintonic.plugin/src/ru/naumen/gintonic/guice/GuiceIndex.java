package ru.naumen.gintonic.guice;

import java.io.Serializable;
import java.util.*;

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

import ru.naumen.gintonic.GinTonicIDs;
import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.guice.annotations.IGuiceAnnotation;
import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.statements.AssistedBindingStatement;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.InstallModuleStatement;
import ru.naumen.gintonic.guice.statements.JustInTimeBindingStatement;
import ru.naumen.gintonic.project.builder.GinTonicProjectBuilder;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.*;

/**
 * The guice index holds the informations about the guice modules as collected
 * during the build process ({@link GinTonicProjectBuilder}), so they can be
 * accessed faster (e.g by the quickfixes).
 * 
 * @author tmajunke
 */
public class GuiceIndex implements Serializable {

    private static final long serialVersionUID = 2155643314847687772L;

    private static volatile GuiceIndex instance;

    private GuiceIndexState buildState = GuiceIndexState.INITIAL;

    private boolean fromDisc;

    /**
     * The guice modules.
     */
    private ArrayList<GuiceModule> guiceModules = new ArrayList<GuiceModule>(100);

    public GuiceIndex() {
        super();
    }

    public static GuiceIndex get() {
        if (instance == null) {
            synchronized (GuiceIndex.class) {
                if (instance == null) {
                    instance = new GuiceIndex();
                    build();
                }
            }
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

    public GuiceIndexState getBuildState() {
        return buildState;
    }

    public void setBuildState(GuiceIndexState buildState) {
        this.buildState = buildState;
    }

    private static Job build() {

        /*
         * The Job has versus the IWorkspaceRunnable approach the nose ahead as
         * it delivers us with a IProgressMonitor so we can see the progress in
         * the UI and maybe cancel it.
         */
        Job job = new Job("GinTonic - Building Guice/Gin module index") {
            @Override
            protected IStatus run(IProgressMonitor progressMonitor) {
                final List<IProject> projectsWithGinTonicNature = IProjectUtils.getAccessibleProjectsWithGinTonicNature();
                for (IProject project : projectsWithGinTonicNature) {
                    try {
                        IProject iProject = project.getProject();
                        iProject.build(IncrementalProjectBuilder.FULL_BUILD, GinTonicIDs.BUILDER, null, progressMonitor);
                    } catch (CoreException e) {
                        GinTonicPlugin.logException(e);
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

        guiceModules.add(guiceModule);
        if (log) {
            GinTonicPlugin.logInfo("Added " + guiceModule.getPrimaryTypeNameFullyQualified() + " to Guice index "
                    + getIndexInfoShort() + ".");
        }
    }

    /**
     * Removes all modules of the given project.
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
            removeGuiceModule(guiceModule.getPrimaryTypeNameFullyQualified(), false);
        }
    }

    public void removeGuiceModule(String nameFullyQualified, boolean log) {
        for (int i = 0; i < guiceModules.size(); i++) {
            GuiceModule guiceModule = guiceModules.get(i);
            String fullyQualifiedName = guiceModule.getPrimaryTypeNameFullyQualified();
            if (fullyQualifiedName.equals(nameFullyQualified)) {
                GuiceModule removedModule = guiceModules.remove(i);
                if (log) {
                    String message = nameFullyQualified + " from Guice index " + getIndexInfoShort() + ".";
                    if (removedModule == null) {
                        message = "Tried but not succeeded in removing " + message;
                    } else {
                        message = "Removed " + message;
                    }
                    GinTonicPlugin.logInfo(message);

                }
            }
        }
    }

    public void updateGuiceModule(GuiceModule guiceModule) {
        removeGuiceModule(guiceModule.getPrimaryTypeNameFullyQualified(), false);
        addGuiceModule(guiceModule, false);
        GinTonicPlugin.logInfo("Updated " + guiceModule.getPrimaryTypeNameFullyQualified() + " from Guice index "
                + getIndexInfoShort() + ".");
    }

    /**
     * <pre>
     * getGuiceModulesInAndBelowPackage(currentPackageBinding, null, 2);
     * </pre>
     */
    public List<GuiceModule> getGuiceModulesInAndBelowPackage(IPackageBinding currentPackageBinding) {
        return getGuiceModulesInAndBelowPackage(currentPackageBinding, null, 2);
    }

    /**
     * Returns the guice modules of the given package and the parent packages as
     * indicated by the parameter depth. Returns an empty list if no modules
     * were found.
     * 
     * @param packageBinding
     *            the package in which we look for the guice modules
     * @param ignoreModule
     *            a fully qualified type name of a guice module, which is
     *            ignored. Can be null.
     * @param depth
     *            the number of parent packages to include (0 = the empty list
     *            is returned, 1 means the modules in the given package, 2 means
     *            the modules in the given package and the parent package, 3...
     *            think you got it).
     */
    public List<GuiceModule> getGuiceModulesInAndBelowPackage(IPackageBinding packageBinding, String ignoreModule,
            int depth) {
        IPackageFragment currentPackage = (IPackageFragment) packageBinding.getJavaElement();
        List<IPackageFragment> parentPackages = IPackageFragmentUtils.getParentPackages(currentPackage, depth);
        parentPackages.add(currentPackage);
        List<GuiceModule> modules = getGuiceModulesInPackage(parentPackages, ignoreModule);
        return modules;
    }

    /**
     * Returns the guice modules in the given packages. Returns an empty List if
     * no modules were found.
     */
    public List<GuiceModule> getGuiceModulesInPackage(List<IPackageFragment> packages, String moduleToIgnore) {
        List<GuiceModule> guiceModulesInGivenPackages = ListUtils.newArrayList();
        for (IPackageFragment packageFragment : packages) {
            if (packageFragment == null) {
                continue;
            }
            String packagePath = packageFragment.getElementName();

            for (GuiceModule guiceModule : guiceModules) {
                String packageFullyQualified = guiceModule.getPackageNameComponentsFullyQualified();
                boolean ignoreModule = guiceModule.getPrimaryTypeNameFullyQualified().equals(moduleToIgnore);

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

    public List<BindingDefinition> getBindingDefinitions(IInjectionPoint injectionPoint) {
        ITypeBinding typeBinding = injectionPoint.getTargetTypeBinding();
        ITypeBinding typeBindingWithoutProvider = ITypeBindingUtils.removeSurroundingProvider(typeBinding);
        IGuiceAnnotation guiceAnnotation = injectionPoint.getGuiceAnnotation();
        List<BindingDefinition> bindingDefinitions = getBindingDefinitions(typeBindingWithoutProvider, guiceAnnotation);

        return bindingDefinitions;
    }

    /**
     * Returns the {@link BindingDefinition}s for the given bound type and
     * annotation type.
     * 
     * <h5>Just in time binding</h5>
     * 
     * If we could not find an explicit binding then we check if it could be a
     * just-in-time-binding. We assume a just-in-time-binding if the typeToFind
     * is a concrete class and there is no guice annotation applied and it is
     * not a binary type.
     * 
     * <h5>MapBinder Statements</h5>
     * 
     * If the type to find is a {@link Map}, then we also check if we can find a
     * suitable MapBinder statement.
     * 
     * @param typeToFind
     *            the bound type. Cannot be null. Primitives (like "int") values
     *            are allowed. Provider types are not allowed( e.g instead of
     *            asking for "Provider&lt;Date&gt;" you must ask for "Date").
     * 
     * @param annotationTypeToFind
     *            the annotationType. Can be null.
     * @param namedAnnotationLiteralValueToFind
     *            the literal value of the named annotation. Can be null.
     * @param packageToLimit
     *            if given then only the Guice modules in the same package are
     *            processed. Can be null.
     * @return the discovered {@link BindingDefinition}s. Is empty if we did not
     *         find a binding.
     */
    public List<BindingDefinition> getBindingDefinitions(ITypeBinding typeToFind,
            IGuiceAnnotation guiceAnnotationToFind, Set<String> packageToLimit) {
        String typeToFindQualifiedName = typeToFind.getQualifiedName();
        /*
         * We only store the wrappers instead of primitives, so maybe we must
         * replace the primitive type by the wrapper type.
         */
        typeToFindQualifiedName = StringUtils.translatePrimitiveToWrapper(typeToFindQualifiedName);
        
        List<BindingDefinition> bindings = ListUtils.newArrayList();
        for (GuiceModule guiceModule : guiceModules) {
        
            if (packageToLimit != null) {
                String packageFullyQualified = guiceModule.getPackageNameComponentsFullyQualified();
        
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
        
                IGuiceAnnotation guiceAnnotation = bindingStatement.getGuiceAnnotation();
                if (guiceAnnotationToFind != null) {
                    if (guiceAnnotation != null && guiceAnnotation.equals(guiceAnnotationToFind)) {
                        bindings.add(bindingStatement);
                    }
                } else { /* No annotation in field declaration */
                    if (guiceAnnotation == null) {
                        bindings.add(bindingStatement);
                    }
                }
            }
        
        }

        checkForJustInTimeBindings(guiceAnnotationToFind, typeToFind, bindings);

        return bindings;
    }

    private void checkForJustInTimeBindings(IGuiceAnnotation guiceAnnotationToFind,
            ITypeBinding typeBindingOfInterfaceType, List<BindingDefinition> bindings) {
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
                    return;
                }

                IJavaProject javaProject = javaElement.getJavaProject();

                IProject project = javaProject.getProject();
                String projectName = project.getName();

                boolean isParameterizedType = typeBindingOfInterfaceType.isParameterizedType();

                String typeName = null;
                if (isParameterizedType) {
                    ITypeBinding typeDeclaration = typeBindingOfInterfaceType.getTypeDeclaration();
                    typeName = typeDeclaration.getName();
                } else {
                    typeName = typeBindingOfInterfaceType.getName();
                }

                IPackageBinding packageBinding = typeBindingOfInterfaceType.getPackage();
                String[] packageName = packageBinding.getNameComponents();

                ICompilationUnit compilationUnit = member.getCompilationUnit();
                List<String> srcFolderPathComponents = ICompilationUnitUtils
                        .getSrcFolderPathComponents(compilationUnit);
                Integer startPositionOfTopLevelType = ICompilationUnitUtils
                        .getStartPositionOfTopLevelType(compilationUnit);

                JustInTimeBindingStatement justInTimeBinding = new JustInTimeBindingStatement();

                SourceCodeReference sourceCodeReference = new SourceCodeReference();

                sourceCodeReference.setProjectName(projectName);
                sourceCodeReference.setPrimaryTypeName(typeName);
                sourceCodeReference.setPackageNameComponents(Arrays.asList(packageName));
                sourceCodeReference.setSrcFolderPathComponents(srcFolderPathComponents);
                sourceCodeReference.setOffset(startPositionOfTopLevelType);

                justInTimeBinding.setSourceCodeReference(sourceCodeReference);

                bindings.add(justInTimeBinding);
            }
        }
    }

    /**
     * Synonym for getBindingsByTypeAndAnnotationLimitToPackage(typeToFind,
     * guiceAnnotationToFind, null);
     * 
     * @see #getBindingDefinitions(ITypeBinding, IGuiceAnnotation, Set)
     */
    public List<BindingDefinition> getBindingDefinitions(ITypeBinding typeToFind, IGuiceAnnotation guiceAnnotationToFind) {
        return getBindingDefinitions(typeToFind, guiceAnnotationToFind, null);
    }

    public AssistedBindingStatement getAssistedBindingDefinitionsByModelType(String boundModelType) {
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

            StringBuilder sb = new StringBuilder("Statistic:");
            sb.append(guiceIndexStatistic.nrOfGuiceModules + " Guice modules in ");
            sb.append(guiceIndexStatistic.nrOfProjects + " projects, ");
            sb.append(guiceIndexStatistic.nrOfBindingStatements + " binding statements, ");
            sb.append(guiceIndexStatistic.nrOfInstallStatements + " install statements.");

            return sb.toString();
        }

    }
}
