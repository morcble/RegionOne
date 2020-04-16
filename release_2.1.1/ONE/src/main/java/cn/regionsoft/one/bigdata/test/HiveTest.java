package cn.regionsoft.one.bigdata.test;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
 
public class HiveTest {
     
    private static String driverName ="org.apache.hive.jdbc.HiveDriver";
   
    public static void main(String[] args)
                            throws SQLException {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
         
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.18.148:10000/default", "hadoop", "s3cret");
        Statement stmt = con.createStatement();
        String tableName = "KYLIN_SALES";
       // stmt.execute("drop table if exists " + tableName);
      //  stmt.execute("create table " + tableName +" (key int, value string)");
        System.out.println("Create table success!");
        // show tables
        String sql = "show tables '" + tableName + "'";
        System.out.println("Running: " + sql);
        ResultSet res = stmt.executeQuery(sql);
        if (res.next()) {
            System.out.println(res.getString(1));
        }
 
        // describe table
        sql = "describe " + tableName;
        System.out.println("Running: " + sql);
        res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(res.getString(1) + "\t" + res.getString(2));
        }
 
       /* sql = "insert into TABLE fenglj  (key, value) values (111, \"aaa\")";
        System.out.println("Running: " + sql);
        stmt.execute(sql);*/
       // res = stmt.executeQuery(sql);
        
        sql = "select * from " + tableName;
        System.out.println("Running: " + sql);
        res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(String.valueOf(res.getInt(1)) + "\t"+ res.getString(2));
        }
 
        sql = "select count(*) from " + tableName;
        System.out.println("Running: " + sql);
        res = stmt.executeQuery(sql);
        while (res.next()) {
            System.out.println(res.getString(1));
        }
    }
}
