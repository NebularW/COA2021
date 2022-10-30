package memory;

import transformer.Transformer;

import java.io.*;
import java.util.Arrays;

/**
 * 磁盘抽象类，磁盘大小为96M
 *
 */
public class Disk {

	public static int DISK_SIZE_B = 96 * 1024 * 1024;      // 磁盘大小 96 MB

	private static Disk diskInstance = new Disk();

//	private static char[] disk = new char[DISK_SIZE_B];

	private static File disk_device;

	private Disk() {
		disk_device = new File("DISK.vdev");
		if (disk_device.exists()) {
			disk_device.delete();
			try {
				disk_device.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter writer = null;
		try {
			disk_device.createNewFile();
			// 初始化磁盘
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(disk_device)));
			char[] dataUnit = new char[1024];
			for (int i = 0; i < 96 * 16; i++) {
				char currentChar = 0x30;
				for (int j = 0; j < 64; j++) {
					Arrays.fill(dataUnit, currentChar);
					writer.write(dataUnit);
					currentChar++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Disk getDisk() {
		return diskInstance;
	}

	public char[] read(String eip, int len){
		char[] data = new char[len];
		RandomAccessFile reader = null;
		try {
			reader = new RandomAccessFile(disk_device, "r");
			// 本作业只有128M磁盘，不会超出int表示范围
			// ps: java的char是两个字节，但是write()方法写的是字节，因此会丢掉char的高8-bits，读的时候需要用readByte()
			// pss: 读磁盘会很慢，请尽可能减少read函数调用
			// psss: 这几行注释你们其实不需要看
			reader.skipBytes(Integer.parseInt(new Transformer().binaryToInt("0" + eip)));
			for (int i=0; i<len; i++) {
				data[i] = (char) reader.readByte();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	public void write(String eip, int len, char[] data){
		RandomAccessFile writer = null;
		try {
			writer = new RandomAccessFile(disk_device, "rw");
			writer.skipBytes(Integer.parseInt(new Transformer().binaryToInt("0" + eip)));
			for (int i=0; i<len; i++) {
				writer.write(data[i]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
