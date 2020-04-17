package cn.regionsoft.tutorial.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.regionsoft.one.annotation.Controller;
import cn.regionsoft.one.annotation.tag.Autowired;
import cn.regionsoft.one.annotation.tag.Cookies;
import cn.regionsoft.one.annotation.tag.HeaderInfo;
import cn.regionsoft.one.annotation.tag.Parameter;
import cn.regionsoft.one.annotation.tag.PathVariable;
import cn.regionsoft.one.annotation.tag.RequestMapping;
import cn.regionsoft.one.enums.RequestMethod;
import cn.regionsoft.tutorial.service.SampleService;

@Controller
@RequestMapping("sample")
public class SampleController {
	@Autowired
	private SampleService sampleService;
	
	/**
	 * Allowed default methods RequestMethod.POST,RequestMethod.GET
	 * Default Response header  {"Content-Type == application/json;charset=UTF-8"}
	 * http://127.0.1.1:8080/region/sample/hello?para=one
	 */
	@RequestMapping(value = "/hello")
    public String save(String para) {
		return "helloworld "+para;
	}
	
	/**
	 * Only allowed post method
	 * http://127.0.1.1:8080/region/sample/post-only
	 */
	@RequestMapping(value = "/post-only" ,method ={RequestMethod.POST},responseHeader= {"Content-Type == application/json;charset=UTF-8"})
    public String postOnly(String para) {
		return "helloworld "+para;
	}
	
	/**
	 * Reponse Header Content-Type == text/html
	 * http://127.0.1.1:8080/region/sample/text-res?para=aa
	 */
	@RequestMapping(value = "/text-res" ,method ={RequestMethod.POST,RequestMethod.GET},responseHeader= {"Content-Type == text/html;charset=UTF-8"})
    public String textResaponseOnly(String para) {
		return "helloworld "+sampleService.someBusinessLogic(para);
	}
	
	/**
	 * Multiple rewritten http headers
	 */
	@RequestMapping(value = "/multi-headers" ,method ={RequestMethod.POST,RequestMethod.GET},responseHeader= {"Content-Type == text/html;charset=UTF-8","MyHeader==headerContent"})
    public String multiAllowedHeaders(String para) {
		return "helloworld "+sampleService.someBusinessLogic(para);
	}
	
	/**
	 * http://127.0.1.1:8080/region/sample/full-attributes/book/case?para1=title&para2=content
	 */
	@RequestMapping(value = "/full-attributes/${path}/case" ,method ={RequestMethod.POST,RequestMethod.GET},responseHeader= {"Content-Type == application/json;charset=UTF-8"})
    public String attributes1(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@HeaderInfo Map<String,String> headerInfo,
    		@Cookies Map<String,String> cookies,
    		@Parameter(value = "para1") String para1,
    		@Parameter String para2,
    		@PathVariable(value = "path") String path) {
		StringBuilder sb = new StringBuilder();
		sb.append(para1);
		sb.append(",");
		sb.append(para2);
		sb.append(",");
		sb.append(path);
		return sb.toString();
	}
}
