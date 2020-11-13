/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.seam.mock;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.jboss.seam.util.IteratorEnumeration;

/**
 * Wraps a ServletContext with own attributes.
 *
 * @author Marek Schmidt
 */
public class ServletContextWrapper implements ServletContext {

	private ServletContext delegate;

	private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

	public ServletContextWrapper(ServletContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration<String>(attributes.keySet().iterator());
	}

	@Override
	public ServletContext getContext(String arg0) {
		return delegate.getContext(arg0);
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public String getInitParameter(String arg0) {
		return delegate.getInitParameter(arg0);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public int getMajorVersion() {
		return delegate.getMajorVersion();
	}

	@Override
	public String getMimeType(String arg0) {
		return delegate.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return delegate.getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return delegate.getNamedDispatcher(arg0);
	}

	@Override
	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return delegate.getRequestDispatcher(arg0);
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return delegate.getResource(arg0);
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return delegate.getResourceAsStream(arg0);
	}

	@Override
	public Set<String> getResourcePaths(String arg0) {
		return delegate.getResourcePaths(arg0);
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@Override
	@Deprecated
	public Servlet getServlet(String arg0) throws ServletException {
		return delegate.getServlet(arg0);
	}

	@Override
	public String getServletContextName() {
		return "Wrap";
	}

	@Override
	@Deprecated
	public Enumeration<String> getServletNames() {
		return delegate.getServletNames();
	}

	@Override
	@Deprecated
	public Enumeration<Servlet> getServlets() {
		return delegate.getServlets();
	}

	@Override
	public void log(String arg0) {
		delegate.log(arg0);
	}

	@Override
	@Deprecated
	public void log(Exception arg0, String arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (value == null) {
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
	}

	@Override
	public int getEffectiveMajorVersion() {
		return delegate.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return delegate.getEffectiveMinorVersion();
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return delegate.setInitParameter(name, value);
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		return delegate.addServlet(servletName, className);
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		return delegate.addServlet(servletName, servlet);
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		return delegate.addServlet(servletName, servletClass);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		return delegate.createServlet(clazz);
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return delegate.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return delegate.getServletRegistrations();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		return delegate.addFilter(filterName, className);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		return delegate.addFilter(filterName, filter);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		return delegate.addFilter(filterName, filterClass);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		return delegate.createFilter(clazz);
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return delegate.getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return delegate.getFilterRegistrations();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return delegate.getSessionCookieConfig();
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		delegate.setSessionTrackingModes(sessionTrackingModes);
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return delegate.getDefaultSessionTrackingModes();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return delegate.getEffectiveSessionTrackingModes();
	}

	@Override
	public void addListener(String className) {
		delegate.addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		delegate.addListener(t);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		delegate.addListener(listenerClass);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		return delegate.createListener(clazz);
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return delegate.getJspConfigDescriptor();
	}

	@Override
	public ClassLoader getClassLoader() {
		return delegate.getClassLoader();
	}

	@Override
	public void declareRoles(String... roleNames) {
		delegate.declareRoles(roleNames);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVirtualServerName() {
		return delegate.getVirtualServerName();
	}
}
