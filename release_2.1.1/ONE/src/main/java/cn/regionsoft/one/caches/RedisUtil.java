package cn.regionsoft.one.caches;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.properties.ConfigUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.util.Hashing;
import redis.clients.jedis.util.Sharded;



public class RedisUtil {
	public static Logger logger = Logger.getLogger(RedisUtil.class);
	
	private static volatile boolean inited = false;
	
	public static boolean isInited() {
		return inited;
	}
	public static ShardedJedisPool pool() {
		return shardedJedisPool;
	}
	private static ShardedJedisPool shardedJedisPool;
	static {
		boolean enableRedis = Boolean.valueOf(ConfigUtil.getProperty("redis.enabled"));
		if(enableRedis) {
			String redisHost= ConfigUtil.getProperty("redis.host");
			if(!CommonUtil.isEmpty(redisHost)) {
				try {
					logger.debug("redis configuration found.");
					logger.debug("redis initing...");
					
					JedisPoolConfig config =new JedisPoolConfig();//Jedis池配置
					config.setTestOnBorrow(true);
					config.setMaxIdle(Integer.valueOf(ConfigUtil.getProperty("redis.max.idle","8")));//对象最大空闲时间
					config.setMaxWaitMillis(Integer.valueOf(ConfigUtil.getProperty("redis.max.waitmillis","60000")));//获取对象时最大等待时间
					config.setMinIdle(Integer.valueOf(ConfigUtil.getProperty("redis.min.idle","1")));
					config.setMaxTotal(Integer.valueOf(ConfigUtil.getProperty("redis.max.total","8")));
					int dbIndex = Integer.valueOf(ConfigUtil.getProperty("redis.db","0"));
					String[] hostsArray = redisHost.split(",");
					
					List<JedisShardInfo> redisNodeList =new ArrayList<JedisShardInfo>();
					
					for(String host:hostsArray) {
						JedisShardInfo redisNode = new JedisShardInfo(URI.create("redis://"+host+"/"+dbIndex));
				        redisNode.setPassword(ConfigUtil.getProperty("redis.password"));
				        redisNodeList.add(redisNode);
					}		
			       
			        shardedJedisPool = new ShardedJedisPool(config, redisNodeList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
			        
			        //测试连接
			        RedisUtil.constainKey("test");
			        
			        logger.debug("redis inited...");
			        
			        inited = true;
			        
			        /**
			         * 选择DB
			         */
//					for (Jedis shardedJedis : pool.getResource().getAllShards()) {
//						shardedJedis.select(1);
//					}
				} catch (Exception e) {
					logger.error(e);
				}
				
			}
		}
	}
	
	public static void init() {}//donothing
	
    
	public static boolean constainKey(final String key) {
		return new Executor<Boolean>() {
			@Override
			Boolean execute() {
				return shardedJedis.exists(key);
			}
		}.getResult();
	}
	/**
	 * 为给定 key 设置生存时间
	 * @param expire   秒
	 */
	public static Long expire(final String key, final int expire) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.expire(key, expire);
			}
		}.getResult();
	}
	
	/**
	 * @param expire   秒
	 * @return
	 */
	public static String set(final String key, final String value, final int expire) {
		return new Executor<String>() {
			@Override
			String execute() {
				return shardedJedis.setex(key, expire, value);
			}
		}.getResult();
	}
	
	public static Boolean setLong(final String key, final long value, final int seconds) {
		return new Executor<Boolean>() {
			@Override
			Boolean execute() {
				try {
					Jedis jedis = shardedJedis.getShard(key);
					Transaction transaction = jedis.multi();
					transaction.set(key, value+Constants.EMPTY_STR);
					transaction.expire(key, seconds);
					transaction.exec();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}.getResult();
	}
	
	public static Boolean setLong(final String key, final long value) {
		return new Executor<Boolean>() {
			@Override
			Boolean execute() {
				try {
					shardedJedis.set(key, value+Constants.EMPTY_STR);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}.getResult();
	}
	
	/**
	 * 设置默认失效
	 * @param key
	 * @return
	 */
	public static String setWithExpire(final String key, final String value) {
		return new Executor<String>() {
			@Override
			String execute() {
				return shardedJedis.setex(key, Integer.parseInt(ConfigUtil.getProperty("redis.expire.default","360000")), value);
			}
		}.getResult();
	}
	
	/**
	 * 把map的值作为一次事务插入
	 * @param map
	 * @return
	 */
	public static List<String> setTranMapWithExpire(final Map<String,String> map) {
		return new Executor<List<String>>() {
			@Override
			List<String> execute() {
				if(map.size()==0)return null;
				
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				
				Iterator<Entry<String,String>> iterator = map.entrySet().iterator();
				Entry<String,String> tmpEntry = null;
				List<Response<String>> responses = new ArrayList<Response<String>>(map.size());
				List<String> result = new ArrayList<String>(map.size());
				while(iterator.hasNext()) {
					tmpEntry = iterator.next();
					responses.add(pipeline.setex(tmpEntry.getKey(), Integer.parseInt(ConfigUtil.getProperty("redis.expire.default","360000")), tmpEntry.getValue()));
				}
				pipeline.sync();
				
				for (Response<String> resp : responses) {
					result.add(resp.get());
				}
				
				return result;
			}
		}.getResult();
	}
	
    
    public static String set(final String key,final String value) {
		return new Executor<String>() {
			@Override
			String execute() {
				return shardedJedis.set(key,value);
			}
		}.getResult();
	}
    
    public static Long setIfNotExists(final String key, final String value) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.setnx(key, value);
			}
		}.getResult();
	}
    
    public static String get(final String key) {
		return new Executor<String>() {
			@Override
			String execute() {
				return shardedJedis.get(key);
			}
		}.getResult();
	}
    
    /**
     * 获取缓存 并刷新缓存生存时间
     * @param key
     * @return
     */
    public static String getAndRefresh(final String key) {
		return new Executor<String>() {
			@Override
			String execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				Response<String> response = pipeline.get(key);
				pipeline.expire(key, Integer.parseInt(ConfigUtil.getProperty("redis.expire.default","360000")));
				pipeline.sync();
				return response.get();
			}
		}.getResult();
	}
    
    /**
     * 自增ID生成器
     */
    public static long genId(final String key) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				long id = shardedJedis.incr(key);
				if ((id + 75807) >= Long.MAX_VALUE) {
					// 避免溢出，重置，getSet命令之前允许incr插队，75807就是预留的插队空间
					shardedJedis.getSet(key, "0");
				}
				return id;
			}
		}.getResult();
	}
    
    /**
     * 给key的值增加increment
     * @param key
     * @param increment
     * @return
     */
    public static long incrBy(final String key,int increment) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.incrBy(key, increment);
			}
		}.getResult();
	}
    
    /**
     * 给key的值减少increment
     * @param key
     * @param increment
     * @return
     */
    public static long decrBy(final String key,int decrement) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.decrBy(key, decrement);
			}
		}.getResult();
	}

    
    /**
	 * 删除模糊匹配的key
	 * @param likeKey 模糊匹配的key,比如以*结尾
	 * @return 删除成功的条数
	 */
	public static long delKeysLike(final String likeKey) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				Collection<Jedis> shardedJedisC = shardedJedis.getAllShards();
				Iterator<Jedis> iter = shardedJedisC.iterator();
				long count = 0;
				while (iter.hasNext()) {
					Jedis jedis = iter.next();
					Set<String> keys = jedis.keys(likeKey);
					if(keys.size()>0) {
						count += jedis.del(keys.toArray(new String[keys.size()]));
					}	
				}
				return count;
			}
		}.getResult();
	}

	/**
	 * 删除
	 * @param key 匹配的key
	 * @return 删除成功的条数
	 */
	public static Long delKey(final String key) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.del(key);
			}
		}.getResult();
	}

	/**
	 * 删除
	 * @param keys 匹配的key的集合
	 * @return 删除成功的条数
	 */
	public static Long delKeys(final String[] keys) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				Collection<Jedis> shardedJedisC = shardedJedis.getAllShards();
				Iterator<Jedis> iter = shardedJedisC.iterator();
				long count = 0;
				while (iter.hasNext()) {
					Jedis jedis = iter.next();
					count += jedis.del(keys);
				}
				return count;
			}
		}.getResult();
	}
	
	/**
	 * 批量的 {@link #setString(String, String)}
	 * @param pairs 键值对数组{数组第一个元素为key，第二个元素为value}
	 * @return 操作状态的集合
	 */
	public static List<Object> batchSet(final List<Pair<String, String>> pairs) {
		return new Executor<List<Object>>() {
			@Override
			List<Object> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				for (Pair<String, String> pair : pairs) {
					pipeline.set(pair.getKey(), pair.getValue());
				}
				return pipeline.syncAndReturnAll();
			}
		}.getResult();
	}
	
	/**
	 * 批量的 {@link #getString(String)}
	 * @param keys key数组
	 * @return value的集合
	 */
	public static List<String> batchGet(final String[] keys) {
		return new Executor<List<String>>() {
			@Override
			List<String> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				List<String> result = new ArrayList<String>(keys.length);
				List<Response<String>> responses = new ArrayList<Response<String>>(keys.length);
				for (String key : keys) {
					responses.add(pipeline.get(key));
				}
				pipeline.sync();
				for (Response<String> resp : responses) {
					result.add(resp.get());
				}
				return result;
			}
		}.getResult();
	}
	
	/* ======================================Hashes====================================== */

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 * 如果 key 不存在，一个新的哈希表被创建并进行 hashSet 操作。
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
	 * 时间复杂度: O(1)
	 * @param key key
	 * @param field 域
	 * @param value string value
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
	 */
	public static Long hashSet(final String key, final String field, final String value) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.hset(key, field, value);
			}
		}.getResult();
	}
	
	public static Long hashDelete(final String key, String... fields) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.hdel(key, fields);
			}
		}.getResult();
	}
	
	

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 * 如果 key 不存在，一个新的哈希表被创建并进行 hashSet 操作。
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
	 * @param key key
	 * @param field 域
	 * @param value string value
	 * @param expire 生命周期，单位为秒
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
	 */
	public static Long hashSet(final String key, final String field, final String value, final int expire) {
		return new Executor<Long>() {

			@Override
			Long execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<Long> result = pipeline.hset(key, field, value);
				pipeline.expire(key, expire);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 * 时间复杂度:O(1)
	 * @param key key
	 * @param field 域
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
	 */
	public static String hashGet(final String key, final String field) {
		return new Executor<String>() {

			@Override
			String execute() {
				return shardedJedis.hget(key, field);
			}
		}.getResult();
	}

	/**
	 * 返回哈希表 key 中给定域 field 的值。 如果哈希表 key 存在，同时设置这个 key 的生存时间
	 * @param key key
	 * @param field 域
	 * @param expire 生命周期，单位为秒
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
	 */
	public static String hashGet(final String key, final String field, final int expire) {
		return new Executor<String>() {

			@Override
			String execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<String> result = pipeline.hget(key, field);
				pipeline.expire(key, expire);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
	 * 时间复杂度: O(N) (N为fields的数量)
	 * @param key key
	 * @param hash field-value的map
	 * @return 如果命令执行成功，返回 OK 。当 key 不是哈希表(hash)类型时，返回一个错误。
	 */
	public static String hashMultipleSet(final String key, final Map<String, String> hash) {
		return new Executor<String>() {

			@Override
			String execute() {
				if(hash.size()==0) return "0";
				return shardedJedis.hmset(key, hash);
			}
		}.getResult();
	}
	
	/**
	 * scan hash table
	 * @param key
	 * @param cursor
	 * @param scanParams
	 * @return
	 */
	public static ScanResult<Entry<String, String>> hashScan(final String key, final String cursor,final ScanParams scanParams) {
		return new Executor<ScanResult<Entry<String, String>>>() {
			@Override
			ScanResult<Entry<String, String>> execute() {
				return shardedJedis.hscan(key, cursor,scanParams);
			}
		}.getResult();
	}
	
	
	/**
	 * @param key
	 * @param adviceReturnCount  建议返回的个数,和实际返回个数不一定相等
	 * @return
	 */
	public static ScanResult<Entry<String, String>> hashScan(final String key, final int adviceReturnCount) {
		return new Executor<ScanResult<Entry<String, String>>>() {
			@Override
			ScanResult<Entry<String, String>> execute() {
				ScanParams scanParams = new ScanParams();
				scanParams = scanParams.count(adviceReturnCount);
				return shardedJedis.hscan(key, ScanParams.SCAN_POINTER_START,scanParams);
			}
		}.getResult();
	}

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。同时设置这个 key 的生存时间
	 * @param key key
	 * @param hash field-value的map
	 * @param expire 生命周期，单位为秒
	 * @return 如果命令执行成功，返回 OK 。当 key 不是哈希表(hash)类型时，返回一个错误。
	 */
	public static String hashMultipleSet(final String key, final Map<String, String> hash, final int expire) {
		return new Executor<String>() {

			@Override
			String execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<String> result = pipeline.hmset(key, hash);
				pipeline.expire(key, expire);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。如果给定的域不存在于哈希表，那么返回一个 nil 值。
	 * 时间复杂度: O(N) (N为fields的数量)
	 * @param key key
	 * @param fields field的数组
	 * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 */
	public static List<String> hashMultipleGet(final String key, final String... fields) {
		return new Executor<List<String>>() {

			@Override
			List<String> execute() {
				return shardedJedis.hmget(key, fields);
			}
		}.getResult();
	}

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。如果给定的域不存在于哈希表，那么返回一个 nil 值。
	 * 同时设置这个 key 的生存时间
	 * @param key key
	 * @param fields field的数组
	 * @param expire 生命周期，单位为秒
	 * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 */
	public static List<String> hashMultipleGet(final String key, final int expire, final String... fields) {
		return new Executor<List<String>>() {

			@Override
			List<String> execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<List<String>> result = pipeline.hmget(key, fields);
				pipeline.expire(key, expire);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

	/**
	 * 批量的{@link #hashMultipleSet(String, Map)}，在管道中执行
	 * @param pairs 多个hash的多个field
	 * @return 操作状态的集合
	 */
	public static List<Object> batchHashMultipleSet(final List<Pair<String, Map<String, String>>> pairs) {
		return new Executor<List<Object>>() {

			@Override
			List<Object> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				for (Pair<String, Map<String, String>> pair : pairs) {
					pipeline.hmset(pair.getKey(), pair.getValue());
				}
				return pipeline.syncAndReturnAll();
			}
		}.getResult();
	}

	/**
	 * 批量的{@link #hashMultipleSet(String, Map)}，在管道中执行
	 * @param data Map<String, Map<String, String>>格式的数据
	 * @return 操作状态的集合
	 */
	public static List<Object> batchHashMultipleSet(final Map<String, Map<String, String>> data) {
		return new Executor<List<Object>>() {

			@Override
			List<Object> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				for (Map.Entry<String, Map<String, String>> iter : data.entrySet()) {
					pipeline.hmset(iter.getKey(), iter.getValue());
				}
				return pipeline.syncAndReturnAll();
			}
		}.getResult();
	}

	/**
	 * 批量的{@link #hashMultipleGet(String, String...)}，在管道中执行
	 * @param pairs 多个hash的多个field
	 * @return 执行结果的集合
	 */
	public static List<List<String>> batchHashMultipleGet(final List<Pair<String, String[]>> pairs) {
		return new Executor<List<List<String>>>() {

			@Override
			List<List<String>> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				List<List<String>> result = new ArrayList<List<String>>(pairs.size());
				List<Response<List<String>>> responses = new ArrayList<Response<List<String>>>(pairs.size());
				for (Pair<String, String[]> pair : pairs) {
					responses.add(pipeline.hmget(pair.getKey(), pair.getValue()));
				}
				pipeline.sync();
				for (Response<List<String>> resp : responses) {
					result.add(resp.get());
				}
				return result;
			}
		}.getResult();

	}

	/**
	 * 返回哈希表 key 中，所有的域和值。在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
	 * 时间复杂度: O(N)
	 * @param key key
	 * @return 以列表形式返回哈希表的域和域的值。若 key 不存在，返回空列表。
	 */
	public static Map<String, String> hashGetAll(final String key) {
		return new Executor<Map<String, String>>() {

			@Override
			Map<String, String> execute() {
				return shardedJedis.hgetAll(key);
			}
		}.getResult();
	}

	/**
	 * 返回哈希表 key 中，所有的域和值。在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
	 * 同时设置这个 key 的生存时间
	 * @param key key
	 * @param expire 生命周期，单位为秒
	 * @return 以列表形式返回哈希表的域和域的值。若 key 不存在，返回空列表。
	 */
	public static Map<String, String> hashGetAll(final String key, final int expire) {
		return new Executor<Map<String, String>>() {

			@Override
			Map<String, String> execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<Map<String, String>> result = pipeline.hgetAll(key);
				pipeline.expire(key, expire);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

	/**
	 * 批量的{@link #hashGetAll(String)}
	 * @param keys key的数组
	 * @return 执行结果的集合
	 */
	public static List<Map<String, String>> batchHashGetAll(final String... keys) {
		return new Executor<List<Map<String, String>>>() {

			@Override
			List<Map<String, String>> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				List<Map<String, String>> result = new ArrayList<Map<String, String>>(keys.length);
				List<Response<Map<String, String>>> responses = new ArrayList<Response<Map<String, String>>>(keys.length);
				for (String key : keys) {
					responses.add(pipeline.hgetAll(key));
				}
				pipeline.sync();
				for (Response<Map<String, String>> resp : responses) {
					result.add(resp.get());
				}
				return result;
			}
		}.getResult();
	}

	/**
	 * 批量的{@link #hashMultipleGet(String, String...)}，与{@link #batchHashGetAll(String...)}不同的是，返回值为Map类型
	 * @param keys key的数组
	 * @return 多个hash的所有filed和value
	 */
	public static Map<String, Map<String, String>> batchHashGetAllForMap(final String... keys) {
		return new Executor<Map<String, Map<String, String>>>() {

			@Override
			Map<String, Map<String, String>> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();

				// 设置map容量防止rehash
				int capacity = 1;
				while ((int) (capacity * 0.75) <= keys.length) {
					capacity <<= 1;
				}
				Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>(capacity);
				List<Response<Map<String, String>>> responses = new ArrayList<Response<Map<String, String>>>(keys.length);
				for (String key : keys) {
					responses.add(pipeline.hgetAll(key));
				}
				pipeline.sync();
				for (int i = 0; i < keys.length; ++i) {
					result.put(keys[i], responses.get(i).get());
				}
				return result;
			}
		}.getResult();
	}

	/* ======================================List====================================== */

	/**
	 * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
	 * @param key key
	 * @param values value的数组
	 * @return 执行 listPushTail 操作后，表的长度
	 */
	public static Long listPushTail(final String key, final String... values) {
		return new Executor<Long>() {

			@Override
			Long execute() {
				return shardedJedis.rpush(key, values);
			}
		}.getResult();
	}

	/**
	 * 将一个或多个值 value 插入到列表 key 的表头
	 * @param key key
	 * @param value string value
	 * @return 执行 listPush 命令后，列表的长度。
	 */
	public static Long listPush(final String key, final String value) {
		return new Executor<Long>() {

			@Override
			Long execute() {
				return shardedJedis.lpush(key, value);
			}
		}.getResult();
	}
	
	
	/**
	 * 将一个或多个值 value 插入到列表 key 的表头, 当列表大于指定长度是就对列表进行修剪(trim)
	 * @param key key
	 * @param value string value
	 * @param size 链表超过这个长度就修剪元素
	 * @return 执行 listPushAndTrim 命令后，列表的长度。
	 */
	public static Long listPushAndTrim(final String key, final String value, final long size) {
		return new Executor<Long>() {

			@Override
			Long execute() {
				Pipeline pipeline = shardedJedis.getShard(key).pipelined();
				Response<Long> result = pipeline.lpush(key, value);
				// 修剪列表元素, 如果 size - 1 比 end 下标还要大，Redis将 size 的值设置为 end 。
				pipeline.ltrim(key, 0, size - 1);
				pipeline.sync();
				return result.get();
			}
		}.getResult();
	}

//	/**
//	 * 批量的{@link #listPushTail(String, String...)}，以锁的方式实现
//	 * @param key key
//	 * @param values value的数组
//	 * @param delOld 如果key存在，是否删除它。true 删除；false: 不删除，只是在行尾追加
//	 */
//	public static void batchListPushTail(final String key, final String[] values, final boolean delOld) {
//		new Executor<Object>() {
//
//			@Override
//			Object execute() {
//				if (delOld) {
//					RedisLock lock = new RedisLock(key, shardedJedisPool);
//					lock.lock();
//					try {
//						Pipeline pipeline = shardedJedis.getShard(key).pipelined();
//						pipeline.del(key);
//						for (String value : values) {
//							pipeline.rpush(key, value);
//						}
//						pipeline.sync();
//					} finally {
//						lock.unlock();
//					}
//				} else {
//					shardedJedis.rpush(key, values);
//				}
//				return null;
//			}
//		}.getResult();
//	}

	/**
	 * 同{@link #batchListPushTail(String, String[], boolean)},不同的是利用redis的事务特性来实现
	 * @param key key
	 * @param values value的数组
	 * @return null
	 */
	public static Object updateListInTransaction(final String key, final List<String> values) {
		return new Executor<Object>() {

			@Override
			Object execute() {
				Transaction transaction = shardedJedis.getShard(key).multi();
				transaction.del(key);
				for (String value : values) {
					transaction.rpush(key, value);
				}
				transaction.exec();
				return null;
			}
		}.getResult();
	}

//	/**
//	 * 在key对应list的尾部部添加字符串元素,如果key存在，什么也不做
//	 * @param key key
//	 * @param values value的数组
//	 * @return 执行insertListIfNotExists后，表的长度
//	 */
//	public static Long insertListIfNotExists(final String key, final String[] values) {
//		return new Executor<Long>() {
//
//			@Override
//			Long execute() {
//				RedisLock lock = new RedisLock(key, shardedJedisPool);
//				lock.lock();
//				try {
//					if (!shardedJedis.exists(key)) {
//						return shardedJedis.rpush(key, values);
//					}
//				} finally {
//					lock.unlock();
//				}
//				return 0L;
//			}
//		}.getResult();
//	}

	/**
	 * 返回list所有元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素，key不存在返回空列表
	 * @param key key
	 * @return list所有元素
	 */
	public static List<String> listGetAll(final String key) {
		return new Executor<List<String>>() {

			@Override
			List<String> execute() {
				return shardedJedis.lrange(key, 0, -1);
			}
		}.getResult();
	}
	
	


	/**
	 * 返回指定区间内的元素，下标从0开始，负值表示从后面计算，-1表示倒数第一个元素，key不存在返回空列表
	 * @param key key
	 * @param beginIndex 下标开始索引（包含）
	 * @param endIndex 下标结束索引（不包含）
	 * @return 指定区间内的元素
	 */
	public static List<String> listRange(final String key, final long beginIndex, final long endIndex) {
		return new Executor<List<String>>() {

			@Override
			List<String> execute() {
				return shardedJedis.lrange(key, beginIndex, endIndex - 1);
			}
		}.getResult();
	}

	/**
	 * 一次获得多个链表的数据
	 * @param keys key的数组
	 * @return 执行结果
	 */
	public static Map<String, List<String>> batchGetAllList(final List<String> keys) {
		return new Executor<Map<String, List<String>>>() {

			@Override
			Map<String, List<String>> execute() {
				ShardedJedisPipeline pipeline = shardedJedis.pipelined();
				Map<String, List<String>> result = new HashMap<String, List<String>>();
				List<Response<List<String>>> responses = new ArrayList<Response<List<String>>>(keys.size());
				for (String key : keys) {
					responses.add(pipeline.lrange(key, 0, -1));
				}
				pipeline.sync();
				for (int i = 0; i < keys.size(); ++i) {
					result.put(keys.get(i), responses.get(i).get());
				}
				return result;
			}
		}.getResult();
	}
	
	/**
	 * 将一个或多个值 value 插入到列表 key 的表头
	 * @param key key
	 * @param value string value
	 * @return 执行 listPush 命令后，列表的长度。
	 */
	public static String lpoppush(final String key) {
		return new Executor<String>() {
			//return shardedJedis.rpush(key, values);
			@Override
			String execute() {
				if (!shardedJedis.exists(key)){  
		                return null;  
				}
				return shardedJedis.lpop(key);
			}
		}.getResult();
	}

	/* ======================================Pub/Sub====================================== */

	/**
	 * 将信息 message 发送到指定的频道 channel。
	 * 时间复杂度：O(N+M)，其中 N 是频道 channel 的订阅者数量，而 M 则是使用模式订阅(subscribed patterns)的客户端的数量。
	 * @param channel 频道
	 * @param message 信息
	 * @return 接收到信息 message 的订阅者数量。
	 */
	public static Long publish(final String channel, final String message) {
		return new Executor<Long>() {

			@Override
			Long execute() {
				Jedis jedis = shardedJedis.getShard(channel);
				return jedis.publish(channel, message);
			}
			
		}.getResult();
	}

	/**
	 * 订阅给定的一个频道的信息。
	 * @param shardedJedisPubSub 监听器
	 * @param channel 频道
	 */
	public static void subscribe(final JedisPubSub shardedJedisPubSub, final String channel) {
		new Executor<Object>() {

			@Override
			Object execute() {
				Jedis jedis = shardedJedis.getShard(channel);
				// 注意subscribe是一个阻塞操作，因为当前线程要轮询Redis的响应然后调用subscribe
				jedis.subscribe(shardedJedisPubSub, channel);
				return null;
			}
		}.getResult();
	}

	/**
	 * 取消订阅
	 * @param shardedJedisPubSub 监听器
	 */
	public static void unSubscribe(final JedisPubSub shardedJedisPubSub) {
		shardedJedisPubSub.unsubscribe();
	}

	/* ======================================Sorted set================================= */

	/**
	 * 将一个 member 元素及其 score 值加入到有序集 key 当中。
	 * @param key key
	 * @param score score 值可以是整数值或双精度浮点数。
	 * @param member 有序集的成员
	 * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
	 */
	public static Long addSortedSet(final String key, final double score, final String member) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.zadd(key, score, member);
			}
		}.getResult();
	}
	
	/**
	 * 将多个 member 元素及其 score 值加入到有序集 key 当中。
	 * @param key key
	 * @param scoreMembers score、member的pair
	 * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
	 */
	public static Long addSortedSetWithMap(final String key, final Map<String, Double> scoreMembers) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				if(scoreMembers.size()==0)return 0l;
				return shardedJedis.zadd(key, scoreMembers);
			}
		}.getResult();
	}
	
	/**
	 * 删除sorted set里的一些键
	 * @param key
	 * @param members
	 * @return
	 */
	public static Long deleteSortedSet(final String key, String... members) {
		return new Executor<Long>() {
			@Override
			Long execute() {
				return shardedJedis.zrem(key, members);
			}
		}.getResult();
	}
	
	public static Double getScoreFromSortedSet(final String key, String member) {
		return new Executor<Double>() {
			@Override
			Double execute() {
				return shardedJedis.zscore(key, member);
			}
		}.getResult();
	}
	
	
	/**
	 * 从sorted里取数据
	 * @param key
	 * @param min		最小score
	 * @param max		最大socre
	 * @param count		返回的数量
	 * @return
	 */
	public static Set<String> getSortedSet(final String key,Double min,Double max,int count ) {
		return new Executor<Set<String>>() {
			@Override
			Set<String> execute() {
				return shardedJedis.zrangeByScore(key, min, max, 0, count);
			}
		}.getResult();
	}
	
	/**
	 * 从min开始取
	 * @param key
	 * @param min
	 * @param max
	 * @param count
	 * @return
	 */
	public static LinkedHashSet<Tuple> getSortedSetWithScores(final String key,Double min,Double max,int count ) {
		return new Executor<LinkedHashSet<Tuple>>() {
			@Override
			LinkedHashSet<Tuple> execute() {
				Set result = shardedJedis.zrangeByScoreWithScores(key, min, max, 0, count);
				if(result.size()==0)return new LinkedHashSet<Tuple>();
				return (LinkedHashSet<Tuple>) result;
			}
		}.getResult();
	}
	
	/**
	 * 从max开始取
	 * @param key
	 * @param max
	 * @param min
	 * @param count
	 * @return
	 */
	public static LinkedHashSet<Tuple> getRevertSortedSetWithScores(final String key,Double max,Double min,int count ) {
		return new Executor<LinkedHashSet<Tuple>>() {
			@Override
			LinkedHashSet<Tuple> execute() {
				System.out.println(1);
				Set result = shardedJedis.zrevrangeByScoreWithScores(key, max, min, 0, count);
				if(result.size()==0)return new LinkedHashSet<Tuple>();
				return (LinkedHashSet<Tuple>) result;
			}
		}.getResult();
	}
	
	/**
	 * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。
	 * 有序集成员按 score 值递减(从大到小)的次序排列。
	 * @param key key
	 * @param max score最大值
	 * @param min score最小值
	 * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
	 */
	public static Set<String> revrangeByScoreWithSortedSet(final String key, final double max, final double min) {
		return new Executor<Set<String>>() {
			@Override
			Set<String> execute() {
				return shardedJedis.zrevrangeByScore(key, max, min);
			}
		}.getResult();
	}
	
	/**
	 * 原子删除key
	 * @param key
	 * @return 返回键值,删除失败返回null
	 */
	public static String delAtomic(final String key) {
		return new Executor<String>() {
			@Override
			String execute() {
				Jedis jedis = shardedJedis.getShard(key);
				try {
					jedis.watch(key);
					
					Transaction transaction = jedis.multi();
					transaction.get(key);
					transaction.del(key);
					List<Object> resutList = transaction.exec();
					if(resutList!=null) {
						return (String) resutList.get(0);
					}
					else {//键被更改
						return null;
					}
				} finally {
					jedis.unwatch();
				}
			}
		}.getResult();
	}
	
	/**
	 * 请求分布式锁
	 * @param key
	 * @param expireSeconds
	 * @return
	 */
	public static Boolean tryGetDistributedLock(final String key, int expireSeconds) {
		return new Executor<Boolean>() {
			@Override
			Boolean execute() {
				Jedis jedis = shardedJedis.getShard(key);
				try {
					jedis.watch(key);
					if(jedis.exists(key))return false;

					Transaction transaction = jedis.multi();
					transaction.setnx(key, Constants.MINUS);
					transaction.expire(key, expireSeconds);
					List<Object> resutList = transaction.exec();
					if(resutList!=null) {
						return true;
					}
					else {
						return false;
					}
				} finally {
					jedis.unwatch();
				}
			}
		}.getResult();
	}
	
	/**
	 * 释放分布式锁
	 * @param key
	 * @return
	 */
	public static Boolean tryReleaseDistributedLock(final String key) {
		return new Executor<Boolean>() {
			@Override
			Boolean execute() {
				Jedis jedis = shardedJedis.getShard(key);
				try {
					jedis.watch(key);
					
					Transaction transaction = jedis.multi();
					transaction.del(key);
					List<Object> resutList = transaction.exec();
					if(resutList!=null) {
						return true;
					}
					else {
						return false;
					}
				} finally {
					jedis.unwatch();
				}
			}
		}.getResult();
	}
	
	
	public static class Pair<K, V> {

		private K key;
		private V value;

		public Pair(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
	}
	
	 
    abstract static class Executor<T> {
    	ShardedJedis shardedJedis;
		public Executor() {
			if(shardedJedisPool!=null)
				shardedJedis = shardedJedisPool.getResource();
			else 
				shardedJedis = new EmptyJedis(null);
		}
		abstract T execute();
		public T getResult() {
			T result = null;
			try {
				result = execute();
			} catch (Throwable e) {
				throw new RuntimeException("Redis execute exception", e);
			} finally {
				if (shardedJedis != null) {
					shardedJedis.close();
				}
			}
			return result;
		}
	}
}
