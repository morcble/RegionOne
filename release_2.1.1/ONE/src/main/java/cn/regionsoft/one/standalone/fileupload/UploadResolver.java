package cn.regionsoft.one.standalone.fileupload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadResolver {
	private final static String EMPTY_STR="";
	
	private final static byte NEW_LINE1 = 13;
	private final static byte NEW_LINE2 = 10;

	public static void main(String[] args) throws Exception{
		UploadResolver fileTest = new UploadResolver();
		String tmpFilePath = "E:/workspace/eclipse-jee-mars-R-win32-x86_64/eclipse/workspace/Morcble/Local/FILES/tmp/3729273749209088";
		List<RegionFileItem> fileItemLs = fileTest.resolveUpload(tmpFilePath);
		
		fileItemLs.get(0).write(new File("E:/workspace/eclipse-jee-mars-R-win32-x86_64/eclipse/workspace/Morcble/Local/FILES/tmp/11"));
    }
	
	private String boundaryStr = null;
	private RegionFileItem fileItem = null;
	private List<RegionFileItem> fileItemLs = new ArrayList<RegionFileItem>();
	private int filePart = 0;
	private boolean isInFileBody = false;
	private String filePath = null;
	public List<RegionFileItem> resolveUpload(String tmpFilePath) throws Exception{	
		this.filePath = tmpFilePath;
		File file = new File(filePath);

		FileInputStream fis = null;
		BufferedInputStream bis = null;
	    try {
	    	fis = new FileInputStream(file);
	    	System.out.println(fis.available());
	    	bis = new BufferedInputStream(fis);
	    	int amount = -1;

	    	byte[] buffer = new byte[1024];
	    	
	    	byte[] lineCache = null;//缓存没有处理的已读buffer
	    	int lineStart = 0;//记录本次buffer读到哪个byte了
	    	
	    	byte[] tmpByte = null;
	    	int byteIndex = 0;
	    	byte[] unhandledBytes = null;
	    	int unhandledLength = 0;
	    	st:while((amount=bis.read(buffer))!=-1){
	    		byteIndex += amount;
	    		lineStart = 0;
	    		if(unhandledBytes==null){
	    			unhandledLength = amount;
	    			unhandledBytes = new byte[amount];
	    			System.arraycopy(buffer, 0, unhandledBytes, 0, amount);
	    		}
	    		else{
	    			unhandledLength = amount+unhandledBytes.length;
	    			tmpByte = new byte[unhandledLength];
					System.arraycopy(unhandledBytes, 0, tmpByte, 0, unhandledBytes.length);
					System.arraycopy(buffer, 0, tmpByte, unhandledBytes.length, amount);
					unhandledBytes = tmpByte;
	    		}
	    		
	    		
	    		
	    		for(int x = 0 ; x<unhandledLength;x++){
	    			if(unhandledBytes[x]==NEW_LINE1&&(x+1)<unhandledLength&&unhandledBytes[x+1]==NEW_LINE2){//捕捉到换行符
	    				
	    				lineCache = new byte[x-lineStart];
						System.arraycopy(unhandledBytes, lineStart, lineCache, 0, lineCache.length);
						if(unhandledLength>amount){
							handleLine(new String(lineCache),byteIndex-unhandledLength+x);
						}
						else{
							handleLine(new String(lineCache),byteIndex-amount+x);
						}
						
						lineStart = x+2;
	    			}
	    			
	    			if(x==(unhandledLength-1)){
	    				tmpByte = unhandledBytes;
	    				
	    				unhandledLength = unhandledLength-lineStart;
	    				if(unhandledLength>2048)unhandledLength=2048;
	    				
	    				unhandledBytes = new byte[unhandledLength];
		    			System.arraycopy(tmpByte, tmpByte.length-unhandledLength, unhandledBytes, 0, unhandledLength);
	    			}
	    		}
	    		
	    		
	    		/*for(int x = offset ; x<amount;x++){
	    			if(buffer[x]==NEW_LINE1){
	    				if((x+1)<amount&&buffer[x+1]==NEW_LINE2){
	    					if(lineCache==null){
		    					lineCache = new byte[x-lineStart-offset];
								System.arraycopy(buffer, lineStart+offset, lineCache, 0, lineCache.length);
								handleLine(new String(lineCache),byteIndex-amount+lineStart);
								lineCache = null;
		    				}
		    				else{
		    					tmpByte = new byte[lineCache.length+x];
	    						System.arraycopy(lineCache, 0, tmpByte, 0, lineCache.length);
	    						System.arraycopy(buffer, 0, tmpByte, lineCache.length, x);
	    						handleLine(new String(tmpByte),byteIndex-amount+x+lineStart);
	    						lineCache = null;
		    				}
	    					if((x+1)<amount){
		    					if(buffer[x+1]==NEW_LINE2){
		    						lineStart = x+2;
		    					}
		    				}
		    				else{
		    					offset = 1;//expect /n in next line
		    					continue st;
		    				}
	    				}	
	    			}
	    			
					if (x == (amount - 1)) {
						if(!isInFileBody){
							if(lineCache!=null){
								tmpByte = new byte[lineCache.length + x+1];
								System.arraycopy(lineCache, 0, tmpByte, 0, lineCache.length);
								System.arraycopy(buffer, 0, tmpByte, lineCache.length, x+1);
								lineCache = tmpByte;
							}
							else{
								if(x+1-offset-lineStart>0){
									lineCache = new byte[x+1-offset-lineStart];
									System.arraycopy(buffer, lineStart, lineCache, 0, lineCache.length);
								}
							}
						}
						else{
							if(x+1-offset-lineStart>0){
								lineCache = new byte[x+1-offset-lineStart];
								System.arraycopy(buffer, lineStart, lineCache, 0, lineCache.length);
							}
						}
						lineStart = 0;
						offset = 0;
					}
	    		}*/
	    	}
	    }
	    catch(Exception e){
	    	throw e;
	    }
	    finally {
	    	if(bis!=null)try {bis.close();} catch (IOException e) {};
			if(fis!=null)try {fis.close();} catch (IOException e) {};
	    }
	    
	    /*FileItem aa = fileItemLs.get(0);
	    aa.setFilePath(filePath);
	    try {
			saveFile(aa,"E:/workspace/eclipse-jee-mars-R-win32-x86_64/eclipse/workspace/Morcble/Local/FILES/tmp/11");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    return fileItemLs;
	}
	
	
	
	
	private boolean handleLine(String tmpLine, int lineStart) {
		//System.out.println(tmpLine);
		if(boundaryStr==null){
			boundaryStr=tmpLine;
			fileItem = new RegionFileItem();
			fileItem.setFilePath(filePath);
			filePart = 0;
			return false;
		}
		else if(tmpLine.indexOf(boundaryStr)!=-1){
			if(fileItem!=null){
				finishFileItem(fileItem);
				fileItemLs.add(fileItem);
			}
			
			if(isInFileBody){
				fileItem.setFileByteEndIndex(lineStart-boundaryStr.getBytes().length-2);
				isInFileBody = false;
			}
			
			fileItem = new RegionFileItem();
			fileItem.setFilePath(filePath);
			filePart = 0;
			return true;
		}
		else{
			if(filePart==0){
				fileItem.setContentDisposition(tmpLine);
				filePart = 1;
			}
			else if(filePart==1){
				if(EMPTY_STR.equals(tmpLine)){
					filePart = 3;
				}
				else{
					fileItem.setContentType(tmpLine);
					filePart = 2;
					isInFileBody = true;
				}	
			}
			else if(filePart==2){
				if(EMPTY_STR.equals(tmpLine)){
					filePart = 3;
					fileItem.setFileByteStartIndex(lineStart+2);
				}
			}
			else if(filePart==3){
				if(fileItem.getContentType()==null){
					fileItem.setContent(tmpLine);
					
				}
				/*else{
					//file binary
					if(fileItem.getFileByteStartIndex()==-1){
						fileItem.setFileByteStartIndex(lineStart);
					}
					
				}*/
			}
			return false;
		}
	}

	private void finishFileItem(RegionFileItem fileItem) {
		// TODO Auto-generated method stub
		
	}

}
