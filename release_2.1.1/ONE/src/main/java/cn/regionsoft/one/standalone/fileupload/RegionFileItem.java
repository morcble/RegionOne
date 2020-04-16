package cn.regionsoft.one.standalone.fileupload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class RegionFileItem implements FileItem{
	private static final long serialVersionUID = -658918796242594130L;
	private static final String SEMICOLON = ";";
	private static final String EQUAL = "=";
	private static final String QUOTATION = "\"";
	private static final String EMPTY_STR ="";
	
	private String contentDisposition = null;
	private String contentType = null;
	
	private String content = null;
	
	private int fileByteStartIndex = -1;
	private int fileByteEndIndex = -1;
	private String filePath = null;
	
	public boolean isFile(){
		if(fileByteEndIndex!=-1)return true;
		else return false;
	}
	
	private String fieldName = null;
	public String getFieldName() {
		if(!isFile()&&fieldName==null){
			if(contentDisposition!=null){
				String[] paraPairs= contentDisposition.split(SEMICOLON);
				String[] paraArray = null;
				if(paraPairs.length>0){
					paraArray = paraPairs[1].split(EQUAL);
					if(paraArray.length>0){
						fieldName = paraArray[1].trim().replace(QUOTATION, EMPTY_STR);
					}
				}
				
			}
		}
		return fieldName;
	}
	
	/**
	 * 
	 * @return filename
	 */
	public String getName() {
		if(contentDisposition!=null){
			String[] paraPairs = contentDisposition.split(SEMICOLON);
			String[] paraArray = null;
			for(String tmp:paraPairs){
				paraArray = tmp.split(EQUAL);
				if(paraArray.length!=2)continue;
				if(!(paraArray[0].trim()).equals("filename"))continue;
				return paraArray[1].replace(QUOTATION, EMPTY_STR);
			}
		}
		return null;
	}

	
	public long getSize() {
		return fileByteEndIndex - fileByteStartIndex;
	}

	public String getString() {
		return content;
	}

	
	public String getContentDisposition() {
		return contentDisposition;
	}
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getFileByteStartIndex() {
		return fileByteStartIndex;
	}
	public void setFileByteStartIndex(int fileByteStartIndex) {
		this.fileByteStartIndex = fileByteStartIndex;
	}
	public int getFileByteEndIndex() {
		return fileByteEndIndex;
	}
	public void setFileByteEndIndex(int fileByteEndIndex) {
		this.fileByteEndIndex = fileByteEndIndex;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public void write(File destFile) throws Exception{
		FileOutputStream  fos = null;
	    BufferedOutputStream bos = null;
	    
	    File file = null;
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    try{
	    	file = new File(this.filePath);
	    	fis = new FileInputStream(file);
	    	bis = new BufferedInputStream(fis);
	    	
	    	fos = new FileOutputStream(destFile);
	    	bos = new BufferedOutputStream(fos);
	    	byte[] writeBuffer = new byte[1024];
	    	
	    	
	    	int fileByteStartIndex = getFileByteStartIndex();
	    	int fileByteEndIndex = getFileByteEndIndex();
	    	int fileLength = fileByteEndIndex - fileByteStartIndex;
	    	
	    	fis.skip(fileByteStartIndex);
	    	
	    	int fullCopyTime = fileLength/writeBuffer.length;
	    	int restByte = fileLength%writeBuffer.length;
	    	int index = 1;
	    	while(fis.read(writeBuffer)!=-1){
	    		if(index==(fullCopyTime+1)){
	    			bos.write(writeBuffer, 0, restByte);
	    			break;
	    		}
	    		else{
	    			bos.write(writeBuffer);
		    		index++;
	    		}
	    	}
	    }
	    catch(Exception e){
	    	throw e;
	    }
	    finally{
	    	if(bos!=null)try {bos.close();} catch (IOException e) {}
			if(fos!=null)try {fos.close();} catch (IOException e) {};
			if(bis!=null)try {bis.close();} catch (IOException e) {};
			if(fis!=null)try {fis.close();} catch (IOException e) {};
			
			file.delete();
	    }
	}

	@Override
	public FileItemHeaders getHeaders() {
		return null;
	}

	@Override
	public void setHeaders(FileItemHeaders headers) {	
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public boolean isInMemory() {
		return false;
	}

	@Override
	public byte[] get() {
		return null;
	}

	@Override
	public String getString(String encoding) throws UnsupportedEncodingException {
		return new String(content.getBytes(),encoding);
	}

	@Override
	public void delete() {

	}

	@Override
	public void setFieldName(String name) {
		this.fieldName = name;
	}

	@Override
	public boolean isFormField() {
		return !isFile();
	}

	@Override
	public void setFormField(boolean state) {
		
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}


	
}
