package org.jboss.seam.security.permission.action;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.permission.Permission;
import org.jboss.seam.security.permission.PermissionManager;

import net.sf.ehcache.store.chm.ConcurrentHashMap;

@Scope(CONVERSATION)
@Name("org.jboss.seam.security.permission.permissionSearch")
@Install(precedence = BUILT_IN)
public class PermissionSearch implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<Principal, List<Permission>> groupedPermissions = new ConcurrentHashMap<Principal, List<Permission>>();

	@DataModel
	List<Principal> recipients;

	@DataModelSelection
	Principal selectedRecipient;

	@In
	IdentityManager identityManager;

	@In
	PermissionManager permissionManager;

	private Object target;

	public PermissionSearch() {
		super();
	}
	
	@Begin
	public void search(Object target) {
		this.target = target;
	}

	public void refresh() {
		List<Permission> permissions = permissionManager.listPermissions(target);
		groupedPermissions.clear();

		for (Permission permission : permissions) {
			List<Permission> recipientPermissions = null;

			if (!groupedPermissions.containsKey(permission.getRecipient())) {
				recipientPermissions = new ArrayList<Permission>();
				groupedPermissions.put(permission.getRecipient(), recipientPermissions);
			} else {
				recipientPermissions = groupedPermissions.get(permission.getRecipient());
			}

			recipientPermissions.add(permission);
		}

		recipients = new ArrayList<Principal>(groupedPermissions.keySet());
	}

	public String getActions(Principal recipient) {
		StringBuilder sb = new StringBuilder();

		for (Permission permission : groupedPermissions.get(recipient)) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(permission.getAction());
		}

		return sb.toString();
	}

	public Object getTarget() {
		return target;
	}

	public void revokeSelected() {
		permissionManager.revokePermissions(getSelectedPermissions());
		refresh();
	}

	public Principal getSelectedRecipient() {
		return selectedRecipient;
	}

	public List<Permission> getSelectedPermissions() {
		return groupedPermissions.get(selectedRecipient);
	}
}
