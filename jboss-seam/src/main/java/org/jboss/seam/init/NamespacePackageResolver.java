/*
 * JBoss, Home of Professional Open Source
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package org.jboss.seam.init;

import java.net.URI;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * <p>Converts an <a href="http://www.w3.org/TR/xml-names/">XML namespace</a> to a Java package name.</p>
 * 
 * @author <a href="mailto:eric DOT jung AT yahoo DOT com">Eric H. Jung</a>
 */
public class NamespacePackageResolver {
	private static final String JAVA_SCHEME = "java";

	@SuppressWarnings("unused")
	private static final LogProvider log = Logging.getLogProvider(NamespacePackageResolver.class);
	
	public NamespacePackageResolver() {
		super();
	}

	/**
	 * <p>Converts an XML namespace, <code>ns</code>, to a Stringified package name
	 *
	 * @param ns the xml namespace to convert
	 * 
	 * @return a namespace descriptor
	 */
	public String resolve(final String ns) {
		try {
			return parseURI(new URI(ns));
		} catch (Exception e) {
			// the exact exception doesn't matter here.  The caller
			// can log if needed
			return null;
		}
	}

	private String parseURI(URI uri) {
		if (!uri.isAbsolute()) {
			throw new IllegalArgumentException(uri + " is not an absolute URI");
		}

		return uri.isOpaque() ? parseOpaqueURI(uri) : parseHierarchicalURI(uri);
	}

	/**
	 * java:package 
	 * seam:component
	 * seam:package:prefix
	 */
	private String parseOpaqueURI(URI uri) {
		if (JAVA_SCHEME.equalsIgnoreCase(uri.getScheme())) {
			return uri.getSchemeSpecificPart();
		}
		throw new IllegalArgumentException("Unrecognized scheme in " + uri);
	}

	private String parseHierarchicalURI(URI uri) {
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new IllegalArgumentException("Hierarchical URLs must use http or https scheme " + uri);
		}

		StringBuilder buf = new StringBuilder();
		appendToPackageName(buf, hostnameToPackage(uri.getHost()));
		appendToPackageName(buf, pathToPackage(uri.getPath()));

		return buf.toString();
	}

	/**
	 * Convert path elements to package names in forward order
	 */
	private String pathToPackage(String path) {
		StringBuilder buf = new StringBuilder();

		if (path != null) {
			String[] pathElements = path.split("/");
			for (int i = 1; i < pathElements.length; i++) {
				appendToPackageName(buf, pathElements[i]);
			}
		}

		return buf.toString();
	}

	private String hostnameToPackage(String hostname) {
		StringBuilder result = new StringBuilder();

		String[] subdomains = hostname.split("\\.");

		//Iterate through the subdomains in reverse converting each to a package name. 
		for (int i = subdomains.length - 1; i >= 0; i--) {
			String subdomain = subdomains[i];
			if (i > 0 || !"www".equalsIgnoreCase(subdomain)) {
				appendToPackageName(result, subdomain);
			}
		}

		return result.toString();
	}

	private void appendToPackageName(StringBuilder buf, String subdomain) {
		if (subdomain.length() > 0) {
			subdomain = makeSafeForJava(subdomain);

			if (buf.length() > 0) {
				buf.append('.');
			}

			buf.append(subdomain);
		}
	}

	/**
	 * Converts characters in <code>subdomain</code> which aren't java-friendly
	 * into java-friendly equivalents. Right now, we only support the conversion
	 * of hyphens ("-") to underscores ("_"). We could do other things like toLowerCase(),
	 * but there are instances of upper-case package names in widespread use even by the
	 * likes of IBM (e.g., <a href="http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.dc.doc/dc/r_jdbcdrivers.htm">
	 * COM.ibm.db2 classnames</a>).
	 * 
	 * @param subdomain
	 * @return
	 */
	private String makeSafeForJava(String subdomain) {
		return subdomain.replace('-', '_');
	}
}
