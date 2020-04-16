package cn.regionsoft.one.systemcommand;

import java.io.IOException;
import cn.regionsoft.one.systemcommand.CommandListener;
import cn.regionsoft.one.systemcommand.SystemCommand;

public class Sample {
	public static void main(String[] args) throws IOException, InterruptedException {
			String command = "ping www.baidu.com -c 5";
			//String command = "ls -lrt";
			//String command = "sh aaa.sh";
			//String command = "/usr/local/texlive/2019/bin/x86_64-linux/latex";
			
			SystemCommand systemCommand = new SystemCommand(new String[] {command});
			systemCommand.setTimeout(5000L);
			
			systemCommand.addCommandListener(new CommandListener() {

				@Override
				public void onLog(String log) {
					System.out.println(log);
				}

				@Override
				public void onCompleted(boolean withError) {
					System.out.println("end");
					System.out.println("withError="+withError);
				}
				
			});
			systemCommand.excute();
			System.out.println(systemCommand.getCommandLogs());
		}
}

