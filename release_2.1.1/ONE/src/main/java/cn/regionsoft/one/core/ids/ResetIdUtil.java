package cn.regionsoft.one.core.ids;


import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class ResetIdUtil {

	public static void main(String[] args) {
		FileInputStream fis = null;
		FileChannel isfc = null;
		try {
			ByteBuffer bb = ByteBuffer.allocate(8);
			fis = new FileInputStream("E:\\workspace\\eclipse-jee-mars-R-win32-x86_64\\eclipse\\workspace\\Morcble\\Local\\Node1\\IDS\\ID1.id");
			isfc = fis.getChannel();
			isfc.read(bb, 0);
			bb.flip();
			System.out.println(bb.getLong());
			
			Long x = 1000000001400013L;
					// 1000000001400013

			/*FileOutputStream fos = new FileOutputStream("E:\\workspace\\eclipse-jee-mars-R-win32-x86_64\\eclipse\\workspace\\Morcble\\Local\\Node1\\IDS\\ID1.id");
			FileChannel fc = fos.getChannel();
			bb.rewind();
		    bb.putLong(x);
		    bb.flip();
		    fc.write(bb, 0);*/

			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
