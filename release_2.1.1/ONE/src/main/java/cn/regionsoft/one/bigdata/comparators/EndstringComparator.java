package cn.regionsoft.one.bigdata.comparators;

import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

public class EndstringComparator extends SubstringComparator {
	public EndstringComparator(String substr) {
		super(substr);
	}

	@Override
	public int compareTo(byte[] value, int offset, int length) {
		String subStr = Bytes.toString(getValue());
		return Bytes.toString(value, offset, length).toLowerCase().endsWith(subStr) ? 0 : 1;
	}
}
