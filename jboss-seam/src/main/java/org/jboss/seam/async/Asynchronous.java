package org.jboss.seam.async;

import java.io.Serializable;

import javax.servlet.ServletContext;

import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletApplicationMap;

/**
 * Something that happens asynchronously, and with a full
 * set of Seam contexts, including propagation of the 
 * business process and task instances.
 * 
 * @author Gavin King
 *
 */
public abstract class Asynchronous implements Serializable {

	static final long serialVersionUID = -551286304424595765L;

	private static transient final LogProvider log = Logging.getLogProvider(Asynchronous.class);

	private Long processId;
	private Long taskId;
	private ServletContext servletContext;

	public Asynchronous() {
		if (Init.instance().isJbpmInstalled()) {
			BusinessProcess businessProcess = BusinessProcess.instance();
			processId = businessProcess.getProcessId();
			taskId = BusinessProcess.instance().getTaskId();
		}
		this.servletContext = ServletLifecycle.getCurrentServletContext();
	}

	protected abstract class ContextualAsynchronousRequest {

		private Object timer;
		private boolean createContexts;

		public ContextualAsynchronousRequest(Object timer) {
			this.timer = timer;
			this.createContexts = !Contexts.isEventContextActive() && !Contexts.isApplicationContextActive();
		}

		private void setup() {
			if (createContexts) {
				Lifecycle.beginApplication(new ServletApplicationMap(servletContext));
				Lifecycle.beginCall();
			}
			Contexts.getEventContext().set(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL, Boolean.TRUE);
			if (taskId != null) {
				BusinessProcess.instance().resumeTask(taskId);
			} else if (processId != null) {
				BusinessProcess.instance().resumeProcess(processId);
			}

			if (timer != null) {
				Contexts.getEventContext().set("timer", timer);
			}

			if (log.isDebugEnabled()) {
				log.debug("executing: " + this);
			}
		}

		protected abstract void process();

		public void run() {
			setup();
			try {
				process();
			} finally {
				cleanup();
			}
		}

		private void cleanup() {
			Contexts.getEventContext().remove(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL);
			if (createContexts) {
				Lifecycle.endCall();
				Lifecycle.beginApplication(null);
			}
		}
	}

	public abstract void execute(Object timer);

	protected abstract void handleException(Exception exception, Object timer);
}