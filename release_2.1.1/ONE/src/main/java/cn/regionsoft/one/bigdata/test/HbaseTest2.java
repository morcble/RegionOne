package cn.regionsoft.one.bigdata.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * rowkey=segmentSalt+”_”＋jobTime+”_”＋jobId＋”_”＋segmentId＋”_”＋dataIndexInSegment（前补零到定长5位）
 * https://yq.aliyun.com/articles/622299
 * 
 * https://github.com/dajobe/hbase-docker
 * https://www.cnblogs.com/zhenjing/p/hbase_example.html
 * @author fenglj
 *
 */
public class HbaseTest2 {
    public static Configuration configuration;
    public static Connection connection;
    public static Admin admin;

    public static void main(String[] args) throws IOException {
    	//scanData("t2", "rw1", "rw1000");
    	//if(true)return;
    	//HbaseTest2.getNumRegexRow("t2", "rw1", "rw2", "rw10", 3);
    	
    	//deleteTable("t2");
    	createTable("t2", new String[] { "cf1", "cf2" });
    	
    	init();
        Table table = connection.getTable(TableName.valueOf("t2"));
        
        long time = System.currentTimeMillis();
        String  colFamily= "cf1";
        String col = "q1";
        List<Put> putList = new ArrayList<Put>(); 
    	for(int i = 0 ; i <100000;i++) {
	        Put put = new Put(Bytes.toBytes("rw"+i));
	        put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes("val"+i));
	        table.put(put);
	        //putList.add(put);
    	}
    	//table.put(putList);
    	
    	System.out.println(System.currentTimeMillis()-time);
        // 批量插入
        /*
         * List<Put> putList = new ArrayList<Put>(); puts.add(put);
         * table.put(putList);
         */
    	
        table.close();
        close();

    	
    	//getData("t2", "rw1", "cf1", "q1");
		//scanData("t2", "rw1", "rw1000");
		//deleRow("t2", "rw1", "cf1", "q1");
		//deleteTable("t2");
       
    }

    // 初始化链接
    public static void init() {
        configuration = HBaseConfiguration.create();
        /*
         * configuration.set("hbase.zookeeper.quorum",
         * "10.10.3.181,10.10.3.182,10.10.3.183");
         * configuration.set("hbase.zookeeper.property.clientPort","2181");
         * configuration.set("zookeeper.znode.parent","/hbase");
         */
        /**
         * /etc/hosts 配置了172.17.0.2 hbase-docker hbase-docker
         */
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "hbase-docker");
        configuration.set("hbase.master", "hbase-docker:60000");
        File workaround = new File(".");
        System.getProperties().put("hadoop.home.dir",
                workaround.getAbsolutePath());
        new File("./bin").mkdirs();
        try {
            new File("./bin/winutils.exe").createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭连接
    public static void close() {
        try {
            if (null != admin)
                admin.close();
            if (null != connection)
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 建表
    public static void createTable(String tableNmae, String[] colFamilies) throws IOException {

        init();
        TableName tableName = TableName.valueOf(tableNmae);

        if (admin.tableExists(tableName)) {
            System.out.println("talbe is exists!");
        } else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
            for (String colFamily : colFamilies) {
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(colFamily);
                hTableDescriptor.addFamily(hColumnDescriptor);
            }
            admin.createTable(hTableDescriptor);
        }
        close();
    }

    // 删表
    public static void deleteTable(String tableName) throws IOException {
        init();
        TableName tn = TableName.valueOf(tableName);
        if (admin.tableExists(tn)) {
            admin.disableTable(tn);
            admin.deleteTable(tn);
        }
        close();
    }

    // 查看已有表
    public static void listTables() throws IOException {
        init();
        HTableDescriptor hTableDescriptors[] = admin.listTables();
        for (HTableDescriptor hTableDescriptor : hTableDescriptors) {
            System.out.println(hTableDescriptor.getNameAsString());
        }
        close();
    }

    // 插入数据
    public static void insterRow(String tableName, String rowkey, String colFamily, String col, String val)
            throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowkey));
        put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(val));
        table.put(put);

        // 批量插入
        /*
         * List<Put> putList = new ArrayList<Put>(); puts.add(put);
         * table.put(putList);
         */
        table.close();
        close();
    }

    // 删除数据
    public static void deleRow(String tableName, String rowkey, String colFamily, String col) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowkey));
        // 删除指定列族
        // delete.addFamily(Bytes.toBytes(colFamily));
        // 删除指定列
        // delete.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        table.delete(delete);
        // 批量删除
        /*
         * List<Delete> deleteList = new ArrayList<Delete>();
         * deleteList.add(delete); table.delete(deleteList);
         */
        table.close();
        close();
    }

    // 根据rowkey查找数据
    public static void getData(String tableName, String rowkey, String colFamily, String col) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Get get = new Get(Bytes.toBytes(rowkey));
        // 获取指定列族数据
        // get.addFamily(Bytes.toBytes(colFamily));
        // 获取指定列数据
        // get.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        Result result = table.get(get);

        showCell(result);
        table.close();
        close();
    }

    // 格式化输出
    public static void showCell(Result result) {
        Cell[] cells = result.rawCells();
        for (Cell cell : cells) {
            System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
            System.out.println("Timetamp:" + cell.getTimestamp() + " ");
            System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
            System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
            System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
        }
    }

    // 批量查找数据
    public static void scanData(String tableName, String startRow, String stopRow) throws IOException {
        init();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        // scan.setStartRow(Bytes.toBytes(startRow));
        // scan.setStopRow(Bytes.toBytes(stopRow));
        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            showCell(result);
        }
        table.close();
        close();
    }
    
    
    /**
    * 根据startRowKey和endRowKey筛选出区间，然后根据regxKey正则匹配和num查出最终的结果
    * @param tableName 表名
    * @param startRowKey 开始的范围
    * @param endRowKey 结束的范围
    * @param regxKey 正则匹配
    * @param num 查询的条数
    * @return List<Result>
    */
    public static List<Result> getNumRegexRow(String tableName,String startRowKey,String endRowKey, String regxKey,int num) {
    		init();
    		Table table = null;
            List<Result> list = null;
            try {
            	table = connection.getTable(TableName.valueOf(tableName));
                //创建一个过滤器容器，并设置其关系（AND/OR）
                FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
                //设置正则过滤器
                RegexStringComparator rc = new RegexStringComparator(regxKey);
                RowFilter rf = new RowFilter(CompareOp.EQUAL, rc);
                //过滤获取的条数
                Filter filterNum = new PageFilter(num);//每页展示条数
                
                //过滤器的添加
                fl.addFilter(rf);
                fl.addFilter(filterNum);
                Scan scan = new Scan();
                //设置取值范围
                scan.setStartRow(startRowKey.getBytes());//开始的key
                scan.setStopRow(endRowKey.getBytes());//结束的key
                scan.setFilter(fl);//为查询设置过滤器的list
                ResultScanner scanner = table.getScanner(scan) ;
                list = new ArrayList<Result>() ;
                for (Result rs : scanner) {
                    list.add(rs) ;
                }
            } catch (Exception e) {
                e.printStackTrace() ;
            }
            finally
            {
                try {
                    table.close() ;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (Result result : list) {
                showCell(result);
            }
            close();
            return list;
        }

}