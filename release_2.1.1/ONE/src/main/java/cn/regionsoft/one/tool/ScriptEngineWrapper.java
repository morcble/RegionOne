package cn.regionsoft.one.tool;

import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ScriptEngineWrapper {
	private ReentrantLock lock = new ReentrantLock();
	
	private ScriptEngine scriptEngine;
	public ScriptEngineWrapper(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}

	public Object eval(String script) throws ScriptException  {
		lock.lock();
		Object result = scriptEngine.eval(script);
		lock.unlock();
		return result;
	}

}
