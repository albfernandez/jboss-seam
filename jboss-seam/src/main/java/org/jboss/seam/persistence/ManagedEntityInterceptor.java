package org.jboss.seam.persistence;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.BijectionInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.transaction.Transaction;

/**
 * Swizzles entity references around each invocation, maintaining referential
 * integrity even across passivation of the stateful bean or Seam-managed
 * extended persistence context, and allowing for more efficient replication.
 * 
 * @author Gavin King
 * @author Pete Muir
 * 
 */
@Interceptor(around = BijectionInterceptor.class)
public class ManagedEntityInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 1L;

	private static LogProvider log = Logging.getLogProvider(ManagedEntityInterceptor.class);

	private static ManagedEntityWrapper managedEntityWrapper = new ManagedEntityWrapper();

	private boolean reentrant;
	
	public ManagedEntityInterceptor() {
		super();
	}

	@Override
	@AroundInvoke
	public Object aroundInvoke(InvocationContext ctx) throws Exception {
		if (reentrant) {
			return ctx.proceed();
		} else {
			reentrant = true;
			if (log.isTraceEnabled()) {
				log.trace("Attempting to activate " + getComponent().getName() + " component");
			}
			managedEntityWrapper.deserialize(ctx.getTarget(), getComponent());
			if (log.isDebugEnabled()) {
				log.debug("Activated " + getComponent().getName() + " component");
			}
			try {
				return ctx.proceed();
			} finally {
				if (!isTransactionRolledBackOrMarkedRollback()) {
					if (log.isTraceEnabled()) {
						log.trace("Attempting to passivate " + getComponent().getName() + " component");
					}
					managedEntityWrapper.wrap(ctx.getTarget(), getComponent());
					reentrant = false;
					if (log.isDebugEnabled()) {
						log.debug("Passivated " + getComponent().getName() + " component");
					}
				}
			}
		}
	}

	@Override
	public boolean isInterceptorEnabled() {
		return getComponent().getScope() == CONVERSATION;
	}

	private static boolean isTransactionRolledBackOrMarkedRollback() {
		try {
			return Transaction.instance().isRolledBackOrMarkedRollback();
		} catch (Exception e) {
			return false;
		}
	}

}
