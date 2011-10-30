package egap.guice;

import java.io.Serializable;
import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;

import egap.utils.IProjectResource;

public class ProjectResource implements Serializable, IProjectResource {

	private static final long serialVersionUID = 8853803394114472544L;

	/**
	 * The name of the project the type belongs to.
	 */
	private String projectName;

	/**
	 * The name of the src folder.
	 */
	private String srcFolderName;

	/**
	 * The package name fully qualified (e.g java.lang). Is empty for the
	 * default package.
	 */
	private String packageFullyQualified;

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
			String srcFolderName,
			String packageFullyQualified,
			String typeName,
			Integer startPosition,
			Integer length) {
		super();
		this.projectName = projectName;
		this.srcFolderName = srcFolderName;
		this.packageFullyQualified = packageFullyQualified;
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

	public void setSrcFolderName(String srcFolderName) {
		this.srcFolderName = srcFolderName;
	}

	public void setPackageFullyQualified(String packageFullyQualified) {
		this.packageFullyQualified = packageFullyQualified;
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
	public String getSrcFolderName() {
		return srcFolderName;
	}

	@Override
	public String getPackageFullyQualified() {
		return packageFullyQualified;
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
		return packageFullyQualified + "." + typeName;
	}

	@Override
	public String toString() {
		return "ProjectResource [projectName=" + projectName
				+ ", srcFolderName=" + srcFolderName
				+ ", packageFullyQualified=" + packageFullyQualified
				+ ", typeName=" + typeName + ", startPosition=" + startPosition
				+ ", length=" + length + "]";
	}

	

}
