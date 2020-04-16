package cn.regionsoft.one.bigdata.comparators;

import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

public class StartstringComparator extends SubstringComparator {
	public StartstringComparator(String substr) {
		super(substr);
	}

	@Override
	public int compareTo(byte[] value, int offset, int length) {
		String subStr = Bytes.toString(getValue());
		return Bytes.toString(value, offset, length).toLowerCase().startsWith(subStr) ? 0 : 1;
	}
}
