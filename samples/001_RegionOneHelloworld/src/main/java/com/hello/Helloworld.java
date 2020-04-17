package com.hello;

import cn.regionsoft.one.annotation.Controller;
import cn.regionsoft.one.annotation.tag.RequestMapping;

@Controller
public class Helloworld {
	@RequestMapping(value = "/hello")
    public String save(String para) {
		return "helloworld "+para;
	}
}
