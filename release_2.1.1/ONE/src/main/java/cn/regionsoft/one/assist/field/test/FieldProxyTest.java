package cn.regionsoft.one.assist.field.test;

import cn.regionsoft.one.assist.field.FieldsProxy;
import cn.regionsoft.one.assist.field.FieldsProxyAssist;

public class FieldProxyTest {

	public static void main(String[] args) throws Exception {
		FieldSample sample = new FieldSample();
		sample.setA("asfsdf");
		sample.setB1(123);
		FieldsProxy proxy = FieldsProxyAssist.genFieldProxy(sample.getClass(),"a");
		System.out.println(proxy.getVal(sample));
		proxy = FieldsProxyAssist.genFieldProxy(sample.getClass(),"B1");
		System.out.println(proxy.getVal(sample));
	}

}
