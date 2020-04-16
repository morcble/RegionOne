package cn.regionsoft.one.systemcommand;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.tool.ThreadPool;

public class SystemCommand {
	private static final Logger logger = Logger.getLogger(SystemCommand.class);
	
	volatile boolean completed = false;

	private StringBuilder commandLogs = new StringBuilder() ;
	
	private String lcommand;
	
	private boolean processExitWithError = false;
	//监听command执行状态事件
	private List<CommandListener> commandListenerList = null;
	
	//命令存活的时间,单位毫秒,-1标识一直等进程结束
	private Long timeout = -1L;
	
	public SystemCommand(String[] commands) {
		StringBuilder commandStrBuilder = new StringBuilder();
		for(int i = 0 ; i <commands.length ; i++) {
			commandStrBuilder.append(commands[i]);
			if(i!=commands.length-1)
				commandStrBuilder.append(" && ");
		}
		
		this.lcommand = commandStrBuilder.toString();
		logger.debug("run command:"+this.lcommand);
	}
	
	public void excute() {
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", lcommand });
			
			CommandLogReaderTask task = new CommandLogReaderTask(this,timeout,p);
//			Thread thread = new Thread(task);
//			thread.start();
			ThreadPool.getInstance().submitSlowTask(task);
			
			p.waitFor();
			if (p.exitValue() != 0) {
				processExitWithError = true;
			}
			this.completed = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void appendCommandLog(String log) {
		commandLogs.append(log);
		commandLogs.append(Constants.NEW_LINE);
		
		if(commandListenerList!=null) {
			for(CommandListener commandListener:commandListenerList) {
				commandListener.onLog(log);
			}
		}
	}
	
	void finish() {
		if(commandListenerList!=null) {
			for(CommandListener commandListener:commandListenerList) {
				commandListener.onCompleted(processExitWithError);
			}
		}
	}

	
	public void addCommandListener(CommandListener commandListener) {
		if(commandListenerList==null) {
			commandListenerList = new ArrayList<CommandListener>();
		}
		commandListenerList.add(commandListener);
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public StringBuilder getCommandLogs() {
		return commandLogs;
	}

	public boolean isProcessExitWithError() {
		return processExitWithError;
	}
	
}
