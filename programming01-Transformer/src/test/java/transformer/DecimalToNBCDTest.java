package transformer;

import org.junit.Assert;
import org.junit.Test;

public class DecimalToNBCDTest {

    private Transformer transformer = new Transformer();

    @Test
    public void test1() {
        Assert.assertEquals("11000000000000000000000000010000",
                transformer.decimalToNBCD("10"));
    }

}
