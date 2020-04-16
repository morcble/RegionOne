package cn.regionsoft.one.caches;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import cn.regionsoft.one.caches.annotation.Cache;


public class CacheThreadData {
	private int stackDepth = 0;
	
	private Stack<LinkedHashMap<String,Cache>> cacheStack = new Stack<LinkedHashMap<String,Cache>>();

	private LinkedHashMap<String,Cache> currentMap = null;
	
	public void put(String key,Cache value) {
		currentMap.put(key, value);
	}
	
	public Iterator<Entry<String,Cache>> iterator() {
		return currentMap.entrySet().iterator();
	}
	
	public void pushStack() {
		LinkedHashMap<String,Cache> map = new LinkedHashMap<String,Cache>();
		currentMap = map;
		cacheStack.add(map);
		stackDepth++;
	}
	
	public void popStack() {
		cacheStack.pop();
		stackDepth--;
		if(stackDepth==0)return;
		currentMap = cacheStack.peek();
	}
}
