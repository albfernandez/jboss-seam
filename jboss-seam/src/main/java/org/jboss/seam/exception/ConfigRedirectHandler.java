package org.jboss.seam.exception;

import javax.faces.application.FacesMessage.Severity;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

/**
 * Implements &lt;redirect/&gt; for pages.xml
 * 
 * @author Gavin King
 *
 */
public final class ConfigRedirectHandler extends RedirectHandler {
	private final ValueExpression<String> id;
	private final Class<?> clazz;
	private final boolean conversation;
	private final String message;
	private final Severity messageSeverity;
	private final boolean conversationBeforeRedirect;

	/**
	* Construct a ConfigRedirectHandler.
	* 
	*/
	public ConfigRedirectHandler(ValueExpression<String> id, Class clazz, boolean conversation, boolean conversationBeforeRedirect,
			String message, Severity messageSeverity) {
		super();
		this.id = id;
		this.clazz = clazz;
		this.conversation = conversation;
		this.conversationBeforeRedirect = conversationBeforeRedirect;
		this.message = message;
		this.messageSeverity = messageSeverity;
	}

	@Deprecated
	public ConfigRedirectHandler(String id, Class<?> clazz, boolean conversation, String message, Severity messageSeverity) {
		this(Expressions.instance().createValueExpression(id, String.class), clazz, conversation, false, message, messageSeverity);
	}

	@Override
	protected String getMessage(Exception e) {
		return message;
	}

	@Override
	protected String getViewId(Exception e) {
		if (id != null) {
			return id.getValue();
		} else {
			return null;
		}
	}

	@Override
	public boolean isHandler(Exception e) {
		return clazz.isInstance(e);
	}

	@Override
	protected boolean isEnd(Exception e) {
		return conversation;
	}

	@Override
	protected boolean isEndBeforeRedirect(Exception e) {
		return conversationBeforeRedirect;
	}

	@Override
	public Severity getMessageSeverity(Exception e) {
		return messageSeverity;
	}

}