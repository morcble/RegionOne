package cn.regionsoft.one.bigdata.impl.redis;
//package cn.regionsoft.bigdata.impl.redis;
//
//import cn.regionsoft.bigdata.core.DataSpace;
//import redis.clients.jedis.ShardedJedis;
//
//public class RedisDataSpace implements DataSpace<ShardedJedis>{
//	private ShardedJedis data;
//	private boolean isCache = false;
//	
//	public RedisDataSpace(ShardedJedis data) {
//		this.data = data;
//	}
//
//	@Override
//	public ShardedJedis getData() {
//		return data;
//	}
//
//	@Override
//	public void close() {
//		data.close();
//	}
//
//
//	@Override
//	public boolean isInBatch() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public void setInBatch(boolean inBatch) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//}
