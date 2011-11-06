package egap.guice;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Joiner;

import egap.utils.IProjectResource;

public class ProjectResource implements Serializable, IProjectResource {

	private static final Joiner JOINER_ON_DOT = Joiner.on('.');
	private static final Joiner JOINER_ON_FILE_SEPARATOR = Joiner.on('/');

	private static final long serialVersionUID = 8853803394114472544L;

	/**
	 * The name of the project the type belongs to.
	 */
	private String projectName;

	private List<String> srcFolderPathComponents;

	/**
	 * The package name fully qualified (e.g java.lang). Is empty for the
	 * default package.
	 */
	private List<String> packageNameComponents;

	/**
	 * The name of the java type (e.g for {@link Collection} it would be
	 * Collection).
	 */
	private String typeName;

	/**
	 * @see ASTNode#getStartPosition()
	 */
	protected Integer startPosition;

	/**
	 * @see ASTNode#getLength()
	 */
	protected Integer length;

	public ProjectResource(String projectName,
			List<String> srcFolderPathComponents,
			List<String> packageFullyQualified,
			String typeName,
			Integer startPosition,
			Integer length) {
		super();
		this.projectName = projectName;
		this.srcFolderPathComponents = srcFolderPathComponents;
		this.packageNameComponents = packageFullyQualified;
		this.typeName = typeName;
		this.startPosition = startPosition;
		this.length = length;
	}

	public ProjectResource() {
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setSrcFolderPathComponents(List<String> srcFolderPathComponents) {
		this.srcFolderPathComponents = srcFolderPathComponents;
	}

	public void setPackagePathComponents(List<String> packageFullyQualified) {
		if(packageFullyQualified == null){
			System.out.println();
		}
		
		this.packageNameComponents = packageFullyQualified;
	}
	
	/**
	 * The simple name of the java type (e.g for {@link Collection} it would be
	 * Collection).
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getSrcFolderPath() {
		return JOINER_ON_FILE_SEPARATOR.join(srcFolderPathComponents);
	}
	
	@Override
	public List<String> getSrcFolderPathComponents() {
		return srcFolderPathComponents;
	}

	@Override
	public List<String> getPackageNameComponents() {
		return packageNameComponents;
	}
	

	@Override
	public String getPackageFullyQualified() {
		return JOINER_ON_DOT.join(packageNameComponents);
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public Integer getStartPosition() {
		return startPosition;
	}

	@Override
	public Integer getLength() {
		return length;
	}

	@Override
	public String getTypeNameFullyQualified() {
		return packageNameComponents + "." + typeName;
	}

}
