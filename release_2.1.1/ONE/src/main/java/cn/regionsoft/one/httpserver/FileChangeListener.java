package cn.regionsoft.one.httpserver;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileChangeListener extends Thread{
	private File baseFolder;
	
	private Map<String,Long> fileChangeDtMap = new HashMap<String,Long>();
	private Set<String> filePathSet = new HashSet<String>();
	
	private ConcurrentHashMap<String,byte[]> resMap ;
    private ConcurrentHashMap<String,String> contentTypeMap ;
    private ConcurrentHashMap<String,Long> lastModifiedMap ;
	
	public FileChangeListener(String baseDoc, ConcurrentHashMap<String, byte[]> resMap, ConcurrentHashMap<String, String> contentTypeMap,ConcurrentHashMap<String,Long> lastModifiedMap) {
		super();
		baseFolder = new File(baseDoc);
		scanFolder(baseFolder);
		
		this.resMap = resMap;
        this.contentTypeMap = contentTypeMap;
        this.lastModifiedMap = lastModifiedMap;
	}
	
	
	private void scanFolder(File file) {
		if(!file.exists()) return;
		
		filePathSet.clear();
		
		File[] childs = file.listFiles();
		for(File tmp:childs) {
			subScanFiles(tmp);
		}
		
		/**
		 * 如果文件修改了,就清空3个map
		 */
		for(String path:filePathSet) {
			resMap.remove(path);
			contentTypeMap.remove(path);
			lastModifiedMap.remove(path);
		}
	}
	
	private void subScanFiles(File file) {
		if(!file.exists()) return;
		
		if(file.isDirectory()) {
			File[] childs = file.listFiles();
			for(File tmp:childs) {
				subScanFiles(tmp);
			}
		}
		else if(file.isFile()) {
			String path = file.getPath();
			Long lastModified = file.lastModified();
			Long changeTime = fileChangeDtMap.get(path);
			if(changeTime==null) {
				fileChangeDtMap.put(path, lastModified);
			}
			else {
				if(changeTime.longValue() != lastModified.longValue()) {
					filePathSet.add(path);
					fileChangeDtMap.put(path, lastModified);
				}
			}
		}
		
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep(3000);
				scanFolder(baseFolder);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
