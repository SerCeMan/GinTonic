package de.jaculon.egap.junit;

import java.util.Arrays;
import java.util.List;

public class MyPackage {

	private List<String> packageParts;

	public MyPackage(String... packageParts) {
		super();
		this.packageParts = Arrays.asList(packageParts);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((packageParts == null) ? 0 : packageParts.hashCode());
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
		MyPackage other = (MyPackage) obj;
		if (packageParts == null) {
			if (other.packageParts != null)
				return false;
		}
		else if (!packageParts.equals(other.packageParts))
			return false;
		return true;
	}



}
