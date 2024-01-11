package org.jboss.seam.theme;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Locale;
import org.jboss.seam.faces.Selector;
import org.jboss.seam.international.Messages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.IteratorEnumeration;

/**
 * Selects the current user's theme
 * 
 * @author Gavin King
 */
@Scope(ScopeType.SESSION)
@Name("org.jboss.seam.theme.themeSelector")
@BypassInterceptors
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
public class ThemeSelector extends Selector {
	private static final long serialVersionUID = 3920407140011388341L;

	private static final LogProvider log = Logging.getLogProvider(ThemeSelector.class);

	private String theme;
	private String[] availableThemes;

	public ThemeSelector() {
		super();
	}
	
	@Create
	public void initDefaultTheme() {
		String themeName = getCookieValueIfEnabled();
		if (themeName != null && availableThemes != null && Arrays.asList(availableThemes).contains(themeName)) {
			setTheme(themeName);
		}

		if (theme == null) {
			if (availableThemes == null || availableThemes.length == 0) {
				throw new IllegalStateException("no themes defined");
			}
			theme = availableThemes[0];
		}
	}

	@Override
	protected String getCookieName() {
		return "org.jboss.seam.core.Theme";
	}

	/**
	* Recreate the JSF view, using the new theme, and raise the 
	* org.jboss.seam.themeSelected event
	*
	*/
	public void select() {
		Contexts.removeFromAllContexts("org.jboss.seam.theme.theme");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String viewId = Pages.getViewId(facesContext);
		UIViewRoot viewRoot = facesContext.getApplication().getViewHandler().createView(facesContext, viewId);
		facesContext.setViewRoot(viewRoot);

		setCookieValueIfEnabled(getTheme());

		if (Events.exists()) {
			Events.instance().raiseEvent("org.jboss.seam.themeSelected", getTheme());
		}
	}

	public void select(ValueChangeEvent event) {
		selectTheme((String) event.getNewValue());
	}

	public void selectTheme(String themeName) {
		setTheme(themeName);
		select();
	}

	/**
	* Get a selectable list of available themes for display in the UI
	*/
	public List<SelectItem> getThemes() {
		List<SelectItem> selectItems = new ArrayList<SelectItem>(availableThemes.length);
		for (String name : availableThemes) {
			selectItems.add(new SelectItem(name, getLocalizedThemeName(name)));
		}
		return selectItems;
	}

	/**
	* Get the name of the current theme
	*/
	public String getTheme() {
		return theme;
	}

	public void setTheme(String themeName) {
		setDirty(this.theme, themeName);
		this.theme = themeName;
	}

	public void setAvailableThemes(String[] themeNames) {
		if (themeNames == null) {
			throw new IllegalArgumentException("Theme names cannot be null");
		}
		String[] copy = Arrays.copyOf(themeNames, themeNames.length);
		setDirty(this.availableThemes, copy);
		this.availableThemes = copy;
	}

	/**
	* Get the resource bundle for the theme
	*/
	public ResourceBundle getThemeResourceBundle() {
		try {
			java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(theme, Locale.instance(),
					Thread.currentThread().getContextClassLoader());
			if (log.isTraceEnabled()) {
				log.trace("loaded resource bundle: " + theme);
			}
			return bundle;
		} catch (MissingResourceException mre) {
			if (log.isDebugEnabled()) {
				log.debug("resource bundle missing: " + theme);
			}
			return new ResourceBundle() {
				@Override
				public Enumeration<String> getKeys() {
					List<String> list = Collections.emptyList();
					return new IteratorEnumeration<String>(list.iterator());
				}

				@Override
				protected Object handleGetObject(String key) {
					return null;
				}
			};
		}
	}

	/**
	* Get the localized name of the named theme, by looking for
	* org.jboss.seam.theme.&lt;name&gt; in the Seam resource
	* bundle
	*/
	public String getLocalizedThemeName(String name) {
		String key = "org.jboss.seam.theme." + name;
		String localizedName = Messages.instance().get(key);
		return key.equals(localizedName) ? name : localizedName;
	}

	public static ThemeSelector instance() {
		if (!Contexts.isSessionContextActive()) {
			throw new IllegalStateException("No active session context");
		}
		return (ThemeSelector) Component.getInstance(ThemeSelector.class, ScopeType.SESSION);
	}

	public String[] getAvailableThemes() {
		if (this.availableThemes == null) {
			return null;
		}
		return Arrays.copyOf(this.availableThemes, this.availableThemes.length);
	}

}
