/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.intercept;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.util.EJB;

/**
 * Abstract superclass of all controller interceptors
 * 
 * @author Gavin King
 */
public class RootInterceptor implements Serializable {
	private static final long serialVersionUID = 8041533870186694663L;
	private static final LogProvider log = Logging.getLogProvider(RootInterceptor.class);

	private final InterceptorType type;
	private boolean isSeamComponent;
	private String componentName;
	private List<Object> userInterceptors;
	
	 //a cache of the Component reference for performance
	private transient Component component;
	
	private static final Constructor CONSTRUCTOR;

	protected RootInterceptor(InterceptorType type) {
		this.type = type;
	}

	protected void init(Component component) {
		isSeamComponent = true;
		componentName = component.getName();
		userInterceptors = component.createUserInterceptors(type);
		this.component = component;
	}

	protected void initNonSeamComponent() {
		isSeamComponent = false;
	}

	protected void postConstruct(Object bean) {
		// initialize the bean instance
		if (isSeamComponent) {
			try {
				getComponent().initialize(bean);
			} catch (Exception e) {
				throw new RuntimeException("exception initializing EJB component", e);
			}
		}
	}

	protected void invokeAndHandle(InvocationContext invocation, EventType invocationType) {
		try {
			invoke(invocation, invocationType);
		} catch (Exception e) {
			throw new RuntimeException("exception in EJB lifecycle callback", e);
		}
	}

	protected Object invoke(InvocationContext invocation, EventType invocationType) throws Exception {
		String methodName = "";
		if (invocation.getMethod() != null) {
			methodName = invocation.getMethod().getName();
		}
		int paramCount = 0;
		if (invocation.getParameters() != null) {
			paramCount = invocation.getParameters().length;
		}
		if (!isSeamComponent || !Lifecycle.isApplicationInitialized()) {
			//not a Seam component
			return invocation.proceed();
		} else if (Contexts.isEventContextActive() || Contexts.isApplicationContextActive()) {
			//not sure about the second bit (only needed at init time!)

			// a Seam component, and Seam contexts exist
			return createInvocationContext(invocation, invocationType).proceed();
		} else if (paramCount == 0 && ("hashCode".equals(methodName) || "toString".equals(methodName))) {
			// if no context is active, for hashCode and toString proceed
			return invocation.proceed();
		} else if (Lifecycle.isApplicationInitialized() && Lifecycle.getApplication() != null)  {
			//if invoked outside of a set of Seam contexts,
			//set up temporary Seam EVENT and APPLICATION
			//contexts just for this call
			// Only call if we're sure we can create the context
			Lifecycle.beginCall();
			try {
				return createInvocationContext(invocation, invocationType).proceed();
			} finally {
				Lifecycle.endCall();
			}			
		}
		else {
			// We need the context but we have no one.
			throw new IllegalArgumentException("trying to use interceptors but no Seam context can be created");
		}
	}

	private InvocationContext createInvocationContext(InvocationContext invocation, EventType eventType) throws Exception {
		if (isProcessInterceptors(invocation.getMethod(), invocation.getTarget())) {
			if (log.isTraceEnabled()) {
				log.trace("intercepted: " + getInterceptionMessage(invocation, eventType));
			}
			return createSeamInvocationContext(invocation, eventType);
		} else {
			if (log.isTraceEnabled()) {
				log.trace("not intercepted: " + getInterceptionMessage(invocation, eventType));
			}
			return invocation;
		}
	}

	private SeamInvocationContext createSeamInvocationContext(InvocationContext invocation, EventType eventType) throws Exception {
		return EJB.INVOCATION_CONTEXT_AVAILABLE ? createEE5SeamInvocationContext(invocation, eventType)
				: createNonEE5SeamInvocationContext(invocation, eventType);
	}

	private SeamInvocationContext createNonEE5SeamInvocationContext(InvocationContext invocation, EventType eventType) {
		return new SeamInvocationContext(invocation, eventType, userInterceptors, getComponent().getInterceptors(type));
	}


	static {
		if (EJB.INVOCATION_CONTEXT_AVAILABLE) {
			try {
				Class[] paramTypes = { InvocationContext.class, EventType.class, List.class, List.class };
				CONSTRUCTOR = Class.forName("org.jboss.seam.intercept.EE5SeamInvocationContext").getConstructor(paramTypes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			CONSTRUCTOR = null;
		}

	}

	private SeamInvocationContext createEE5SeamInvocationContext(InvocationContext invocation, EventType eventType) {
		try {
			return (SeamInvocationContext) CONSTRUCTOR.newInstance(invocation, eventType, userInterceptors,
					getComponent().getInterceptors(type));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getInterceptionMessage(InvocationContext invocation, EventType eventType) {
		return getComponent().getName() + '.' + (eventType == EventType.AROUND_INVOKE ? invocation.getMethod().getName() : eventType);
	}

	private boolean isProcessInterceptors(Method method, Object bean) {
		return isSeamComponent && getComponent().isInterceptionEnabled() && !isBypassed(method) && !isClearDirtyMethod(method, bean);
	}

	private boolean isBypassed(Method method) {
		return method != null && method.isAnnotationPresent(BypassInterceptors.class);
	}

	private boolean isClearDirtyMethod(Method method, Object bean) {
		return bean instanceof Mutable && method != null && "clearDirty".equals(method.getName()) && method.getParameterTypes().length == 0;
	}

	protected Component getComponent() {
		if (isSeamComponent && component == null) {
			component = Seam.componentForName(componentName);
		}
		return component;
	}

	protected boolean isSeamComponent() {
		return isSeamComponent;
	}

	protected String getComponentName() {
		return componentName;
	}

}
