package cn.regionsoft.one.core.ids;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import cn.regionsoft.one.core.CommonUtil;

/**
 * max long 9223372036854775807
 *          [0-9]999999999999999
 * @author fenglj
 *
 */
public class IDWorker {						  
	private static final Long ClUSTER_BASE = 1000000000000000L;
	private static final Long MAX_BASE = ClUSTER_BASE - 1;
	/**
	 * 
	 * @param clusterId  range : 1 - 9222
	 * @param recordFolderPath
	 * @param persistGap
	 */
	public IDWorker(Long clusterId,String recordFolderPath,int persistGap){
		if(clusterId<0||clusterId>9){
			throw new RuntimeException("clusterId range : 0- 9");
		}
		File folder = new File(recordFolderPath);
		if(!folder.exists())folder.mkdirs();
		this.recordFilePath = recordFolderPath +  System.getProperty("file.separator") + "ID"+clusterId.toString()+".id";
		this.recordFileLock = recordFolderPath +  System.getProperty("file.separator") + "ID"+clusterId.toString()+".idlock";
		this.persistGap = persistGap;
		
		clusterMinID = clusterId*ClUSTER_BASE;
		clusterMaxID = clusterId*ClUSTER_BASE + MAX_BASE; 
		init();
	}
	private Long clusterMaxID; 
	private Long clusterMinID;
	
	private int persistGap;
	private FileOutputStream fos = null;      
	private FileChannel fc = null;
	private FileLock idFileLock = null;
	
	private String recordFilePath;
	private String recordFileLock;
	private Long lastGenerateId = null;
	private ByteBuffer bb = ByteBuffer.allocate(8);
	
	
	private RandomAccessFile idLockFileRaf = null;
	private FileChannel idLockFileFc = null;
	private FileLock idLockFileLock = null;
	private File idLockFile;
	private void lockFile(File idLockFile) throws IOException{
		idLockFileRaf = new RandomAccessFile(idLockFile, "rw");
		idLockFileFc = idLockFileRaf.getChannel();
		idLockFileLock = idLockFileFc.tryLock();
	}
	
	private void init() {
		FileInputStream fis = null;
		FileChannel isfc = null;
		try {
			File file = new File(recordFilePath);
			if(file.exists()){
				int jumpGap = 0;
				idLockFile = new File(recordFileLock);
				if(idLockFile.exists()){
					jumpGap = persistGap;
				}
				else{
					idLockFile.createNewFile();
					lockFile(idLockFile);
				}
				
				fis = new FileInputStream(recordFilePath);
				isfc = fis.getChannel();
				isfc.read(bb, 0);
				bb.flip();
				lastGenerateId = bb.getLong();
				lastGenerateId +=jumpGap;
			}
			else{
				lastGenerateId = clusterMinID;
				idLockFile = new File(recordFileLock);
				if(!idLockFile.exists())idLockFile.createNewFile();
				lockFile(idLockFile);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally{
			CommonUtil.closeQuietly(isfc);
			CommonUtil.closeQuietly(fis);
		}

		try {
			fos = new FileOutputStream(recordFilePath);
			fc = fos.getChannel();
			idFileLock = fc.tryLock();
			persistCurrentId(lastGenerateId);
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					IDWorker.this.releaseResource();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Lock reentrantLock = new ReentrantLock();
	
	public Long getNextID() throws Exception{
		try{
			reentrantLock.lock();
			lastGenerateId ++;
			if(lastGenerateId%persistGap==0){
				persistCurrentId(lastGenerateId);
			}
			if(lastGenerateId>clusterMaxID) throw new Exception("Cluster IDWorker is exhausted");
			return lastGenerateId;
		} catch (Exception e) {
			throw e;
		}
		finally{
			reentrantLock.unlock();
		}
		
	}

	private void persistCurrentId(Long index) throws Exception{
		try{
			bb.rewind();
		    bb.putLong(index);
		    bb.flip();
		    fc.write(bb, 0);
		}
		catch(Exception e){
			throw e;
		}
	}
	
	private void releaseResource(){
		try {
			persistCurrentId(lastGenerateId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CommonUtil.closeQuietly(fc);
		CommonUtil.closeQuietly(fos);

		CommonUtil.closeQuietly(idLockFileFc);
		CommonUtil.closeQuietly(idLockFileRaf);
		idLockFile.delete();

	}
}
