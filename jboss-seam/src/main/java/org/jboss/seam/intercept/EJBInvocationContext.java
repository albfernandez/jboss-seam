package org.jboss.seam.intercept;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Wraps the invocation context coming from EJB3, and
 * adapts it to the Seam version of the API.
 *
 * @author Gavin King
 *
 */
class EJBInvocationContext implements InvocationContext, javax.interceptor.InvocationContext {
	private javax.interceptor.InvocationContext context;

	public EJBInvocationContext(javax.interceptor.InvocationContext context) {
		this.context = context;
	}

	@Override
	public Map getContextData() {
		return context.getContextData();
	}

	@Override
	public Method getMethod() {
		return context.getMethod();
	}

	@Override
	public Object[] getParameters() {
		return context.getParameters();
	}

	@Override
	public Object getTarget() {
		return context.getTarget();
	}

	@Override
	public Object proceed() throws Exception {
		return context.proceed();
	}

	@Override
	public void setParameters(Object[] params) {
		context.setParameters(params);
	}

	@Override
	public Object getTimer() {
		return context.getTimer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Constructor<?> getConstructor() {
		// TODO Auto-generated method stub
		return null;
	}
}
