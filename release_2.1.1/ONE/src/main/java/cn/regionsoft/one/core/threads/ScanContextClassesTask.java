package cn.regionsoft.one.core.threads;

import java.util.HashSet;
import java.util.concurrent.Callable;

import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.utils.ContextScan;

public class ScanContextClassesTask implements Callable<HashSet<Class<?>>>{
	private H2OContext context;
    public ScanContextClassesTask(H2OContext context) {
		this.context = context;
	}

	@Override
    public HashSet<Class<?>> call(){
	    try{
	    	HashSet<Class<?>> result  = new HashSet<Class<?>>();
	    	for(String path : context.getConfig().getSystemContextPaths()){
	    		result.addAll(ContextScan.getClassListByPackage(path));
	    	}
	    	//result.addAll(ContextScan.getClassListByPackage("cn.regionsoft.one.core.containermanaged"));
	    	result.addAll(ContextScan.getClassListByPackage("cn.regionsoft.one.admin"));
	    	result.addAll(ContextScan.getClassListByPackage("cn.regionsoft.one.event"));
	    	
	    	//for jar 混淆
	    	result.add(cn.regionsoft.one.event.HttpMsgEventListener.class);
	    	result.add(cn.regionsoft.one.event.SampleSlowEventListener.class);
	    	result.add(cn.regionsoft.one.event.SystemSlowEventListener.class);
	    	result.add(cn.regionsoft.one.event.HttpFileEventListener.class);
	    	result.add(cn.regionsoft.one.event.EventRegister.class);
	    	
	        return result;
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    	return null;
	    }
    }
}
