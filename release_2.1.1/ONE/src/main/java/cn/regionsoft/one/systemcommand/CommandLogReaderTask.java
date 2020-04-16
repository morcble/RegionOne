package cn.regionsoft.one.systemcommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;

class CommandLogReaderTask implements Runnable{
	private static Logger logger = Logger.getLogger(CommandLogReaderTask.class);
	
	private InputStream inputStream;
	private InputStream errorStream;
	private SystemCommand systemCommand;
	
	private Long deadTime;
	private Long timeout;
	
	private Process process;
	
	private ThreadHealthMonitor monitor;
	

	public CommandLogReaderTask(SystemCommand systemCommand, Long timeout,Process process) {
		this.inputStream = process.getInputStream();
		this.errorStream = process.getErrorStream();
		this.systemCommand = systemCommand;
		this.timeout = timeout;
		this.process = process;
		if(timeout!=-1) {
			this.deadTime = System.currentTimeMillis() + timeout;
		}
		
		this.monitor = new ThreadHealthMonitor();
		//this.monitor.start();
		
	}

	@Override
	public void run() {
		BufferedReader bs = null;
		BufferedReader bsError = null;
		try{
			bs = new BufferedReader(new InputStreamReader(inputStream));
			bsError = new BufferedReader(new InputStreamReader(errorStream));
			String line = null;
			String errorLine = null;
			while(true) {
				if(this.timeout!=-1 && System.currentTimeMillis()>this.deadTime) {
					systemCommand.appendCommandLog("command timeout for life duration "+timeout+" milliseconds");
					this.process.destroy();
					break;
				}
				
				
				line = bs.readLine();
				this.monitor.setLastReadTime(System.currentTimeMillis());
				if(line!=null) {
					if(line!=null)systemCommand.appendCommandLog(line);
					continue;
				}
				
				errorLine = bsError.readLine();
				if(errorLine!=null) {
					if(errorLine!=null)systemCommand.appendCommandLog(errorLine);
				}
				
				if(systemCommand.completed) {
					break;
				}
				else {
					Thread.sleep(100);
				}
			}
		}
		catch(Exception e) {
			if(e instanceof java.io.IOException) {
				
			}
			else logger.error(e);
		}
		finally {
			systemCommand.finish();
			CommonUtil.closeQuietly(bs);
			CommonUtil.closeQuietly(bsError);
			CommonUtil.closeQuietly(inputStream);
			CommonUtil.closeQuietly(errorStream);
		}

	}
	
	//TODO readline 超时
	class ThreadHealthMonitor extends Thread{
		private Long lastReadTime;
		public void setLastReadTime(Long lastReadTime) {
			this.lastReadTime = lastReadTime;
		}
		public void run() {
			lastReadTime = System.currentTimeMillis();
			while(true) {
				if(System.currentTimeMillis()-lastReadTime>10000) {//10秒内没有日志输出 则销毁进程
					try {
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						System.err.println("No response from console more than 10 seconds, auto kill the process");
						CommandLogReaderTask.this.inputStream.close();
						CommandLogReaderTask.this.process.destroy();
						
						break;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
