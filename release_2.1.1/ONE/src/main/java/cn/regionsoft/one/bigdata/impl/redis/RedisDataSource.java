package cn.regionsoft.one.bigdata.impl.redis;
//package cn.regionsoft.bigdata.impl.redis;
//
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//import cn.regionsoft.bigdata.core.DataSource;
//import cn.regionsoft.bigdata.core.object.RDColumn;
//import cn.regionsoft.bigdata.core.object.RDSchema;
//import cn.regionsoft.bigdata.core.object.RDTable;
//import cn.regionsoft.bigdata.enums.DataType;
//
//import redis.clients.jedis.JedisPoolConfig;
//import redis.clients.jedis.JedisShardInfo;
//import redis.clients.jedis.ShardedJedis;
//import redis.clients.jedis.ShardedJedisPool;
//import redis.clients.util.Hashing;
//import redis.clients.util.Sharded;
//
//public class RedisDataSource implements DataSource<RedisDataSpace> {
//	//线程安全
//	private ShardedJedis shardedJedis;
//	
//	public RedisDataSource() {
//		init();
//	}
//
//	private void init() {
//		JedisPoolConfig config =new JedisPoolConfig();//Jedis池配置
//		config.setMaxIdle(8);//对象最大空闲时间
//		config.setMaxWaitMillis(-1);//获取对象时最大等待时间
//		config.setTestOnBorrow(true);
//		config.setMinIdle(1);
//		config.setMaxTotal(8);
//		
//		int dbIndex = 0;
//		List<JedisShardInfo> redisNodeList =new ArrayList<JedisShardInfo>();
//        JedisShardInfo redisNode1 = new JedisShardInfo(URI.create("redis://172.16.160.135:6379/"+dbIndex));
//        //redisNode1.setPassword("12345");
//        redisNodeList.add(redisNode1);
//        
//        
//        JedisShardInfo redisNode2 = new JedisShardInfo(URI.create("redis://172.16.160.135:6378/"+dbIndex));
//        redisNode2.setPassword("123456");
//        redisNodeList.add(redisNode2);
//       
//        
//        ShardedJedisPool pool = new ShardedJedisPool(config, redisNodeList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
//        shardedJedis = pool.getResource();
//        
//	}
//
//	@Override
//	public RedisDataSpace createDataSpaceIfNotExists(String collectionName) throws Exception {
//		return new RedisDataSpace(shardedJedis);
//	}
//
//	@Override
//	public void close() {
//		shardedJedis.close();
//	}
//
//	@Override
//	public void createRDSchema(String schemaNm) throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public RDSchema getRDSchema(String rdSchemaNm) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void dropRDSchema(String rdSchemaNm) throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void createRdTable(String rdSchemaNm, String rdTableName) throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public RDTable getRdTable(String rdSchemaNm, String rdTableNm) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void createRdColumn(String schemaNm, String rdTableNm, String columnNm,DataType dataType) throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public List<RDColumn> getRdTableColumns(String rdSchemaNm, String rdTableNm) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void printAllData() throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//}
