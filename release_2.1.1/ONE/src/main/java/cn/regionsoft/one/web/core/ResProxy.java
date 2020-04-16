package cn.regionsoft.one.web.core;

public interface ResProxy {
	public <T> T create(Class<?> interfaceClass, final String bindService);
}
