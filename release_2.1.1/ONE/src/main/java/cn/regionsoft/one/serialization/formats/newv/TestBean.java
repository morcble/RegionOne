package cn.regionsoft.one.serialization.formats.newv;

public class TestBean {
	private byte[] bb;
	private String name;
	private String desc;
	private TestBean2 testBean2;

	public byte[] getBb() {
		return bb;
	}

	public void setBb(byte[] bb) {
		this.bb = bb;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestBean2 getTestBean2() {
		return testBean2;
	}

	public void setTestBean2(TestBean2 testBean2) {
		this.testBean2 = testBean2;
	}
}
