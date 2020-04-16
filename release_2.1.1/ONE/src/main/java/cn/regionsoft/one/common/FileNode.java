package cn.regionsoft.one.common;

import java.io.File;

public class FileNode{
	private File file;
	private String name;
	private String path;
	private boolean directory;
	private FileNode[] listFiles = null;
	public FileNode(File file, String path,boolean directory,String name) {
		this.file = file;
		this.path = path;
		this.directory = directory;
		this.name = name;
	}

	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isDirectory() {
		return directory;
	}
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}
	public FileNode[] listFiles() {
		return listFiles;
	}
	public void setListFiles(FileNode[] listFiles) {
		this.listFiles = listFiles;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
