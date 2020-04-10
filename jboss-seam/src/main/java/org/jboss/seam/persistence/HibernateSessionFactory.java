//$Id: HibernateSessionFactory.java 6143 2007-09-07 00:59:34Z gavin $
package org.jboss.seam.persistence;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.HibernateSecurityInterceptor;
import org.jboss.seam.util.Naming;
import org.jboss.seam.util.Reflections;

/**
 * A Seam component that bootstraps a Hibernate SessionFactory
 *
 * <p>
 * Loads Hibernate configuration options by checking:
 * <li>hibernate.properties in root of the classpath
 * <li>hibernate.cfg.xml in root of the classpath
 * <li>cfgResourceName as location of a cfg.xml file
 * <li>factory-supplied cfgProperties options
 * <p>
 * Note that this factory only supports cfg.xml files <b>or</b> programmatic
 * <tt>cfgProperties</tt> supplied to the factory. Any
 * <tt>hibernate.properties</tt> are always loaded from the classpath.
 * <p>
 * Mapping metadata can be supplied via
 * <li>mappingClasses: equivalent to &lt;mapping class="..."/&gt;</li>
 * <li>mappingFiles: equivalent to &lt;mapping file="..."/&gt;</li>
 * <li>mappingJars: equivalent to &lt;mapping jar="..."/&gt;</li>
 * <li>mappingPackages: equivalent to &lt;mapping package="..."/&gt;</li>
 * <li>mappingResources: equivalent to &lt;mapping resource="..."/&gt;<li>
 * <p>
 * or via cfg.xml files.
 * <p>
 * The <tt>jndiProperties</tt> are convenience, the factory will automatically
 * prefix regular JNDI properties for use as Hibernate configuration properties.
 *
 * @author Gavin King
 * @author Christian Bauer
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup
public class HibernateSessionFactory {
	private SessionFactory sessionFactory;

	private String cfgResourceName;
	private Map<String, String> cfgProperties;
	private List<String> mappingClasses;
	private List<String> mappingFiles;
	private List<String> mappingJars;
	private List<String> mappingPackages;
	private List<String> mappingResources;
	private ImplicitNamingStrategy implicitNamingStrategy;
	private PhysicalNamingStrategy physicalNamingStrategy;

	@Unwrap
	public SessionFactory getSessionFactory() throws Exception {
		return sessionFactory;
	}

	@Create
	public void startup() throws Exception {
		sessionFactory = createSessionFactory();
	}

	@Destroy
	public void shutdown() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	protected SessionFactory createSessionFactory() throws ClassNotFoundException {
		Configuration configuration = new Configuration();

		// setup non-default naming strategy
		if (implicitNamingStrategy != null) {
			configuration.setImplicitNamingStrategy(implicitNamingStrategy);
		}
		if (physicalNamingStrategy != null) {
			configuration.setPhysicalNamingStrategy(physicalNamingStrategy);
		}

		// Programmatic configuration
		if (cfgProperties != null) {
			Properties props = new Properties();
			props.putAll(cfgProperties);
			configuration.setProperties(props);
		}
		@SuppressWarnings("unchecked")
		Hashtable<String, String> jndiProperties = Naming.getInitialContextProperties();
		if (jndiProperties != null) {
			// Prefix regular JNDI properties for Hibernate
			for (Map.Entry<String, String> entry : jndiProperties.entrySet()) {
				configuration.setProperty(Environment.JNDI_PREFIX + "." + entry.getKey(), entry.getValue());
			}
		}
		// hibernate.cfg.xml configuration
		if (cfgProperties == null && cfgResourceName == null) {
			configuration.configure();
		} else if (cfgProperties == null && cfgResourceName != null) {
			configuration.configure(cfgResourceName);
		}
		// Mapping metadata
		if (mappingClasses != null) {
			for (String className : mappingClasses) {
				configuration.addAnnotatedClass(Reflections.classForName(className));
			}
		}
		if (mappingFiles != null) {
			for (String fileName : mappingFiles) {
				configuration.addFile(fileName);
			}
		}
		if (mappingJars != null) {
			for (String jarName : mappingJars) {
				configuration.addJar(new File(jarName));
			}
		}
		if (mappingPackages != null) {
			for (String packageName : mappingPackages) {
				configuration.addPackage(packageName);
			}
		}
		if (mappingResources != null) {
			for (String resourceName : mappingResources) {
				configuration.addResource(resourceName);
			}
		}

		configuration.setInterceptor(new HibernateSecurityInterceptor(configuration.getInterceptor()));

		return configuration.buildSessionFactory();
	}

	public String getCfgResourceName() {
		return cfgResourceName;
	}

	public void setCfgResourceName(String cfgFileName) {
		this.cfgResourceName = cfgFileName;
	}

	public void setImplicitNamingStrategy(ImplicitNamingStrategy implicitNamingStrategy) {
		this.implicitNamingStrategy = implicitNamingStrategy;
	}

	public void setPhysicalNamingStrategy(PhysicalNamingStrategy physicalNamingStrategy) {
		this.physicalNamingStrategy = physicalNamingStrategy;
	}

	public Map<String, String> getCfgProperties() {
		return cfgProperties;
	}

	public void setCfgProperties(Map<String, String> cfgProperties) {
		this.cfgProperties = cfgProperties;
	}

	public List<String> getMappingClasses() {
		return mappingClasses;
	}

	public void setMappingClasses(List<String> mappingClasses) {
		this.mappingClasses = mappingClasses;
	}

	public List<String> getMappingFiles() {
		return mappingFiles;
	}

	public void setMappingFiles(List<String> mappingFiles) {
		this.mappingFiles = mappingFiles;
	}

	public List<String> getMappingJars() {
		return mappingJars;
	}

	public void setMappingJars(List<String> mappingJars) {
		this.mappingJars = mappingJars;
	}

	public List<String> getMappingPackages() {
		return mappingPackages;
	}

	public void setMappingPackages(List<String> mappingPackages) {
		this.mappingPackages = mappingPackages;
	}

	public List<String> getMappingResources() {
		return mappingResources;
	}

	public void setMappingResources(List<String> mappingResources) {
		this.mappingResources = mappingResources;
	}

}