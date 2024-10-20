/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.seam.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


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
import org.jboss.seam.util.CollectionsUtils;

/**
 * Mock implementation of the {@link javax.servlet.http.HttpServletRequest}
 * interface. Supports the Servlet 2.4 API level.
 * <p/>
 * <p>Used for testing the web framework; also useful for testing
 * application controllers.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rick Evans
 * @author Mark Fisher
 * @since 1.0.2
 */
public class EnhancedMockHttpServletRequest implements HttpServletRequest {

	/**
	* The default protocol: 'http'.
	*/
	public static final String DEFAULT_PROTOCOL = "http";

	/**
	* The default server address: '127.0.0.1'.
	*/
	public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

	/**
	* The default server name: 'localhost'.
	*/
	public static final String DEFAULT_SERVER_NAME = "localhost";

	/**
	* The default server port: '80'.
	*/
	public static final int DEFAULT_SERVER_PORT = 80;

	/**
	* The default remote address: '127.0.0.1'.
	*/
	public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

	/**
	* The default remote host: 'localhost'.
	*/
	public static final String DEFAULT_REMOTE_HOST = "localhost";

	private boolean active = true;

	//---------------------------------------------------------------------
	// ServletRequest properties
	//---------------------------------------------------------------------

	private final Map<String, Object> attributes = new HashMap<String, Object>();

	private String characterEncoding;

	private byte[] content;

	private String contentType;

	private final Map parameters = new LinkedHashMap(16);

	private String protocol = DEFAULT_PROTOCOL;

	private String scheme = DEFAULT_PROTOCOL;

	private String serverName = DEFAULT_SERVER_NAME;

	private int serverPort = DEFAULT_SERVER_PORT;

	private String remoteAddr = DEFAULT_REMOTE_ADDR;

	private String remoteHost = DEFAULT_REMOTE_HOST;

	/**
	* List of locales in descending order
	*/
	private final List<Locale> locales = new ArrayList<Locale>();

	private boolean secure = false;

	private final ServletContext servletContext;

	private int remotePort = DEFAULT_SERVER_PORT;

	private String localName = DEFAULT_SERVER_NAME;

	private String localAddr = DEFAULT_SERVER_ADDR;

	private int localPort = DEFAULT_SERVER_PORT;

	//---------------------------------------------------------------------
	// HttpServletRequest properties
	//---------------------------------------------------------------------

	private String authType;

	private Cookie[] cookies;

	/**
	* The key is the lowercase header name; the value is a {@link org.jboss.seam.mock.HeaderValueHolder} object.
	*/
	private final Map<String, HeaderValueHolder> headers = new HashMap<String, HeaderValueHolder>();

	private String method;

	private String pathInfo;

	private String contextPath = "";

	private String queryString;

	private Map<String, String> queryParameters = new HashMap<String, String>();

	private String remoteUser;

	private Set<String> userRoles = new HashSet<String>();

	private Principal userPrincipal;

	private String requestURI;

	private String servletPath = "";

	private HttpSession session;

	private boolean requestedSessionIdValid = true;

	private boolean requestedSessionIdFromCookie = true;

	private boolean requestedSessionIdFromURL = false;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	* Create a new MockHttpServletRequest with a default
	* {@link MockServletContext}.
	*
	* @see MockServletContext
	*/
	public EnhancedMockHttpServletRequest() {
		this(null, "", "");
	}

	/**
	* Create a new MockHttpServletRequest with a default
	* {@link MockServletContext}.
	*
	* @param method     the request method (may be <code>null</code>)
	* @param requestURI the request URI (may be <code>null</code>)
	* @see #setMethod
	* @see #setRequestURI
	* @see MockServletContext
	*/
	public EnhancedMockHttpServletRequest(String method, String requestURI) {
		this(null, method, requestURI);
	}

	/**
	* Create a new MockHttpServletRequest.
	*
	* @param servletContext the ServletContext that the request runs in
	*                       (may be <code>null</code> to use a default MockServletContext)
	* @see MockServletContext
	*/
	public EnhancedMockHttpServletRequest(ServletContext servletContext) {
		this(servletContext, "", "");
	}

