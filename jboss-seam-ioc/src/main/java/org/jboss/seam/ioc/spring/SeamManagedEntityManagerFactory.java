package org.jboss.seam.ioc.spring;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.jboss.seam.Component;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.springframework.util.ClassUtils;

/**
 * An EntityManagerFactory that defers creation and management of an
 * EntityManager to a Seam ManagedPersistenceContext.
 *
 * @author Mike Youngstrom
 */
public class SeamManagedEntityManagerFactory implements EntityManagerFactory, Serializable {
	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(SeamManagedEntityManagerFactory.class);

	private String persistenceContextName;

	private boolean closed = false;

	public SeamManagedEntityManagerFactory(String seamPersistenceContextName) {
		if (seamPersistenceContextName == null || "".equals(seamPersistenceContextName)) {
			throw new IllegalArgumentException("persistenceContextName cannot be null");
		}
		this.persistenceContextName = seamPersistenceContextName;
	}

	@Override
	public void close() {
		closed = true;
	}

	/**
	* Wraps the Seam ManagedPersistenceContext in a close suppressing proxy and
	* returns.
	*/
	@Override
	public EntityManager createEntityManager() {
		if (closed) {
			throw new IllegalStateException("EntityManagerFactory is closed");
		}
		log.debug("Returning a Seam Managed PC from createEntityManager()");
		SeamLifecycleUtils.beginTransactionalSeamCall();
		EntityManager em = (EntityManager) Component.getInstance(persistenceContextName);
		//Creating Proxy of EntityManager to ensure we implement all the interfaces
		return (EntityManager) Proxy.newProxyInstance(getClass().getClassLoader(), ClassUtils.getAllInterfaces(em),
				new SeamManagedPersistenceContextHandler(em));
	}

	@Override
	public EntityManager createEntityManager(Map properties) {
		// Not really sure if I should throw an exception here or just ignore the Map
		throw new UnsupportedOperationException("Cannot change properties of a Seam ManagedPersistenceContext this way.  "
				+ "This must be done on the ManagedPersistenceContext seam component.");
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	/**
	* EntityManager InvocationHandler used to correctly calls to close and
	* isOpen. We don't want Spring closing the SeamEntityManager only this
	* proxy.
	*
	* @author Mike Youngstrom
	*
	*/
	public static class SeamManagedPersistenceContextHandler implements InvocationHandler, Serializable {
		private static final long serialVersionUID = 1L;

		private static final LogProvider log = Logging.getLogProvider(SeamManagedPersistenceContextHandler.class);

		private EntityManager delegate;

		private boolean closed = false;

		public SeamManagedPersistenceContextHandler(EntityManager delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("equals".equals(method.getName())) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			if ("hashCode".equals(method.getName())) {
				// Use hashCode of EntityManager proxy.
				return Integer.valueOf(hashCode());
			}
			if ("isOpen".equals(method.getName())) {
				return delegate.isOpen() && !closed;
			}
			if (!delegate.isOpen()) {
				// Defer to delegate error if it's closed.
				try {
					return method.invoke(delegate, args);
				} catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
			if (closed) {
				throw new IllegalStateException("This PersistenceContext is closed.");
			}
			if ("close".equals(method.getName())) {
				log.debug("Closing PersistenceContext Proxy.");
				closed = true;
				return null;
			}

			try {
				return method.invoke(delegate, args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cache getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNamedQuery(String name, Query query) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub

	}
}
