package cn.regionsoft.one.tool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/**
 * all the asyn task has to be taken by this thread pool
 * 
 * @author fenglj
 *
 */
public class ThreadPool {
	public static ThreadPool threadPool = new ThreadPool();
	
	//private ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 200, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(100000));
	
	private ExecutorService slowTaskExecutor = null;
	
	private ExecutorService quickTaskExecutor = null;
	

	public ExecutorService getSlowTaskExecutor() {
		return slowTaskExecutor;
	}

	public ExecutorService getQuickTaskExecutor() {
		return quickTaskExecutor;
	}

	public static ThreadPool getInstance() {
		return threadPool;
	}
	
	private int slowTaskThreadsCount = 0 ;
	
	private int quickTaskThreadsCount = 0 ;
	
	private ThreadPool() {
		slowTaskThreadsCount = 50;
		quickTaskThreadsCount = Runtime.getRuntime().availableProcessors();
		
		slowTaskExecutor = Executors.newFixedThreadPool(slowTaskThreadsCount);
		quickTaskExecutor = Executors.newFixedThreadPool(quickTaskThreadsCount);
	}
	
	public int getSlowTaskThreadsCount() {
		return slowTaskThreadsCount;
	}

	public int getQuickTaskThreadsCount() {
		return quickTaskThreadsCount;
	}

	public void excuteQuickTask(Runnable runnable) {
		quickTaskExecutor.execute(runnable);
	}
	
	public Future<?> submitQuickTask(Runnable runnable) {
		return quickTaskExecutor.submit(runnable);
	}
	
	public <T> Future<T> submitQuickTask(Callable<T> task){
		return quickTaskExecutor.submit(task);
	}
	
	public void excuteSlowTask(Runnable runnable) {
		slowTaskExecutor.execute(runnable);
	}
	
	public Future<?> submitSlowTask(Runnable runnable) {
		return slowTaskExecutor.submit(runnable);
	}
	
	public <T> Future<T> submitSlowTask(Callable<T> task){
		return slowTaskExecutor.submit(task);
	}
	
}
