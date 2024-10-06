package org.jboss.seam.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.util.Strings;
import org.jboss.seam.web.AbstractResource;

/**
 * Routes all types of HTTP requests to implementors of AbstractResource.
 * <p>
 * This servlet is optional in a Seam application but required for certain features, such as
 * <tt>&lt;s:graphicImage/&gt;</tt>, RichFaces resources, and REST integration. It is typically
 * mapped in <tt>web.xml</tt> with the <tt>/seam/resource</tt> URL pattern.
 * </p>
 * 
 * @author Shane Bryzak
 */
public class SeamResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private ServletContext context;

	private Map<String, AbstractResource> providers = new HashMap<String, AbstractResource>();

	public SeamResourceServlet() {
		super();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		context = config.getServletContext();
		loadResourceProviders();
	}

	protected void loadResourceProviders() {
		try {
			Lifecycle.setupApplication(new ServletApplicationMap(context));

			for (String name : Init.instance().getResourceProviders()) {
				AbstractResource provider = (AbstractResource) Component.getInstance(name, true);
				if (provider != null) {
					provider.setServletContext(context);
					providers.put(provider.getResourcePath(), provider);
				}
			}
		} finally {
			Lifecycle.cleanupApplication();
		}
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String prefix = request.getContextPath() + request.getServletPath();

		if (request.getRequestURI().startsWith(prefix)) {
			String path = Strings.removeStart(request.getRequestURI(), prefix);
			int index = path.indexOf('/', 1);
			if (index != -1) {
				path = path.substring(0, index);
			}

			AbstractResource provider = providers.get(path);
			if (provider != null) {
				provider.getResource(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

}
