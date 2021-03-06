package org.jboss.seam.security;

import static org.jboss.seam.security.EntityAction.DELETE;
import static org.jboss.seam.security.EntityAction.INSERT;
import static org.jboss.seam.security.EntityAction.READ;
import static org.jboss.seam.security.EntityAction.UPDATE;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.jboss.seam.Entity.NotEntityException;

/**
 * Facilitates security checks for Hibernate entities
 * 
 * @author Shane Bryzak
 *
 */
public class HibernateSecurityInterceptor extends EmptyInterceptor {
	private static final long serialVersionUID = 1L;
	private Interceptor wrappedInterceptor;

	public HibernateSecurityInterceptor(Interceptor wrappedInterceptor) {
		super();
		this.wrappedInterceptor = wrappedInterceptor;
	}

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			EntityPermissionChecker.instance().checkEntityPermission(entity, READ);
		} catch (NotEntityException ignored) {
			// Not a JPA entity
		}

		return wrappedInterceptor != null && wrappedInterceptor.onLoad(entity, id, state, propertyNames, types);
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			EntityPermissionChecker.instance().checkEntityPermission(entity, DELETE);
		} catch (NotEntityException ignored) {
			// Not a JPA entity
		}

		if (wrappedInterceptor != null) {
			wrappedInterceptor.onDelete(entity, id, state, propertyNames, types);
		}
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames,
			Type[] types) {
		try {
			EntityPermissionChecker.instance().checkEntityPermission(entity, UPDATE);
		} catch (NotEntityException ignored) {
			// Not a JPA entity
		}

		return wrappedInterceptor != null && wrappedInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			EntityPermissionChecker.instance().checkEntityPermission(entity, INSERT);
		} catch (NotEntityException ignored) {
			// Not a JPA entity
		}

		return wrappedInterceptor != null && wrappedInterceptor.onSave(entity, id, state, propertyNames, types);
	}
}
