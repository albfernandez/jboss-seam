package org.jboss.seam.deployment;

import java.io.File;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.CloneUtils;

/**
 * A special deployment strategy that can be used to scan the war root. This
 * is treated as a special case. 
 *
 * @author pmuir
 *
 */
public class WarRootDeploymentStrategy extends DeploymentStrategy {

	private static LogProvider log = Logging.getLogProvider(WarRootDeploymentStrategy.class);

	private ClassLoader classLoader;

	private ServletContext servletContext;

	private File[] warRoot;

	private File[] excludedDirectories;

	public static final String HANDLERS_KEY = "org.jboss.seam.deployment.deploymentHandlers";

	public static final String NAME = "warRootDeploymentStrategy";

	private DotPageDotXmlDeploymentHandler dotPageDotXmlDeploymentHandler;


	public WarRootDeploymentStrategy(ClassLoader classLoader, File warRoot, ServletContext servletContext) {
		this(classLoader, warRoot, servletContext, new File[0]);
	}

	public WarRootDeploymentStrategy(ClassLoader classLoader, File warRoot, ServletContext servletContext, File[] excludedDirectories) {
		super();
		this.classLoader = classLoader;
		this.servletContext = servletContext;
		this.warRoot = new File[1];
		this.excludedDirectories = CloneUtils.cloneArray(excludedDirectories);
		if (warRoot != null) {
			this.warRoot[0] = warRoot;
			getFiles().add(warRoot);
		} else {
			log.warn("Unable to discover war root, .page.xml files won't be found");
			this.warRoot = new File[0];
		}
		dotPageDotXmlDeploymentHandler = new DotPageDotXmlDeploymentHandler();
		PagesDotXmlDeploymentHandler pagesDotXmlDeploymentHandler = new PagesDotXmlDeploymentHandler();
		getDeploymentHandlers().put(DotPageDotXmlDeploymentHandler.NAME, dotPageDotXmlDeploymentHandler);
		getDeploymentHandlers().put(PagesDotXmlDeploymentHandler.NAME, pagesDotXmlDeploymentHandler);

	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	protected String getDeploymentHandlersKey() {
		return HANDLERS_KEY;
	}

	@Override
	public void scan() {
		Scanner scanner = getScanner();
		scanner.scanDirectories(warRoot, excludedDirectories);
		if (scanner instanceof AbstractScanner) {
			if (((AbstractScanner) scanner).isTimestampScan()) {
				return;
			}
		}
		postScan();
	}

	public File[] getExcludedDirectories() {
		return CloneUtils.cloneArray(excludedDirectories);
	}

	public Set<FileDescriptor> getDotPageDotXmlFileNames() {
		return dotPageDotXmlDeploymentHandler.getResources();
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

}
