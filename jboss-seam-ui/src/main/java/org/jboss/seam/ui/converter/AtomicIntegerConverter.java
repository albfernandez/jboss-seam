package org.jboss.seam.ui.converter;

import java.util.concurrent.atomic.AtomicInteger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.jboss.seam.util.Strings;

/**
 *  Converter for java.util.concurrent.atomic.AtomicInteger
 * @author Dennis Byrne
 */
@FacesConverter(value = "org.jboss.seam.ui.AtomicIntegerConverter")
public class AtomicIntegerConverter implements Converter {
	
	public AtomicIntegerConverter() {
		super();
	}

	@Override
	public Object getAsObject(FacesContext ctx, UIComponent ui, String value) {
		if (Strings.isEmpty(value)) {
			return null;
		}
		try {
			return new AtomicInteger(Integer.parseInt(value.trim()));
		} catch (NumberFormatException nfe) {
			throw new ConverterException(nfe);
		}
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent ui, Object object) {
		String string = "";
		if (object != null) {
			if (object instanceof String) {
				string = (String) object;
			} else if (object instanceof AtomicInteger) {
				string = ((AtomicInteger) object).toString();
			} else {
				throw new ConverterException("Received an instance of " + object.getClass().getName()
						+ ", but was expecting an instance of " + AtomicInteger.class.getName());
			}
		}
		return string;
	}

}
