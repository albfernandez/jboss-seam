package org.jboss.seam.international;

import static org.jboss.seam.international.StatusMessage.Severity.INFO;
import static org.jboss.seam.international.StatusMessage.Severity.WARN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.validation.ConstraintViolation;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;

/**
 * Abstract base class for providing status messages. View layers should provide
 * a concrete implementation.
 * 
 * @author Pete Muir
 *
 */
public abstract class StatusMessages implements Serializable {
	private static final long serialVersionUID = -5395975397632138270L;

	public static final String COMPONENT_NAME = "org.jboss.seam.international.statusMessages";

	private List<StatusMessage> messages = new ArrayList<StatusMessage>();
	private Map<String, List<StatusMessage>> keyedMessages = new HashMap<String, List<StatusMessage>>();

	private transient List<Runnable> tasks;

	public StatusMessages() {
		super();
	}
	
	protected List<StatusMessage> getMessages() {
		return messages;
	}

	protected Map<String, List<StatusMessage>> getKeyedMessages() {
		return keyedMessages;
	}

	/**
	* Clear all status messages
	*/
	public void clear() {
		messages.clear();
		keyedMessages.clear();
	}

	public void clearKeyedMessages(String id) {
		keyedMessages.remove(id);
	}

	public void clearGlobalMessages() {
		messages.clear();
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* You can also specify the severity, and parameters to be interpolated
	*/
	public void add(Severity severity, String key, String detailKey, String messageTemplate, String messageDetailTemplate,
			final Object... params) {
		final StatusMessage message = new StatusMessage(severity, key, detailKey, messageTemplate, messageDetailTemplate);
		if (!message.isEmpty()) {
			messages.add(message);
			getTasks().add(new Runnable() {

				@Override
				public void run() {
					message.interpolate(params);
				}

			});
		}
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used. 
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addToControl(String id, Severity severity, String key, String messageTemplate, final Object... params) {
		final StatusMessage message = new StatusMessage(severity, key, null, messageTemplate, null);
		if (!message.isEmpty()) {
			List<StatusMessage> mess = keyedMessages.get(id);
			if (mess == null) {
				mess = new ArrayList<StatusMessage>();
				keyedMessages.put(id,  mess);
			}
			mess.add(message);
			
			getTasks().add(new Runnable() {

				@Override
				public void run() {
					message.interpolate(params);
				}

			});
		}
	}

	/**
	* Create a new status message, with the messageTemplate is as the message.
	*
	* A severity of INFO will be used, and you can specify paramters to be
	* interpolated
	*/
	public void add(String messageTemplate, Object... params) {
		add(INFO, messageTemplate, params);
	}

	/**
	* Create a new status message, with the messageTemplate is as the message.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void add(Severity severity, String messageTemplate, Object... params) {
		add(severity, null, null, messageTemplate, null, params);
	}

	/**
	* Create a new status message, with the messageTemplate is as the message.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* A severity of INFO will be used, and you can specify parameters to be 
	* interpolated
	* 
	*/
	public void addToControl(String id, String messageTemplate, Object... params) {
		addToControl(id, INFO, null, messageTemplate, params);
	}

	/**
	* Create a new status message, with the messageTemplate is as the message.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addToControl(String id, Severity severity, String messageTemplate, Object... params) {
		addToControl(id, severity, null, messageTemplate, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* A severity of INFO will be used, and you can specify parameters to be 
	* interpolated
	*/
	public void addFromResourceBundle(String key, Object... params) {
		addFromResourceBundle(INFO, key, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addFromResourceBundle(Severity severity, String key, Object... params) {
		addFromResourceBundleOrDefault(severity, key, key, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* A severity of INFO will be used, and you can specify parameters to be 
	* interpolated
	* 
	*/
	public void addFromResourceBundleOrDefault(String key, String defaultMessageTemplate, Object... params) {
		addFromResourceBundleOrDefault(INFO, key, defaultMessageTemplate, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addFromResourceBundleOrDefault(Severity severity, String key, String defaultMessageTemplate, Object... params) {
		add(severity, key, null, defaultMessageTemplate, null, params);
	}

	/**
	* Create a new status message, looking up the message in the resource bundle
	* using the provided key.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* A severity of INFO will be used, and you can specify parameters to be 
	* interpolated
	* 
	*/
	public void addToControlFromResourceBundle(String id, String key, Object... params) {
		addToControlFromResourceBundle(id, INFO, key, params);
	}

	/**
	* Create a new status message, looking up the message in the resource bundle
	* using the provided key.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addToControlFromResourceBundle(String id, Severity severity, String key, Object... params) {
		addToControlFromResourceBundleOrDefault(id, severity, key, key, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* A severity of INFO will be used, and you can specify parameters to be 
	* interpolated
	* 
	*/
	public void addToControlFromResourceBundleOrDefault(String id, String key, String defaultMessageTemplate, Object... params) {
		addToControlFromResourceBundleOrDefault(id, INFO, key, defaultMessageTemplate, params);
	}

	/**
	* Add a status message, looking up the message in the resource bundle
	* using the provided key. If the message is found, it is used, otherwise, 
	* the defaultMessageTemplate will be used.
	* 
	* The message will be added to the widget specified by the ID. The algorithm
	* used determine which widget the id refers to is determined by the view 
	* layer implementation in use.
	* 
	* You can also specify the severity, and parameters to be interpolated
	* 
	*/
	public void addToControlFromResourceBundleOrDefault(String id, Severity severity, String key, String defaultMessageTemplate,
			Object... params) {
		addToControl(id, severity, key, defaultMessageTemplate, params);
	}

	/**
	* Add an array of InvalidValues from Hibernate Validator. Each message will
	* be added with a severity of WARN.
	*/
	public void add(Set<ConstraintViolation<Object>> constraintViolations) {
		for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
			add(constraintViolation);
		}
	}

	/**
	* Add an array of InvalidValues from Hibernate Validator. Each message will
	* be added with a severity of WARN.
	* 
	* The name of the property that was validated will be used as the widget ID
	*/
	public void addToControls(Set<ConstraintViolation<Object>> constraintViolations) {
		for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
			addToControl(constraintViolation);
		}
	}

	/**
	* Add an InvalidValue from Hibernate Validator. The message will
	* be added with a severity of WARN.
	*/
	public void add(ConstraintViolation<Object> constraintViolation) {
		add(WARN, constraintViolation.getMessage());
	}

	/**
	* Add an InvalidValue from Hibernate Validator. The message will
	* be added with a severity of WARN.
	* 
	* The name of the property that was validated will be used as the widget ID
	*/
	public void addToControl(ConstraintViolation<Object> constraintViolation) {
		addToControl(constraintViolation.getInvalidValue().toString(), constraintViolation);
	}

	/**
	* Add an InvalidValue from Hibernate Validator. The message will
	* be added with a severity of WARN.
	* 
	* You can also specify the id of the widget to add the message to
	*/
	public void addToControl(String id, ConstraintViolation<Object> constraintViolation) {
		addToControl(id, WARN, constraintViolation.getMessage());
	}

	private List<Runnable> getTasks() {
		if (tasks == null) {
			tasks = new Vector<Runnable>();
		}
		return tasks;
	}

	protected static void runTasks() {
		if (!Contexts.isApplicationContextActive()) {
			// Prevent fail to get component if not application context is active
			return;
		}
		Component component = Component.forName(StatusMessages.COMPONENT_NAME);
		if (component != null && !component.getScope().isContextActive()) {
			return;
		}
		//Attempting to get the instance anyway for backwards compatibility with some potential hack situations.
		StatusMessages statusMessages = instance();
		if (statusMessages != null) {
			statusMessages.doRunTasks();
		}
	}

	protected void doRunTasks() {
		if (tasks != null) {
			tasks.forEach(task -> task.run());
			tasks.clear();
		}
	}

	public static StatusMessages instance() {
		Component component = Component.forName(StatusMessages.COMPONENT_NAME);
		if (component != null && !component.getScope().isContextActive()) {
			throw new IllegalStateException("No active " + component.getScope().name() + " context");
		}
		//Attempting to get the instance anyway for backwards compatibility with some potential hack situations.
		return (StatusMessages) Component.getInstance(COMPONENT_NAME);
	}

	/**
	* checks if there are pending messages.
	* @return true if there are messages
	*/

	public boolean isEmpty() {
		return (this.messages == null || this.messages.isEmpty()) && (this.keyedMessages == null || this.keyedMessages.isEmpty());
	}

}
