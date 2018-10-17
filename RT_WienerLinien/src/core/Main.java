package core;


import org.apache.log4j.BasicConfigurator;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class Main {

	public static void main(String[] args) throws SchedulerException {
		
		BasicConfigurator.configure();
		
		Scheduler sc = StdSchedulerFactory.getDefaultScheduler();
		
		JobDetail job = JobBuilder.newJob(RT_Process.class).build();
		
//		Trigger t1 = TriggerBuilder.newTrigger().withIdentity("CronTrigger").withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * ? * *")).build();
		
		Trigger t1 = TriggerBuilder.newTrigger().withIdentity("CronTrigger").startNow().build();
		
		sc.start();
		sc.scheduleJob(job,t1);
		
		
		
	}

}
