package org.jboss.seam.transaction;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;

import javax.persistence.EntityManager;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;

/**
 * Base implementation of UserTransaction
 * 
 * @author Gavin King
 * 
 */
public abstract class AbstractUserTransaction implements UserTransaction {

	protected AbstractUserTransaction() {
		super();
	}
	
	@Override
	public boolean isActive() throws SystemException {
		return getStatus() == STATUS_ACTIVE;
	}

	@Override
	public boolean isActiveOrMarkedRollback() throws SystemException {
		int status = getStatus();
		return status == STATUS_ACTIVE || status == STATUS_MARKED_ROLLBACK;
	}

	@Override
	public boolean isRolledBackOrMarkedRollback() throws SystemException {
		int status = getStatus();
		return status == STATUS_ROLLEDBACK || status == STATUS_MARKED_ROLLBACK;
	}

	@Override
	public boolean isMarkedRollback() throws SystemException {
		return getStatus() == STATUS_MARKED_ROLLBACK;
	}

	@Override
	public boolean isNoTransaction() throws SystemException {
		return getStatus() == STATUS_NO_TRANSACTION;
	}

	@Override
	public boolean isRolledBack() throws SystemException {
		return getStatus() == STATUS_ROLLEDBACK;
	}

	@Override
	public boolean isCommitted() throws SystemException {
		return getStatus() == STATUS_COMMITTED;
	}

	@Override
	public boolean isConversationContextRequired() {
		return false;
	}

	@Override
	public abstract void registerSynchronization(Synchronization sync);

	@Override
	public void enlist(EntityManager entityManager) throws SystemException {
		if (isActiveOrMarkedRollback()) {
			entityManager.joinTransaction();
		}
	}

	public static Synchronizations getSynchronizations() {
		return (Synchronizations) Component.getInstance("org.jboss.seam.transaction.synchronizations", ScopeType.EVENT);
	}

}
