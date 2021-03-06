package org.jboss.seam.util;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.jboss.seam.Seam;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

public class Resources {
	private static final LogProvider log = Logging.getLogProvider(Resources.class);

	public static InputStream getResourceAsStream(String resource, ServletContext servletContext) {
		String stripped = resource.startsWith("/") ? resource.substring(1) : resource;

		InputStream stream = null;

		if (servletContext != null) {
			try {
				stream = servletContext.getResourceAsStream(resource);
				if (stream != null) {
					if (log.isDebugEnabled()) {
						log.debug("Loaded resource from servlet context: " + resource);
					}
					return stream;
				}
			} catch (Exception ignored) {
				//
			}
		}

		return getResourceAsStream(resource, stripped);
	}

	public static URL getResource(String resource, ServletContext servletContext) {
		if (!resource.startsWith("/")) {
			resource = "/" + resource;
		}

		String stripped = resource.startsWith("/") ? resource.substring(1) : resource;

		URL url = null;

		if (servletContext != null) {
			try {
				url = servletContext.getResource(resource);
				if (log.isDebugEnabled()) {
					log.debug("Loaded resource from servlet context: " + url);
				}
			} catch (Exception ignored) {
				//
			}
		}

		if (url == null) {
			url = getResource(resource, stripped);
		}

		return url;
	}

	static InputStream getResourceAsStream(String resource, String stripped) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream stream = null;
		if (classLoader != null) {
			stream = classLoader.getResourceAsStream(stripped);
			if (stream != null) {
				if (log.isDebugEnabled()) {
					log.debug("Loaded resource from context classloader: " + stripped);
				}
				return stream;
			}
		}

		InputStream stream2 = null;
		stream2 = Seam.class.getResourceAsStream(resource);
		if (stream2 != null) {
			if (log.isDebugEnabled()) {
				log.debug("Loaded resource from Seam classloader: " + resource);
			}
			return stream2;
		}

		InputStream stream3 = Seam.class.getClassLoader().getResourceAsStream(stripped);
		if (stream3 != null) {
			if (log.isDebugEnabled()) {
				log.debug("Loaded resource from Seam classloader: " + stripped);
			}
			return stream3;
		}

		return null;
	}

	static URL getResource(String resource, String stripped) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = null;
		if (classLoader != null) {
			url = classLoader.getResource(stripped);
			if (url != null && log.isDebugEnabled()) {
				log.debug("Loaded resource from context classloader: " + url);
			}
		}

		if (url == null) {
			url = Seam.class.getResource(resource);
			if (url != null && log.isDebugEnabled()) {
				log.debug("Loaded resource from Seam classloader: " + url);
			}
		}

		if (url == null) {
			url = Seam.class.getClassLoader().getResource(stripped);
			if (url != null && log.isDebugEnabled()) {
				log.debug("Loaded resource from Seam classloader: " + url);
			}
		}

		return url;
	}

	public static void close(AutoCloseable... closeables) {
		for (AutoCloseable c : closeables) {
			Resources.close(c);
		}
	}

	public static void close(AutoCloseable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception ignored) {
			//
		}
	}

	public static void closeStream(InputStream inputStream) {
		close(inputStream);
	}

	public static void closeReader(java.io.Reader reader) {
		close(reader);
	}

	public static File getRealFile(ServletContext servletContext, String path) {
		String realPath = servletContext.getRealPath(path);
		if (realPath == null) {
			 //WebLogic!
			try {
				URL resourcePath = servletContext.getResource(path);
				if (resourcePath != null && "file".equals(resourcePath.getProtocol())) {
					realPath = resourcePath.getPath();
				} else {
					if (log.isInfoEnabled()) {
						log.info("Unable to determine real path from servlet context for \"" + path + "\" path does not exist.");
					}
				}
			} catch (MalformedURLException e) {
				if (log.isWarnEnabled()) {
					log.warn("Unable to determine real path from servlet context for : " + path);
				}
				log.debug("Caused by MalformedURLException", e);
			}

		}

		if (realPath != null) {
			File file = new File(realPath);
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

}
