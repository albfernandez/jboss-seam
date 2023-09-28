package org.jboss.seam.jmx;

import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.mx.server.ServerConstants;


/**
 * Utility class for creating JMX agent identifiers. Also contains the
 * helper method for retrieving the <tt>AgentID</tt> of an existing MBean server
 * instance.
 *
 * @see javax.management.MBeanServerDelegateMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class AgentID implements ServerConstants {
	// Static ----------------------------------------------------
	private static AtomicLong id = new AtomicLong(0L);

	private static final SecureRandom rand = new SecureRandom();

	/**
	* Creates a new agent ID string. The identifier is of the form
	* <tt>&lt;ip.address&gt;/&lt;creation time in ms&gt;/&lt;VMID+(random int 0-100)&gt;/&lt;sequence #&gt;</tt>.<P>
	*
	* This AgentID string is globally unique.
	*
	* @return Agent ID string
	*/
	public static String create() {
		String ipAddress = getIP();
		// use the VMID to create a more unique ID that can be used to guarantee that this
		// MBeanServerID is unique across multiple JVMs, even on the same host
		String vmid = new java.rmi.dgc.VMID().toString().replace(':', 'x').replace('-', 'X') + rand.nextInt(100);

		return ipAddress + "/" + System.currentTimeMillis() + "/" + vmid + "/" + id.incrementAndGet();
	}

	private static String getIP() {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
				@Override
				public String run() throws Exception {
					return InetAddress.getLocalHost().getHostAddress();
				}
			});
		} catch (PrivilegedActionException e) {
			return "127.0.0.1";
		}
	}



	/**
	* Returns the agent identifier string of a given MBean server instance.
	*
	* @return <tt>MBeanServerId</tt> attribute of the MBean server delegate.
	*/
	public static String get(MBeanServer server) {
		try {
			ObjectName name = new ObjectName(MBEAN_SERVER_DELEGATE);
			return (String) server.getAttribute(name, "MBeanServerId");
		} catch (Exception t) {
			throw new Error("Cannot find the MBean server delegate: ", t);
		}
	}
}
