package cn.regionsoft.one.core.contextinfo.dto;

import java.util.ArrayList;
import java.util.List;

public class SystemContextDetail{
	private List<H2OContextDetail> h2oContextDetails = new ArrayList<H2OContextDetail>();
	
	public void addH2OContextDetail(H2OContextDetail h2oContextDetail){
		h2oContextDetails.add(h2oContextDetail);
	}
}
