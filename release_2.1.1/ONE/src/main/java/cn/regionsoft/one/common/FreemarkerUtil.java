package cn.regionsoft.one.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerUtil {
	private static Configuration cfg = null;
	static {
		cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setDefaultEncoding("UTF-8");
	}
	
	public static Configuration getFreemarkerConfig(){
		return cfg;
		//cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
		//Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    	//cfg.setDefaultEncoding("UTF-8");
		
	}
	
	/*public static void print(String name, Map<String, Object> root) {
        try {
        	Template temp = cfg.getTemplate(name);
            temp.process(root, new PrintWriter(System.out));
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
	
	private static Map<String,Template> templateMap = new ConcurrentHashMap<String,Template>();

	public static boolean templateLoaded(String filePath){
		return templateMap.containsKey(filePath);
	}
	
	/*public static FileNode getFileNode(String filePath){
		FileNode fileNode = folderCache.get(filePath);
		if(fileNode==null){
			File file = new File(filePath);
			boolean isDirectory = file.isDirectory();
			fileNode = new FileNode(file,file.getPath(),isDirectory,file.getName());
			
			if(isDirectory){
				File[] files = file.listFiles();
				FileNode[] childFiles = new FileNode[files.length];
				int i = 0 ;
				for(File tmpFile:files){
					childFiles[i]=getFileNode(tmpFile);
					i++;
				}
				fileNode.setListFiles(childFiles);
			}
			else{
				fileNode.setListFiles(new FileNode[0]);
			}
			folderCache.put(filePath, fileNode);
		}
		return fileNode;
	}
	
	private static FileNode getFileNode(File file){
		FileNode fileNode = folderCache.get(file.getPath());
		
		if(fileNode==null){
			boolean isDirectory = file.isDirectory();
			fileNode = new FileNode(file,file.getPath(),isDirectory,file.getName());
			
			if(isDirectory){
				File[] files = file.listFiles();
				FileNode[] childFiles = new FileNode[files.length];
				int i = 0 ;
				for(File tmpFile:files){
					childFiles[i]=getFileNode(tmpFile);
					i++;
				}
				fileNode.setListFiles(childFiles);
			}
			folderCache.put(file.getPath(), fileNode);
		}
		return fileNode;
	}*/
	
	/*
	 * 
	 * @param templateFileCacheKey	缓存key
	 * @param name			模板名
	 * @param paraMap         
	 * @param outFile
	 */
	public static void fprint(String templateFileCacheKey,String name, Map<String, Object> paraMap, String outFile) {
        FileWriter out = null;
        try {
            out = new FileWriter(new File(outFile));
            Template temp = templateMap.get(templateFileCacheKey);
            if(temp==null){
            	temp = cfg.getTemplate(name,Constants.UTF8);
            	templateMap.put(templateFileCacheKey, temp);
            }
            temp.process(paraMap, out);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
