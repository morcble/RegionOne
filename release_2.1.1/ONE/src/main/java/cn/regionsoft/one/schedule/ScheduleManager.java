package cn.regionsoft.one.schedule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import cn.regionsoft.one.annotation.Job;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.Assert;
import cn.regionsoft.one.core.CommonUtil;


public class ScheduleManager {
	private static final Logger logger = Logger.getLogger(ScheduleManager.class);
	
	private static ScheduleManager scheduleManager = null;
	
	private Scheduler scheduler = null;
	private ScheduleManager() {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public static ScheduleManager getInstance() {
		if(scheduleManager==null) {
			synchronized(ScheduleManager.class) {
				if(scheduleManager==null) {
					scheduleManager = new ScheduleManager();
				}
			}
		}
		return scheduleManager;
	}
	
	/**
	 * groupName - <jobName - meta>
	 */
	private ConcurrentHashMap<String,HashMap<String,JobClassMeta>> jobRegistry = new ConcurrentHashMap<String,HashMap<String,JobClassMeta>>();
	private ConcurrentHashMap<String,JobClassMeta> jobMetaMap = new ConcurrentHashMap<String,JobClassMeta>();
	
	public synchronized void initNewJob(String jobName,String groupName,String corn, Method method, Object managedBean) throws Exception {
		Assert.notEmpty(corn, "corn is emtpy");
		
		JobClassMeta jobClassMeta = JobClassMeta.newJobClassMeta();
		jobClassMeta.setManagedBean(managedBean);
		jobClassMeta.setMethod(method);
		//把job加入注册表,并判断是否有重复的job
		HashMap<String,JobClassMeta> groupJobMetas = jobRegistry.get(groupName);
		if(groupJobMetas==null) {
			groupJobMetas = new HashMap<String,JobClassMeta>();
			jobRegistry.put(groupName, groupJobMetas);
			
			groupJobMetas.put(jobName, jobClassMeta);
		}
		else {
			JobClassMeta exsitJobClassMeta = groupJobMetas.get(jobName);
			if(exsitJobClassMeta!=null) {
				throw new Exception("duplicate scheduled jobs are found: jobGroup="+groupName+",jobName="+jobName+". "+managedBean.getClass().getName()+"."+method.getName());
			}
			
			groupJobMetas.put(jobName, jobClassMeta);
		}

		JobDetail jobDetail = JobBuilder.newJob(jobClassMeta.getJobClass())
                .withIdentity(jobName, groupName)
                .build();
		
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("trigger_"+jobName, "trigger_"+groupName)
				.startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule(corn))
				.build();
		jobClassMeta.setJobKey(jobDetail.getKey());
		jobClassMeta.setTriggerKey(trigger.getKey());
		jobClassMeta.setGroupName(groupName);
		jobClassMeta.setJobName(jobName);
	
        scheduler.scheduleJob(jobDetail, trigger);
        
        jobMetaMap.put(jobClassMeta.getId().toString(), jobClassMeta);
	}
	
	public synchronized void initNewJob(Job job, Method method, Object managedBean) throws Exception {
		String corn = job.corn();
		String jobName = job.name();
		String groupName = job.group();
		if(CommonUtil.isEmpty(groupName)) {
			groupName = managedBean.getClass().getName();
		}
		
		if(CommonUtil.isEmpty(jobName)) {
			jobName = method.getName();
		}
		
		this.initNewJob(jobName, groupName, corn, method, managedBean);
	}
	
	public void excuteJob(String jobId) {
		JobClassMeta jobClassMeta = jobMetaMap.get(jobId);
		try {
			jobClassMeta.getMethod().invoke(jobClassMeta.getManagedBean());
		}
		catch(Exception e) {
			logger.error(e);
		}
	}
}
