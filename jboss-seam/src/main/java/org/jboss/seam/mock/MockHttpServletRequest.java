/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.jboss.seam.util.CloneUtils;
import org.jboss.seam.util.IteratorEnumeration;

/**
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 */
public class MockHttpServletRequest implements HttpServletRequest {

	private Map<String, String[]> parameters = new HashMap<String, String[]>();
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private HttpSession session;
	private Map<String, String[]> headers = new HashMap<String, String[]>();
	private String principalName;
	private Set<String> principalRoles;
	private Cookie[] cookies;
	private String method;
	private HttpServletRequest httpServletRequest;
	private String authType;
	private String pathInfo;
	private String pathTranslated;
	private String contextPath;
	private String queryString;
	private String requestedSessionId;
	private String requestURI;
	private StringBuffer requestURL;
	private String servletPath;
	private String characterEncoding;
	private int contentLength;
	private String contentType;
	private ServletInputStream inputStream;
	private String protocol;
	private String scheme;
	private String serverName;
	private int serverPort;
	private BufferedReader reader;
	private String remoteAddr;
	private String remoteHost;
	private Locale locale;
	private Enumeration<Locale> locales;
	private boolean isSecure;
	private int remotePort;
	private String localName;
	private String localAddr;
	private int localPort;

	public MockHttpServletRequest(HttpSession session) {
		this(session, null, new HashSet<String>());
	}

	public MockHttpServletRequest(HttpSession session, ExternalContext externalContext) {
		this(session, null, new HashSet<String>());
		if (externalContext == null) {
			return;
		}
		Object request = externalContext.getRequest();
		if (request instanceof HttpServletRequest) {
			httpServletRequest = (HttpServletRequest) request;
			authType = httpServletRequest.getAuthType();
			pathInfo = httpServletRequest.getPathInfo();
			pathTranslated = httpServletRequest.getPathTranslated();
			contextPath = httpServletRequest.getContextPath();
			queryString = httpServletRequest.getQueryString();
			requestedSessionId = httpServletRequest.getRequestedSessionId();
			requestURI = httpServletRequest.getRequestURI();
			requestURL = httpServletRequest.getRequestURL();
			servletPath = httpServletRequest.getServletPath();
			characterEncoding = httpServletRequest.getCharacterEncoding();
			contentLength = httpServletRequest.getContentLength();
			contentType = httpServletRequest.getContentType();
			protocol = httpServletRequest.getProtocol();
			scheme = httpServletRequest.getScheme();
			serverName = httpServletRequest.getServerName();
			serverPort = httpServletRequest.getServerPort();
			remoteAddr = httpServletRequest.getRemoteAddr();
			remoteHost = httpServletRequest.getRemoteHost();
			locale = httpServletRequest.getLocale();
			locales = httpServletRequest.getLocales();
			isSecure = httpServletRequest.isSecure();
			remotePort = httpServletRequest.getRemotePort();
			localName = httpServletRequest.getLocalName();
			localAddr = httpServletRequest.getLocalAddr();
			localPort = httpServletRequest.getLocalPort();
		}
	}

	public MockHttpServletRequest(HttpSession session, String principalName, Set<String> principalRoles) {
		this(session, principalName, principalRoles, new Cookie[] {}, null);
	}

	public MockHttpServletRequest(HttpSession session, String principalName, Set<String> principalRoles, Cookie[] cookies, String method) {
		this.session = session;
		this.principalName = principalName;
		this.principalRoles = principalRoles;
		this.cookies = cookies;
		this.method = method;
		// The 1.2 RI NPEs if this header isn't present
		headers.put("Accept", new String[0]);
		List<Locale> list = Collections.emptyList();
		locales = new IteratorEnumeration<Locale>(list.iterator());
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getAuthType() {
		return authType;
	}

	@Override
	public Cookie[] getCookies() {
		return CloneUtils.cloneArray(cookies);
	}

	@Override
	public long getDateHeader(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHeader(String header) {
		String[] values = headers.get(header);
		return values == null || values.length == 0 ? null : values[0];
	}

	@Override
	public Enumeration<String> getHeaders(String header) {
		return new IteratorEnumeration<String>(Arrays.asList(headers.get(header)).iterator());
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new IteratorEnumeration<String>(headers.keySet().iterator());
	}

	@Override
	public int getIntHeader(String header) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return pathTranslated;
	}

	@Override
	public String getContextPath() {
		return (contextPath != null ? contextPath : "/project");
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return principalName;
	}

	@Override
	public boolean isUserInRole(String role) {
		return principalRoles.contains(role);
	}

	@Override
	public Principal getUserPrincipal() {
		return principalName == null ? null : new Principal() {
			@Override
			public String getName() {
				return principalName;
			}
		};
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return (requestURI != null ? requestURI : "http://localhost:8080/myproject/page.seam");
	}

	@Override
	public StringBuffer getRequestURL() {
		return (requestURL != null ? requestURL : new StringBuffer(getRequestURI()));
	}

	@Override
	public String getServletPath() {
		return (servletPath != null ? servletPath : "/page.seam");
	}

	@Override
	public HttpSession getSession(boolean create) {
		return session;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public Object getAttribute(String att) {
		return attributes.get(att);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration<String>(attributes.keySet().iterator());
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		// TODO

	}

	@Override
	public int getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public String getParameter(String param) {
		String[] values = parameters.get(param);
		return values == null || values.length == 0 ? null : values[0];
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new IteratorEnumeration<String>(parameters.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String param) {
		return parameters.get(param);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return reader;
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return remoteHost;
	}

	@Override
	public void setAttribute(String att, Object value) {
		if (value == null) {
			attributes.remove(att);
		} else {
			attributes.put(att, value);
		}
	}

	@Override
	public void removeAttribute(String att) {
		attributes.remove(att);
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return locales;
	}

	@Override
	public boolean isSecure() {
		return isSecure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		if (httpServletRequest != null) {
			return httpServletRequest.getRequestDispatcher(path);
		}
		return null;
	}

	@Override
	@Deprecated
	public String getRealPath(String path) {
		if (httpServletRequest != null) {
			return httpServletRequest.getRealPath(path);
		}
		return null;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getLocalAddr() {
		return localAddr;
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}

	public Map<String, String[]> getHeaders() {
		return headers;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getContentLengthLong() {
		return getContentLength();
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

}
