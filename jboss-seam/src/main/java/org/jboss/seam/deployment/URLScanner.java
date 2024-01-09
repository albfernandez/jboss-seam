package org.jboss.seam.deployment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;

import javassist.bytecode.ClassFile;

/**
 * Implementation of {@link Scanner} which can scan a {@link URLClassLoader}
 * 
 * @author Thomas Heute
 * @author Gavin King
 * @author Norman Richards
 * @author Pete Muir
 *
 */
public class URLScanner extends AbstractScanner {
	private static final LogProvider log = Logging.getLogProvider(URLScanner.class);

	private long timestamp;

	public URLScanner(DeploymentStrategy deploymentStrategy) {
		super(deploymentStrategy);
	}

	@Override
	public void scanDirectories(File[] directories) {
		scanDirectories(directories, new File[0]);
	}

	@Override
	public void scanDirectories(File[] directories, File[] excludedDirectories) {
		for (File directory : directories) {
			handleDirectory(directory, null, excludedDirectories);
		}
	}

	@Override
	public void scanResources(String[] resources) {
		long startTimeNano = System.nanoTime();
		Set<String> paths = new HashSet<String>();
		for (String resourceName : resources) {
			try {
				Enumeration<URL> urlEnum = getDeploymentStrategy().getClassLoader().getResources(resourceName);
				while (urlEnum.hasMoreElements()) {
					String urlPath = urlEnum.nextElement().getFile();
					urlPath = URLDecoder.decode(urlPath, StandardCharsets.UTF_8.name());
					if (urlPath.startsWith("file:")) {
						urlPath = urlPath.substring(5);
					}
					if (urlPath.indexOf('!') > 0) {
						urlPath = urlPath.substring(0, urlPath.indexOf('!'));
					} else {
						File dirOrArchive = new File(urlPath);
						if (resourceName != null && resourceName.lastIndexOf('/') > 0) {
							//for META-INF/components.xml
							dirOrArchive = dirOrArchive.getParentFile();
						}
						urlPath = dirOrArchive.getParent();
					}
					paths.add(urlPath);
				}
			} catch (IOException ioe) {
				if (log.isWarnEnabled()) {
					log.warn("could not read: " + resourceName, ioe);
				}
			}
		}
		long finishTimeNano = System.nanoTime();
		if (log.isInfoEnabled()) {
			log.info("found " + paths.size() + " resources in " + ((finishTimeNano - startTimeNano)/1000000L) + " ms");
		}

		startTimeNano = System.nanoTime();
		handle(paths);
		finishTimeNano = System.nanoTime();

		if (log.isInfoEnabled()) {
			log.info("handled all resources in " + ((finishTimeNano - startTimeNano)/1000000L) + " ms");
		}

	}

	protected void handle(Set<String> paths) {
		for (String urlPath : paths) {
			long startTimeNano = System.nanoTime();
			try {
				if (log.isTraceEnabled()) {
					log.trace("scanning: " + urlPath);
				}
				File file = new File(urlPath);
				if (file.isDirectory()) {
					handleDirectory(file, null);
				} else if (file.isFile() && file.exists()) {
					handleArchiveByFile(file);
				} else {
					if (log.isWarnEnabled()) {
						log.warn("file not found:" + urlPath);
					}
				}
			} catch (IOException ioe) {
				if (log.isWarnEnabled()) {
					log.warn("could not read entries", ioe);
				}
			}
			long finishTimeNano = System.nanoTime();
			if (log.isInfoEnabled()) {
				log.info("loaded " + urlPath + " in " + ((finishTimeNano - startTimeNano)/1000000L) + " ms");
			}
		}
	}

	private void handleArchiveByFile(File file) throws IOException {
		ZipFile zip = null;
		try {
			if (log.isTraceEnabled()) {
				log.trace("archive: " + file);
			}
			touchTimestamp(file);
			zip = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				String name = entry.getName();
				if (omitPackage.acceptClass(name)) {
					ClassFile classFile = null;
					if (name.endsWith(".class") && !this.scanCache.isHit(name)) {
						java.io.InputStream inputStream = null;
						try {
							inputStream = zip.getInputStream(entry);
							classFile = loadClassFile(inputStream);
						}
						finally {
							Resources.close(inputStream);
						}
					}
					handle(name, classFile);
				}
			}

		} catch (ZipException e) {
			throw new RuntimeException("Error handling file " + file, e);
		} finally {
			Resources.close(zip);
		}
	}

	private void handleDirectory(File file, String path) {
		handleDirectory(file, path, new File[0]);
	}

	private void handleDirectory(File file, String path, File[] excludedDirectories) {
		for (File excludedDirectory : excludedDirectories) {
			if (file.equals(excludedDirectory)) {
				if (log.isTraceEnabled()) {
					log.trace("skipping excluded directory: " + file);
				}
				return;
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("handling directory: " + file);
		}
		File[] childFiles = file.listFiles();
		if (childFiles == null) {
			return;
		}
		for (File child : childFiles) {
			String newPath = path == null ? child.getName() : path + '/' + child.getName();
			if (child.isDirectory()) {
				if (omitPackage.acceptPackage(newPath)) {
					handleDirectory(child, newPath, excludedDirectories);
				}
			} else if (omitPackage.acceptClass(newPath)) {
				ClassFile classFile = null;
				if (newPath.endsWith(".class") && !this.scanCache.isHit(newPath)) {
					java.io.InputStream inputStream = null;
					try {
						inputStream = new BufferedInputStream(Files.newInputStream(child.toPath()));
						classFile = loadClassFile(inputStream);
					}
					catch (IOException ioe) {
						classFile = null;
					}
					finally {
						Resources.close(inputStream);
					}
				}
				if (handle(newPath, classFile)) {
					// only try to update the timestamp on this scanner if the file was actually handled
					touchTimestamp(child);
				}
			}
		}
	}

	private void touchTimestamp(File file) {
		if (file.lastModified() > timestamp) {
			timestamp = file.lastModified();
		}
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
