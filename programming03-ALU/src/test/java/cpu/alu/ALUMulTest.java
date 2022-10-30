package cpu.alu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import static org.junit.Assert.assertEquals;

public class ALUMulTest {

	private final ALU alu = new ALU();
	private final Transformer transformer = new Transformer();
	private DataType src;
	private DataType dest;
	private DataType result;

	@Test
	/**
	 * 10 * 10 = 100
	 */
	public void MulTest1() {
		src = new DataType("00000000000000000000000000001010");
		dest = new DataType("00000000000000000000000000001010");
		result = alu.mul(src, dest);
		assertEquals("00000000000000000000000001100100", result.toString());
	}
	@Test
	/**
	 * 14 * 10 = 140
	 */
	public void MulTest2() {
		src = new DataType(transformer.intToBinary("123"));
		dest = new DataType(transformer.intToBinary("3"));
		result = alu.mul(src, dest);
		assertEquals(transformer.intToBinary("369"), result.toString());
	}
}
