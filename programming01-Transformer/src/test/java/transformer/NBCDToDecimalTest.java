package transformer;

import org.junit.Assert;
import org.junit.Test;

public class NBCDToDecimalTest {

	private Transformer transformer = new Transformer();

	@Test
	public void test1() {
		Assert.assertEquals("10",
				transformer.NBCDToDecimal("11000000000000000000000000010000"));
	}

}
