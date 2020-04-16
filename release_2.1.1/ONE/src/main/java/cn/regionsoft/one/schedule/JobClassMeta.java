package cn.regionsoft.one.schedule;

import java.lang.reflect.Method;

import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import cn.regionsoft.one.core.ids.IDGenerator;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class JobClassMeta {
	private Long id;
	private Method method;
	private Object managedBean;
	private JobKey jobKey;
	private TriggerKey triggerKey;
	private String jobName;
	private String groupName;
	
	private Class<org.quartz.Job> jobClass;

	public TriggerKey getTriggerKey() {
		return triggerKey;
	}

	void setTriggerKey(TriggerKey triggerKey) {
		this.triggerKey = triggerKey;
	}

	public JobKey getJobKey() {
		return jobKey;
	}

	void setJobKey(JobKey jobKey) {
		this.jobKey = jobKey;
	}

	public String getJobName() {
		return jobName;
	}

	void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getGroupName() {
		return groupName;
	}

	void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	void setJobClass(Class<org.quartz.Job> jobClass) {
		this.jobClass = jobClass;
	}

	/**
	 * 动态创建不同的job class
	 * @return
	 * @throws Exception
	 */
	public static JobClassMeta newJobClassMeta() throws Exception {
		Long jobClassId = IDGenerator.generateSnowflakeID();
		
		ClassPool pool = ClassPool.getDefault();  
        CtClass cc = pool.makeClass("cn.regionsoft.one.schedule.jobs.RegionJob"+jobClassId); 
        cc.addInterface(pool.get("org.quartz.Job"));
      
        StringBuilder methodBody = new StringBuilder("public void execute(org.quartz.JobExecutionContext context) throws org.quartz.JobExecutionException{") ;
        methodBody.append("cn.regionsoft.one.schedule.ScheduleManager.getInstance().excuteJob(\""+jobClassId+"\");");
        methodBody.append("}");
        
        CtMethod m1=CtMethod.make(methodBody.toString(), cc);
        cc.addMethod(m1);
        Class<?> realClass = (Class<?>) cc.toClass();
        cc.detach();
        
        JobClassMeta jobClassMeta = new JobClassMeta();
        jobClassMeta.id = jobClassId;
        jobClassMeta.jobClass = (Class<Job>) realClass;
        return jobClassMeta;
	}

	public Long getId() {
		return id;
	}

	public Class<org.quartz.Job> getJobClass() {
		return jobClass;
	}

	public Method getMethod() {
		return method;
	}

	void setMethod(Method method) {
		this.method = method;
	}

	public Object getManagedBean() {
		return managedBean;
	}

	void setManagedBean(Object managedBean) {
		this.managedBean = managedBean;
	}
}
