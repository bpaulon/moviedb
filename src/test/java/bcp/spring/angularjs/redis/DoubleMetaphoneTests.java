package bcp.spring.angularjs.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class DoubleMetaphoneTests {

	DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

	@Test
	public void test() {
		assertEquals("AKSP", doubleMetaphone.doubleMetaphone("exceptin", true));
		assertEquals("AKSP", doubleMetaphone.doubleMetaphone("exceptions", false));
		assertEquals("PRM0", doubleMetaphone.doubleMetaphone("Prometheus", false));
	}

}
