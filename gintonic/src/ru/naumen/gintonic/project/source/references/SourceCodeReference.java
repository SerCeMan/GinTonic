package ru.naumen.gintonic.project.source.references;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.ITextSelection;

import ru.naumen.gintonic.project.files.SelectAndReveal;
import ru.naumen.gintonic.project.navigate.IJumpable;
import ru.naumen.gintonic.project.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.utils.ICompilationUnitSelectionUtils;
import ru.naumen.gintonic.utils.ICompilationUnitUtils;
import ru.naumen.gintonic.utils.StringUtils;

/**
 * A leightweight reference to a source code location. It is currently used to
 * navigate from one source code location to another.
 * 
 * @author tmajunke
 */
public class SourceCodeReference implements Serializable, IJumpable {

    private static final long serialVersionUID = -9134622846341053959L;

    private String projectName;
    private List<String> srcFolderPathComponents;
    private List<String> packageNameComponents;
    private String primaryTypeName;
    private Integer offset;
    private Integer length;

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setSrcFolderPathComponents(List<String> srcFolderPathComponents) {
        this.srcFolderPathComponents = srcFolderPathComponents;
    }

    public void setPackageNameComponents(List<String> packageNameComponents) {
        this.packageNameComponents = packageNameComponents;
    }

    public void setPrimaryTypeName(String primaryTypeName) {
        this.primaryTypeName = primaryTypeName;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * The name of the project the type belongs to.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Returns the path to the src folder. Most likely this is ["src"] but maven
     * projects use ["src", "main", "java"].
     * 
     * @return the path to the src folder.
     */
    public List<String> getSrcFolderPathComponents() {
        return srcFolderPathComponents;
    }

    /**
     * Returns the package as list of strings (e.g ["java","lang"]).
     * 
     * @return the package as list of strings.
     */
    public List<String> getPackageNameComponents() {
        return packageNameComponents;
    }

    /**
     * The package name fully qualified (e.g "java.lang"). Is empty for the
     * default package.
     */
    public String getPackageNameComponentsFullyQualified() {
        return StringUtils.join('.', getPackageNameComponents());
    }

    /**
     * Returns the simple name of the java type (e.g for {@link Collection} it
     * would be Collection).
     */
    public String getPrimaryTypeName() {
        return primaryTypeName;
    }

    /**
     * Returns the fully qualified type name (e.g for {@link Collection} it
     * would be java.util.Collection).
     */
    public String getPrimaryTypeNameFullyQualified() {
        return getPackageNameComponentsFullyQualified() + "." + getPrimaryTypeName();
    }

    /**
     * Returns the character index into the original source file indicating
     * where the source fragment corresponding to this reference begins.
     * 
     * @see ASTNode#getStartPosition()
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Returns the length in characters of the original source file indicating
     * where the source fragment corresponding to this reference ends.
     * 
     * @see ASTNode#getLength()
     */
    public Integer getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
        result = prime * result + ((packageNameComponents == null) ? 0 : packageNameComponents.hashCode());
        result = prime * result + ((primaryTypeName == null) ? 0 : primaryTypeName.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((srcFolderPathComponents == null) ? 0 : srcFolderPathComponents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SourceCodeReference other = (SourceCodeReference) obj;
        if (length == null) {
            if (other.length != null)
                return false;
        } else if (!length.equals(other.length))
            return false;
        if (offset == null) {
            if (other.offset != null)
                return false;
        } else if (!offset.equals(other.offset))
            return false;
        if (packageNameComponents == null) {
            if (other.packageNameComponents != null)
                return false;
        } else if (!packageNameComponents.equals(other.packageNameComponents))
            return false;
        if (primaryTypeName == null) {
            if (other.primaryTypeName != null)
                return false;
        } else if (!primaryTypeName.equals(other.primaryTypeName))
            return false;
        if (projectName == null) {
            if (other.projectName != null)
                return false;
        } else if (!projectName.equals(other.projectName))
            return false;
        if (srcFolderPathComponents == null) {
            if (other.srcFolderPathComponents != null)
                return false;
        } else if (!srcFolderPathComponents.equals(other.srcFolderPathComponents))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "@" + projectName + srcFolderPathComponents + packageNameComponents + "." + primaryTypeName + "("
                + offset + "," + length + ")";
    }

    @Override
    public void jump() {
        jump(getOffset());
    }

    public void jump(int offset) {
        IFile file = resolveIFile();
        SelectAndReveal.selectAndReveal(file, offset, 0);
    }

    public IFile resolveIFile() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(projectName);

        char pathSeparator = StringUtils.PATH_SEPARATOR;

        String sourceFolder = StringUtils.join(pathSeparator, srcFolderPathComponents) + pathSeparator
                + StringUtils.join(pathSeparator, getPackageNameComponents());

        IFolder folder = project.getFolder(sourceFolder);

        String filename = getPrimaryTypeName() + ICompilationUnitUtils.JAVA_EXTENSION;
        IFile file = folder.getFile(filename);

        return file;
    }

    public ICompilationUnit resolveICompilationUnit() {
        IFile resolvedIFile = resolveIFile();
        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(resolvedIFile);
        return compilationUnit;
    }

    public static SourceCodeReference createCurrent() {
        
        ICompilationUnitSelection compilationUnitSelection = ICompilationUnitSelectionUtils.getCompilationUnitSelection();
        if (compilationUnitSelection == null) {
            return null;
        }
        ICompilationUnit icompilationUnit = compilationUnitSelection.getICompilationUnit();
        ITextSelection textSelection= compilationUnitSelection.getITextSelection();
        
    	SourceCodeReference codeReference = new SourceCodeReference();
    
    	IResource resource = icompilationUnit.getResource();
    	IProject project = resource.getProject();
    	codeReference.setProjectName(project.getName());
    
    	List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(icompilationUnit);
    	codeReference.setSrcFolderPathComponents(srcFolderPath);
    
    	IPackageFragment parent = (IPackageFragment) icompilationUnit.getParent();
    	String packageDotSeparated = parent.getElementName();
    	List<String> packageAsList = StringUtils.split('.', packageDotSeparated);
    	codeReference.setPackageNameComponents(packageAsList);
    
    	String typeName = ICompilationUnitUtils.getNameWithoutJavaExtension(icompilationUnit);
    	codeReference.setPrimaryTypeName(typeName);
    
    	if (textSelection != null) {
    		int offset = textSelection.getOffset();
    		codeReference.setOffset(offset);
    		int length = textSelection.getLength();
    		codeReference.setLength(length);
    	}
    
    	return codeReference;
    }

}