	/**
	* Create a new MockHttpServletRequest.
	*
	* @param servletContext the ServletContext that the request runs in
	*                       (may be <code>null</code> to use a default MockServletContext)
	* @param method         the request method (may be <code>null</code>)
	* @param requestURI     the request URI (may be <code>null</code>)
	* @see #setMethod
	* @see #setRequestURI
	* @see MockServletContext
	*/
	public EnhancedMockHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
		this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
		this.method = method;
		this.requestURI = requestURI;
		this.locales.add(Locale.ENGLISH);

		// Old mock: The 1.2 RI NPEs if this header isn't present
		addHeader("Accept", new String[0]);
	}

	// OLD CONSTRUCTORS FROM < SEAM 2.2 MOCKS!

	public EnhancedMockHttpServletRequest(HttpSession session) {
		this(session, null, new HashSet<String>());
	}

	public EnhancedMockHttpServletRequest(HttpSession session, String principalName, Set<String> principalRoles) {
		this(session, principalName, principalRoles, new Cookie[] {}, null);
	}

	public EnhancedMockHttpServletRequest(HttpSession session, final String principalName, Set<String> principalRoles, Cookie[] cookies,
			String method) {
		this(null, method, "");
		this.session = session;

		this.userPrincipal = principalName == null ? null : new Principal() {
			@Override
			public String getName() {
				return principalName;
			}
		};

		this.userRoles = principalRoles;
		this.cookies = CloneUtils.cloneArray(cookies);

		// Old mock: The 1.2 RI NPEs if this header isn't present
		addHeader("Accept", new String[0]);
	}

	//---------------------------------------------------------------------
	// Lifecycle methods
	//---------------------------------------------------------------------

	/**
	* Return the ServletContext that this request is associated with.
	* (Not available in the standard HttpServletRequest interface for some reason.)
	*/
	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	* Return whether this request is still active (that is, not completed yet).
	*/
	public boolean isActive() {
		return this.active;
	}

	/**
	* Mark this request as completed, keeping its state.
	*/
	public void close() {
		this.active = false;
	}

	/**
	* Invalidate this request, clearing its state.
	*/
	public void invalidate() {
		close();
		clearAttributes();
	}

	/**
	* Check whether this request is still active (that is, not completed yet),
	* throwing an IllegalStateException if not active anymore.
	*/
	protected void checkActive() throws IllegalStateException {
		if (!this.active) {
			throw new IllegalStateException("Request is not active anymore");
		}
	}

	//---------------------------------------------------------------------
	// ServletRequest interface
	//---------------------------------------------------------------------

	@Override
	public Object getAttribute(String name) {
		checkActive();
		return this.attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		checkActive();
		return Collections.enumeration(this.attributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public int getContentLength() {
		return (this.content != null ? this.content.length : -1);
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.content != null) {
			return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
		} else {
			return null;
		}
	}

	/**
	* Set a single value for the specified HTTP parameter.
	* <p>If there are already one or more values registered for the given
	* parameter name, they will be replaced.
	*/
	public void setParameter(String name, String value) {
		setParameter(name, new String[] { value });
	}

	/**
	* Set an array of values for the specified HTTP parameter.
	* <p>If there are already one or more values registered for the given
	* parameter name, they will be replaced.
	*/
	public void setParameter(String name, String[] values) {
		this.parameters.put(name, values);
	}

	/**
	* Sets all provided parameters <emphasis>replacing</emphasis> any
	* existing values for the provided parameter names. To add without
	* replacing existing values, use {@link #addParameters(java.util.Map)}.
	*/
	public void setParameters(Map params) {
		for (Map.Entry entry: ((Map<?,?>)params).entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				this.setParameter((String) key, (String) value);
			} else if (value instanceof String[]) {
				this.setParameter((String) key, (String[]) value);
			} else {
				throw new IllegalArgumentException(
						"Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
			}
		}
	}

	/**
	* Add a single value for the specified HTTP parameter.
	* <p>If there are already one or more values registered for the given
	* parameter name, the given value will be added to the end of the list.
	*/
	public void addParameter(String name, String value) {
		addParameter(name, new String[] { value });
	}

	/**
	* Add an array of values for the specified HTTP parameter.
	* <p>If there are already one or more values registered for the given
	* parameter name, the given values will be added to the end of the list.
	*/
	public void addParameter(String name, String[] values) {
		String[] oldArr = (String[]) this.parameters.get(name);
		if (oldArr != null) {
			String[] newArr = new String[oldArr.length + values.length];
			System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
			System.arraycopy(values, 0, newArr, oldArr.length, values.length);
			this.parameters.put(name, newArr);
		} else {
			this.parameters.put(name, values);
		}
	}

	/**
	* Adds all provided parameters <emphasis>without</emphasis> replacing
	* any existing values. To replace existing values, use
	* {@link #setParameters(java.util.Map)}.
	*/
	public void addParameters(Map params) {
		for (Map.Entry entry: ((Map<?,?>)params).entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				this.addParameter((String) key, (String) value);
			} else if (value instanceof String[]) {
				this.addParameter((String) key, (String[]) value);
			} else {
				throw new IllegalArgumentException(
						"Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
			}
		}
	}

	/**
	* Remove already registered values for the specified HTTP parameter, if any.
	*/
	public void removeParameter(String name) {
		this.parameters.remove(name);
	}

	/**
	* Removes all existing parameters.
	*/
	public void removeAllParameters() {
		this.parameters.clear();
	}

	@Override
	public String getParameter(String name) {
		String[] arr = (String[]) this.parameters.get(name);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return (String[]) this.parameters.get(name);
	}

	@Override
	public Map getParameterMap() {
		return this.parameters;
	}

	// Old mock
	public Map<String, String[]> getParameters() {
		return parameters;
	}

	/**
	* Add a query parameter that will be appended to the URI query string.
	*/
	public void addQueryParameter(String name, String value) {
		addParameter(name, value);
		this.queryParameters.put(name, value);
	}

	public void removeQueryParameter(String name) {
		removeParameter(name);
		this.queryParameters.remove(name);
	}

	public Map<String, String> getQueryParameters() {
		return queryParameters;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String getProtocol() {
		return this.protocol;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Override
	public String getScheme() {
		return this.scheme;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public String getServerName() {
		return this.serverName;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	@Override
	public int getServerPort() {
		return this.serverPort;
	}

	@Override
	public BufferedReader getReader() throws UnsupportedEncodingException {
		if (this.content != null) {
			InputStream sourceStream = new ByteArrayInputStream(this.content);
			Reader sourceReader = (this.characterEncoding != null) ? new InputStreamReader(sourceStream, this.characterEncoding)
					: new InputStreamReader(sourceStream);
			return new BufferedReader(sourceReader);
		} else {
			return null;
		}
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	@Override
	public String getRemoteAddr() {
		return this.remoteAddr;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	@Override
	public String getRemoteHost() {
		return this.remoteHost;
	}

	@Override
	public void setAttribute(String name, Object value) {
		checkActive();
		if (value != null) {
			this.attributes.put(name, value);
		} else {
			this.attributes.remove(name);
		}
	}

	@Override
	public void removeAttribute(String name) {
		checkActive();
		this.attributes.remove(name);
	}

	/**
	* Clear all of this request's attributes.
	*/
	public void clearAttributes() {
		this.attributes.clear();
	}

	/**
	* Add a new preferred locale, before any existing locales.
	*/
	public void addPreferredLocale(Locale locale) {
		this.locales.add(0, locale);
	}

	@Override
	public Locale getLocale() {
		return this.locales.get(0);
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return Collections.enumeration(this.locales);
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Override
	public boolean isSecure() {
		return this.secure;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return new MockRequestDispatcher(path);
	}

	@Override
	@Deprecated
	public String getRealPath(String path) {
		return this.servletContext.getRealPath(path);
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	@Override
	public int getRemotePort() {
		return this.remotePort;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	@Override
	public String getLocalName() {
		return this.localName;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	@Override
	public String getLocalAddr() {
		return this.localAddr;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	@Override
	public int getLocalPort() {
		return this.localPort;
	}

	//---------------------------------------------------------------------
	// HttpServletRequest interface
	//---------------------------------------------------------------------

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	@Override
	public String getAuthType() {
		return this.authType;
	}

	public void setCookies(Cookie[] cookies) {
		this.cookies = CloneUtils.cloneArray(cookies);
	}

	@Override
	public Cookie[] getCookies() {
		return CloneUtils.cloneArray(this.cookies);
	}

	public void addCookie(Cookie cookie) {
		this.cookies = new Cookie[this.cookies.length + 1];
		this.cookies[this.cookies.length - 1] = cookie;
	}

	/**
	* Add a header entry for the given name.
	* <p>If there was no entry for that header name before,
	* the value will be used as-is. In case of an existing entry,
	* a String array will be created, adding the given value (more
	* specifically, its toString representation) as further element.
	* <p>Multiple values can only be stored as list of Strings,
	* following the Servlet spec (see <code>getHeaders</code> accessor).
	* As alternative to repeated <code>addHeader</code> calls for
	* individual elements, you can use a single call with an entire
	* array or Collection of values as parameter.
	*
	* @see #getHeaderNames
	* @see #getHeader
	* @see #getHeaders
	* @see #getDateHeader
	* @see #getIntHeader
	*/
	public void addHeader(String name, Object value) {
		HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
		if (header == null) {
			header = new HeaderValueHolder();
			this.headers.put(name, header);
		}
		if (value instanceof Collection) {
			header.addValues((Collection) value);
		} else if (value.getClass().isArray()) {
			header.addValueArray(value);
		} else {
			header.addValue(value);
		}
	}

	@Override
	public long getDateHeader(String name) {
		HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
		Object value = (header != null ? header.getValue() : null);
		if (value instanceof Date) {
			return ((Date) value).getTime();
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value != null) {
			throw new IllegalArgumentException("Value for header '" + name + "' is neither a Date nor a Number: " + value);
		} else {
			return -1L;
		}
	}

	@Override
	public String getHeader(String name) {
		HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
		return (header != null ? header.getValue().toString() : null);
	}

	public Map<String, String[]> getHeaders() {
		Map<String, String[]> headerValues = CollectionsUtils.newHashMap(this.headers.size());
		for (Map.Entry<String, HeaderValueHolder> entry : this.headers.entrySet()) {
			String[] values = (String[]) entry.getValue().getValues().toArray(new String[0]);
			headerValues.put(entry.getKey(), values);
		}

		return headerValues;
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
		return Collections.enumeration(header != null ? header.getValues() : Collections.emptyList());
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public int getIntHeader(String name) {
		HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
		Object value = (header != null ? header.getValue() : null);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			return Integer.parseInt((String) value);
		} else if (value != null) {
			throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
		} else {
			return -1;
		}
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	@Override
	public String getPathTranslated() {
		return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public String getContextPath() {
		return this.contextPath != null ? this.contextPath : "/project"; // Old mock default
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	@Override
	public String getQueryString() {
		if (!getQueryParameters().isEmpty()) {
			StringBuilder q = new StringBuilder(queryString);
			if (!queryString.endsWith("&")) {
				q.append('&');
			}
			for (Map.Entry<String, String> entry : getQueryParameters().entrySet()) {
				q.append(entry.getKey());
				q.append('=');
				q.append(entry.getValue());
				q.append('&');
			}
			if (q.toString().endsWith("&")) {
				q.deleteCharAt(q.length() - 1);
			}
			return q.toString();
		}
		return this.queryString;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Override
	public String getRemoteUser() {
		return this.remoteUser;
	}

	/**
	* @see #addUserRole
	* @deprecated in favor of addUserRole
	*/
	@Deprecated
	public void addRole(String role) {
		addUserRole(role);
	}

	public void addUserRole(String role) {
		this.userRoles.add(role);
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.userRoles.contains(role);
	}

	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.userPrincipal;
	}

	@Override
	public String getRequestedSessionId() {
		HttpSession session = getSession();
		return (session != null ? session.getId() : null);
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	@Override
	public String getRequestURI() {
		return this.requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer(this.scheme);
		url.append("://").append(this.serverName).append(':').append(this.serverPort).append(getRequestURI());
		return url;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	@Override
	public String getServletPath() {
		return this.servletPath;
	}

	public void setSession(HttpSession session) {
		/*
		 TODO: We don't track access times in mocks (yet) mockSession.access();
		if (session instanceof MockHttpSession) {
		    MockHttpSession mockSession = ((MockHttpSession) session);
		    ...
		}
		*/
		this.session = session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		checkActive();
		// Reset session if invalidated.
		if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
			this.session = null;
		}
		// Create new session if necessary.
		if (this.session == null && create) {
			this.session = new MockHttpSession(this.servletContext);
		}
		return this.session;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
		this.requestedSessionIdValid = requestedSessionIdValid;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionIdValid;
	}

	public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
		this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.requestedSessionIdFromCookie;
	}

	public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
		this.requestedSessionIdFromURL = requestedSessionIdFromURL;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return this.requestedSessionIdFromURL;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	public boolean isAllParametersInQueryString() {
		return true;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getContentLengthLong() {
		return (this.content != null ? this.content.length : -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String changeSessionId() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}