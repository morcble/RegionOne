package cn.regionsoft.tutorial.service;

import cn.regionsoft.one.annotation.Service;
import cn.regionsoft.one.annotation.tag.Autowired;
import cn.regionsoft.tutorial.dao.SampleDao;

@Service
public class SampleService {
	@Autowired
	private SampleDao helloworldDao;
	
	public String someBusinessLogic(String para) {
		return helloworldDao.queryDb(para);
	}
}
