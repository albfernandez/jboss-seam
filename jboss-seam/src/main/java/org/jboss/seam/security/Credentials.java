package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;
import java.io.Serializable;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

@Name("org.jboss.seam.security.credentials")
@Scope(SESSION)
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class Credentials implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_INIT_CREDENTIALS = "org.jboss.seam.security.initCredentials";
	public static final String EVENT_CREDENTIALS_UPDATED = "org.jboss.seam.security.credentialsUpdated";

	private static final LogProvider log = Logging.getLogProvider(Credentials.class);

	private String username;
	private String password;

	private boolean invalid = false;

	private boolean initialized;
	
	public Credentials() {
		super();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public String getUsername() {
		if (!isInitialized() && Events.exists()) {
			setInitialized(true);
			Events.instance().raiseEvent(EVENT_INIT_CREDENTIALS, this);
		}

		return username;
	}

	public void setUsername(String username) {
		if (this.username == null && username == null) {
			return;
		}
		if (this.username == null || !this.username.equals(username)) {
			this.username = username;
			invalid = false;
			if (Events.exists()) {
				Events.instance().raiseEvent(EVENT_CREDENTIALS_UPDATED);
			}
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (this.password == null && password == null) {
			return;
		}
		if (this.password == null || !this.password.equals(password)) {
			this.password = password;
			invalid = false;
			if (Events.exists()) {
				Events.instance().raiseEvent(EVENT_CREDENTIALS_UPDATED);
			}
		}
	}

	public boolean isSet() {
		return getUsername() != null && password != null;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void invalidate() {
		invalid = true;
	}

	public void clear() {
		username = null;
		password = null;
	}

	/**
	* Creates a callback handler that can handle a standard username/password
	* callback, using the username and password properties.
	*/
	public CallbackHandler createCallbackHandler() {
		return new CallbackHandler() {
			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for (Callback call: callbacks) {
					if (call instanceof NameCallback) {
						((NameCallback) call).setName(getUsername());
					} else if (call instanceof PasswordCallback) {
						((PasswordCallback) call).setPassword(getPassword() != null ? getPassword().toCharArray() : null);
					} else {
						if (log.isWarnEnabled()) {
							log.warn("Unsupported callback " + call);
						}
					}
				}
			}
		};
	}
}
