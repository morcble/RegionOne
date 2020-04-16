package cn.regionsoft.one.systemcommand;

public interface CommandListener {
	public void onCompleted(boolean withError);
	public void onLog(String log);
}
