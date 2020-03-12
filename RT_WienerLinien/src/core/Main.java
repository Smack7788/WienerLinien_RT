package core;


import java.io.IOException;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;

public class Main {

	public static void main(String[] args) throws SchedulerException, IOException {
		PropertyConfigurator.configureAndWatch("../RT_WienerLinien/conf/core.DataConverter.log4j.properties");
		
		System.out.println(System.getProperty("user.dir"));
//		System.setProperty("http.proxyHost", "web-proxy.houston.hpecorp.net");
//		System.setProperty("http.proxyPort", "8080");
		
		Scheduler realTimeScheduler = StdSchedulerFactory.getDefaultScheduler();
		
		JobDetail realTimeJob = JobBuilder.newJob(RT_Process.class).build();
		
		Trigger realTimeTrigger = TriggerBuilder.newTrigger().withIdentity("CronTrigger").withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * ? * *")).build();
		
//		Trigger realTimeTrigger = TriggerBuilder.newTrigger().withIdentity("CronTrigger").startNow().build();
		
		realTimeScheduler.start();
		realTimeScheduler.scheduleJob(realTimeJob,realTimeTrigger);
        
		Scheduler indexSetupScheduler = StdSchedulerFactory.getDefaultScheduler();
		
		JobDetail indexSetupJob = JobBuilder.newJob(IndexSetupProcess.class).build();
		
		Trigger indexSetupTrigger = TriggerBuilder.newTrigger().withIdentity("CronTrigger2").withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 1/1 * ? *")).build();
		
//		Trigger indexSetupTrigger = TriggerBuilder.newTrigger().withIdentity("CronTrigger").startNow().build();
		
		indexSetupScheduler.start();
		indexSetupScheduler.scheduleJob(indexSetupJob,indexSetupTrigger);
		
		
	}

}
