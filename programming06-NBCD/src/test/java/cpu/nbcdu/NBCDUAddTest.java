package cpu.nbcdu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class NBCDUAddTest {

    private final NBCDU nbcdu = new NBCDU();
    private final Transformer transformer = new Transformer();
    private DataType src;
    private DataType dest;
    private DataType result;

    @Test
    public void AddTest1() {
        src = new DataType("11000000000000000000000010011000");
        dest = new DataType("11000000000000000000000001111001");
        result = nbcdu.add(src, dest);
        assertEquals("11000000000000000000000101110111", result.toString());
    }

    @Test
    public void AddTest444() {
        src = new DataType(transformer.decimalToNBCD("430263"));
        dest = new DataType(transformer.decimalToNBCD("91563"));
        result = nbcdu.sub(src, dest);
        assertEquals(transformer.decimalToNBCD("-338700"), result.toString());
    }

    @Test
    public void AddTest333() {
        for (int k = 0; k <= 1000; k++) {
            Random r = new Random(k);
            int i = r.nextInt(500000);
            int j = r.nextInt(500000);
            System.out.printf("%d %d\n", i, j);
            src = new DataType(transformer.decimalToNBCD(String.valueOf(i)));
            dest = new DataType(transformer.decimalToNBCD(String.valueOf(j)));
            result = nbcdu.sub(src, dest);
            assertEquals(transformer.decimalToNBCD(String.valueOf(j - i)), result.toString());
        }
    }

}
