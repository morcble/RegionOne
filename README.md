# RegionOne

Lightweight java server framework , much faster and easier.


Quick start:
  
  1.add declaration maven dependency in pom.xml of your project.
    <dependency>
  		<groupId>cn.regionsoft</groupId>
      <artifactId>ONE</artifactId>
      <version>2.1.1</version>
  	</dependency>
    
  2.add project configuration properties as config.properties in your classpath
    contexts=DefaultContext
    DefaultContext.context.paths = com.test<base package name of your project>
    
  3.add controller and entry method
  
    Helloworld.java:
      package com.hello;
      import cn.regionsoft.one.annotation.Controller;
      import cn.regionsoft.one.annotation.tag.RequestMapping;

      @Controller
      public class Helloworld {
        @RequestMapping(value = "/hello")
          public String save(String para) {
          return "helloworld "+para;
        }
      }
    Entry.java:
      import cn.regionsoft.one.core.RegionSoftServer;
      public class Entry {
        public static void main(String[] args) throws Exception {
          RegionSoftServer.start();
        }
      }
  
  4.run main method 
   try to access http://127.0.1.1:8080/region/hello?para=one
   
  you may also download the helloworld sample project from  https://github.com/morcble/RegionOne/tree/master/samples
  
  


