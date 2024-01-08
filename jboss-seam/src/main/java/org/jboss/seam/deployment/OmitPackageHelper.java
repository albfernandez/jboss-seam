package org.jboss.seam.deployment;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletContext;

import org.jboss.seam.util.Strings;

public final class OmitPackageHelper {

	public static final String KEY_OMIT_PACKAGES = "org.jboss.seam.deployment.OMIT_PACKAGES";
	public static final String KEY_OMIT_EXTENSIONS = "org.jboss.seam.deployment.OMIT_EXTENSIONS";
	
	private String[] ignoredPackages;
	private String[] ignoredExtensions;

	
	public static OmitPackageHelper getInstance(ServletContext ctx) {

		if (ctx.getAttribute(KEY_OMIT_PACKAGES) == null) {
			String omittedPackageString = ctx.getInitParameter(KEY_OMIT_PACKAGES);
			String ignoredExtensionsString = ctx.getInitParameter(KEY_OMIT_EXTENSIONS);
			String[] filterPackages = Strings.splitTrimAndRemoveEmpty(omittedPackageString, ",");
			String[] filterExtensions = Strings.splitTrimAndRemoveEmpty(ignoredExtensionsString, ",");			
			OmitPackageHelper instance = new OmitPackageHelper(filterPackages, filterExtensions);

			ctx.setAttribute(KEY_OMIT_PACKAGES, instance);
			return instance;
		}
		return (OmitPackageHelper) ctx.getAttribute(KEY_OMIT_PACKAGES);
	}
	


	private OmitPackageHelper(String[] ignoredPackages, String[] ignoredExtensions) {
		super();
		assert ignoredPackages != null;
		assert ignoredExtensions != null;
		this.ignoredPackages = ignoredPackages;
		this.ignoredExtensions = ignoredExtensions;
	}

	public boolean acceptClass(String fullClassFileName) {
		if (Strings.isEmpty(fullClassFileName)) {
			return false;
		}
		boolean acceptExtension = ignoredExtensions.length == 0 || acceptExtension(fullClassFileName);
		if (!acceptExtension) {
			return false;
		}
		if (ignoredPackages.length == 0) {
			return true;
		}
		int idx = fullClassFileName.lastIndexOf('/');

		String packageName = fullClassFileName;
		if (idx >= 0) {
			packageName = fullClassFileName.substring(0, idx);
		}
		return acceptPackage1(packageName);
	}

	public boolean acceptPackage(String packageName) {
		if (Strings.isEmpty(packageName)) {
			return true;
		}
		if (ignoredPackages.length == 0) {
			return true;
		}
		if (packageName.endsWith("/") || packageName.endsWith("\\")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
		return acceptPackage1(packageName);
	}
	
	private boolean acceptExtension(String packageName) {
		for (String ignored: ignoredExtensions) {
			if (packageName.endsWith(ignored)) {
				return false;
			}
		}
		return true;
	}

	private boolean acceptPackage1(String packageName) {
		packageName = packageName.replace('/', '.').replace('\\', '.');
		for (String ignored : ignoredPackages) {
			if (packageName.startsWith(ignored)) {
				return false;
			}
		}
		return true;
	}
}
