package cn.regionsoft.tutorial.service;

import cn.regionsoft.one.annotation.MicroProvider;
import cn.regionsoft.one.annotation.Service;
import cn.regionsoft.one.annotation.tag.Autowired;
import cn.regionsoft.tutorial.dao.SampleDao;

@Service
@MicroProvider(version="1.1",group="1.1")
public class SampleService implements SampleServiceI{
	@Autowired
	private SampleDao helloworldDao;
	
	public String someBusinessLogic(String para) {
		return helloworldDao.queryDb(para);
	}
}
