package org.jboss.seam.core;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

/**
 * Metadata about an active conversation. Also used
 * by the conversation list and breadcrumbs.
 *
 * @author Gavin King
 *
 */
public final class ConversationEntry implements Serializable, Comparable<ConversationEntry> {
	private static final long serialVersionUID = 3624635335271963568L;

	private long lastRequestTime;
	private String description;
	private String id;
	private Date startDatetime;
	private Date lastDatetime;
	private String viewId;
	private List<String> conversationIdStack;
	private Integer timeout;
	private Integer concurrentRequestTimeout;
	private boolean removeAfterRedirect;
	private boolean ended;

	private ConversationEntries entries;

	private ReentrantLock lock;

	private boolean destroyed = false;

	public ConversationEntry(String id, List<String> stack, ConversationEntries entries) {
		this.id = id;

		if (stack == null) {
			throw new IllegalArgumentException("Stack must not be null");
		}
		if (id == null) {
			throw new IllegalArgumentException("id must not be null");
		}

		this.conversationIdStack = stack;

		this.startDatetime = new Date();
		this.entries = entries;

		if (conversationIdStack.size() > 1) {
			// get the root conversation entry lock (we want to share the same lock  
			// among all nested conversations in the same conversation stack)
			lock = entries.getConversationEntry(conversationIdStack.get(conversationIdStack.size() - 1)).lock;
		} else {
			lock = new ReentrantLock(true);
		}
		touch();
	}

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		entries.setDirty(this.description, description);
		this.description = description;
	}

	public synchronized long getLastRequestTime() {
		return lastRequestTime;
	}

	synchronized void touch() {
		entries.setDirty();
		lastRequestTime = System.currentTimeMillis();
		lastDatetime = new Date();
	}

	public String getId() {
		return id;
	}

	public Date getStartDatetime() {
		return startDatetime;
	}

	public void destroy() {
		boolean success = Manager.instance().switchConversation(getId());
		if (success) {
			Manager.instance().endConversation(false);
		}
	}

	public void select() {
		redirect();
	}

	public boolean redirect() {
		String viewId = getViewId();
		if (viewId == null) {
			return false;
		} else {
			Manager.instance().redirect(viewId, getId());
			return true;
		}
	}

	void setViewId(String viewId) {
		entries.setDirty(this.viewId, viewId);
		this.viewId = viewId;
	}

	public String getViewId() {
		return viewId;
	}

	public synchronized Date getLastDatetime() {
		return lastDatetime;
	}

	public List<String> getConversationIdStack() {
		return conversationIdStack;
	}

	public boolean isDisplayable() {
		return !isEnded() && !isRemoveAfterRedirect() && getDescription() != null;
	}

	public boolean isCurrent() {
		Manager manager = Manager.instance();
		if (manager.isLongRunningConversation()) {
			return id.equals(manager.getCurrentConversationId());
		} else if (manager.isNestedConversation()) {
			return id.equals(manager.getParentConversationId());
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(ConversationEntry entry) {
		int result = Long.compare(getLastRequestTime(), entry.getLastRequestTime());
		return -(result == 0 ? getId().compareTo(entry.getId()) : result);
	}

	public int getTimeout() {
		return timeout == null ? Manager.instance().getConversationTimeout() : timeout;
	}

	void setTimeout(int conversationTimeout) {
		entries.setDirty(this.timeout, timeout);
		this.timeout = conversationTimeout;
	}

	public Integer getConcurrentRequestTimeout() {
		if (concurrentRequestTimeout != null) {
			return concurrentRequestTimeout;
		}
		return Integer.valueOf(Manager.instance().getConcurrentRequestTimeout());
	}

	void setConcurrentRequestTimeout(Integer concurrentRequestTimeout) {
		entries.setDirty(this.concurrentRequestTimeout, concurrentRequestTimeout);
		this.concurrentRequestTimeout = concurrentRequestTimeout;
	}

	public boolean isRemoveAfterRedirect() {
		return removeAfterRedirect;
	}

	public void setRemoveAfterRedirect(boolean removeAfterRedirect) {
		entries.setDirty();
		this.removeAfterRedirect = removeAfterRedirect;
	}

	void setId(String id) {
		this.id = id;
	}

	public boolean lockNoWait() //not synchronized!
	{
		return lock.tryLock();
	}

	public boolean lock() //not synchronized!
	{
		try {
			return lock.tryLock(getConcurrentRequestTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

	public void unlock() //not synchronized!
	{
		lock.unlock();
	}

	public boolean isLockedByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}

	public void end() {
		ended = true;
	}

	public boolean isEnded() {
		return ended;
	}

	public boolean isNested() {
		return conversationIdStack.size() > 1;
	}

	/**
	* Determines which conversation in the stack is holding the instance of this
	* component. A nested conversation can see context variables in all ancestor
	* conversations. In this case, we are interesting in knowing where that
	* instance was found. We are assuming that if the reference is not in an
	* ancestor conversation, then it must be in the current conversation. The
	* goal here is not to locate the instance, but rather to determine which
	* conversation is contributing the instance that we already know exists.
	* 
	* The low-level interaction with the session context should be refactored
	* out. The problem is that it is defined in private areas of
	* ServerConversationContext and cannot be reused. Actually, what we really
	* need is a general purpose utility for analyzing the contents of each
	* conversation in the stack (at least the keys).
	*/
	public String findPositionInConversationStack(Component component) {
		if (component.isPerNestedConversation()) {
			return id;
		}

		String name = component.getName();
		Context session = Contexts.getSessionContext();
		String location = id;
		int len = conversationIdStack.size();
		for (int i = 1; i < len; i++) {
			String cid = conversationIdStack.get(i);
			String key = ScopeType.CONVERSATION.getPrefix() + '#' + cid + '$' + name;
			if (session.get(key) != null) {
				location = cid;
				break;
			}
		}

		return location;
	}

	/**
	* Should be called before destroying conversation.
	* Only one thread gets to destroy conversation
	* @return true if is the first time called and this thread can/should destroy conversation
	*/
	synchronized boolean lockDestroy() {
		if (destroyed) {
			return false;
		}
		destroyed = true;
		return true;
	}

	@Override
	public String toString() {
		return "ConversationEntry(" + id + ")";
	}
}