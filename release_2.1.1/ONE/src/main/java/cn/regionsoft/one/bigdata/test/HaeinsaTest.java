package cn.regionsoft.one.bigdata.test;

import org.apache.hadoop.hbase.util.Bytes;
import kr.co.vcnc.haeinsa.HaeinsaPut;
import kr.co.vcnc.haeinsa.HaeinsaTableIface;
import kr.co.vcnc.haeinsa.HaeinsaTransaction;
import kr.co.vcnc.haeinsa.HaeinsaTransactionManager;

public class HaeinsaTest {
//	public static void main(String[] args) {
//		HaeinsaTransactionManager tm = new HaeinsaTransactionManager(tablePool);
//		HaeinsaTableIface table = tablePool.getTable("test");
//		byte[] family = Bytes.toBytes("data");
//		byte[] qualifier = Bytes.toBytes("status");
//
//		HaeinsaTransaction tx = tm.begin(); // start transaction
//
//		HaeinsaPut put1 = new HaeinsaPut(Bytes.toBytes("user1"));
//		put1.add(family, qualifier, Bytes.toBytes("Hello World!"));
//		table.put(tx, put1);
//
//		HaeinsaPut put2 = new HaeinsaPut(Bytes.toBytes("user2"));
//		put2.add(family, qualifier, Bytes.toBytes("Linearly Scalable!"));
//		table.put(tx, put2);
//
//		tx.commit(); // commit transaction to HBase
//	}
}
