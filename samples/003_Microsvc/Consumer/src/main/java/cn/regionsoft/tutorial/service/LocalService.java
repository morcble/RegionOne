package cn.regionsoft.tutorial.service;


import cn.regionsoft.one.annotation.MicroConsumer;
import cn.regionsoft.one.annotation.Service;

@Service
public class LocalService {
	@MicroConsumer(version="1.1",group="1.1")
	private SampleServiceI sampleServiceI;
	
	public String invokeRemoveService() {
		return sampleServiceI.someBusinessLogic("query");
	}
}
