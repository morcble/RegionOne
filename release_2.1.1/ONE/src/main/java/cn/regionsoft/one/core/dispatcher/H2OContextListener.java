package cn.regionsoft.one.core.dispatcher;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.regionsoft.one.core.SYSEnvSetup;

public class H2OContextListener implements ServletContextListener{

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			SYSEnvSetup.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

}
