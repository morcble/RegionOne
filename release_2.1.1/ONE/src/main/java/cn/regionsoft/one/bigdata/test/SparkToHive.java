package cn.regionsoft.one.bigdata.test;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.sql.SQLContext;
//
//
//public class SparkToHive {
///**
// * https://blog.csdn.net/u012164361/article/details/79742201
// * @param args
// */
//	public static void main(String[] args) {
//		SparkConf sparkConf = new SparkConf().setAppName("SparkHive").setMaster("local[2]");
//		JavaSparkContext sc = new JavaSparkContext(sparkConf);
//
//		// 不要使用SQLContext,部署异常找不到数据库和表
//		HiveContext hiveContext = new HiveContext(sc);
//		SQLContext sqlContext = new SQLContext(sc);
//		// 查询表前10条数据
//		hiveContext.sql("select * from bi_ods.owms_m_locator limit 10").show();
//
//		sc.stop();
//	}
//}
