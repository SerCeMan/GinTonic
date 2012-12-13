package de.jaculon.egap.project_resource;

import java.io.Serializable;
import java.util.List;

import de.jaculon.egap.utils.StringUtils;

public class ProjectResource implements Serializable, IProjectResource {

	private static final long serialVersionUID = 8853803394114472544L;

	private String projectName;
	private List<String> srcFolderPathComponents;
	private List<String> packageNameComponents;
	private String typeName;
	private Integer startPosition;

	public ProjectResource() {
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setSrcFolderPathComponents(List<String> srcFolderPathComponents) {
		this.srcFolderPathComponents = srcFolderPathComponents;
	}

	public void setPackage(List<String> packageFullyQualified) {
		this.packageNameComponents = packageFullyQualified;
	}

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
	public List<String> getSrcFolderPathComponents() {
		return srcFolderPathComponents;
	}

	@Override
	public List<String> getPackage() {
		return packageNameComponents;
	}

	@Override
	public String getPackageFullyQualified() {
		return StringUtils.join('.', packageNameComponents);
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
	public String getTypeNameFullyQualified() {
		return getPackageFullyQualified() + "." + typeName;
	}

	@Override
	public String toString() {
		return "@" + projectName + srcFolderPathComponents
				+ packageNameComponents + "." + typeName + "(" + startPosition
				+ ")";
	}

}
