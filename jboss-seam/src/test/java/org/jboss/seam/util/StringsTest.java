package org.jboss.seam.util;


import org.junit.Test;
import org.junit.Assert;

public class StringsTest {
	
	public StringsTest() {
		super();
	}
	
	@Test
	public void split() {
		Assert.assertNotNull(Strings.split(null, null));
		Assert.assertEquals(0, Strings.split(null, null).length);
		Assert.assertNotNull(Strings.split("", null));
		Assert.assertEquals(0, Strings.split("", null).length);
		Assert.assertEquals(3, Strings.split("a,b,c", ", \r\n\f\t").length);
		Assert.assertEquals(3, Strings.split("a,,b,c", ", \r\n\f\t").length);
		Assert.assertEquals(3, Strings.split("a, b,c", ", \r\n\f\t").length);
	}
	
	@Test
	public void removeStart() {
		Assert.assertEquals("", Strings.removeStart(null, null));
		Assert.assertEquals("abc", Strings.removeStart("abc", null));
		Assert.assertEquals("abc", Strings.removeStart("abc", ""));
		Assert.assertEquals("abc", Strings.removeStart("abc", "x"));
		Assert.assertEquals("abc", Strings.removeStart("testabc", "test"));
		Assert.assertEquals("", Strings.removeStart("abc", "abc"));
		
	}

}
