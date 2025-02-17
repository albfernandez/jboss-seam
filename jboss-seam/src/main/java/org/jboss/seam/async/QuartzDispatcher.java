package org.jboss.seam.async;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.InputStream;
import java.rmi.server.UID;
import java.text.ParseException;
import java.util.Date;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Dispatcher implementation that uses the Quartz library.
 * 
 * @author Michael Yuan
 *
 */
@Startup
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.async.dispatcher")
@Install(value = false, precedence = BUILT_IN)
@BypassInterceptors
public class QuartzDispatcher extends AbstractDispatcher<QuartzTriggerHandle, Schedule> {

	private static final LogProvider log = Logging.getLogProvider(QuartzDispatcher.class);

	private Scheduler scheduler;

	public QuartzDispatcher() {
		super();
	}
	
	@Create
	public void initScheduler() throws SchedulerException {
		StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();

		//TODO: magical properties files are *not* the way to config Seam apps!
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("seam.quartz.properties");
			if (is != null) {
				schedulerFactory.initialize(is);
				log.debug("Found seam.quartz.properties file. Using it for Quartz config.");
			} else {
				schedulerFactory.initialize();
				log.warn("No seam.quartz.properties file. Using in-memory job store.");
			}
		}
		finally {
			Resources.close(is);
		}

		scheduler = schedulerFactory.getScheduler();
		scheduler.start();
	}

	@Override
	public QuartzTriggerHandle scheduleAsynchronousEvent(String type, Object... parameters) {
		String jobName = nextUniqueName();
		String triggerName = nextUniqueName();

		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(jobName).build();
		jobDetail.getJobDataMap().put("async", new AsynchronousEvent(type, parameters));
		
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName).build();

		try {
			scheduler.scheduleJob(jobDetail, trigger);
			return new QuartzTriggerHandle(triggerName);
		} catch (Exception se) {
			log.debug("Cannot Schedule a Quartz Job");
			throw new RuntimeException(se);
		}
	}

	@Override
	public QuartzTriggerHandle scheduleTimedEvent(String type, Schedule schedule, Object... parameters) {
		return scheduleWithQuartzServiceAndWrapExceptions(schedule, new AsynchronousEvent(type, parameters));
	}

	@Override
	public QuartzTriggerHandle scheduleInvocation(InvocationContext invocation, Component component) {
		return scheduleWithQuartzServiceAndWrapExceptions(createSchedule(invocation), new AsynchronousInvocation(invocation, component));
	}

	private static Date calculateDelayedDate(long delay) {
		Date now = new Date();
		now.setTime(now.getTime() + delay);
		return now;
	}

	private QuartzTriggerHandle scheduleWithQuartzServiceAndWrapExceptions(Schedule schedule, Asynchronous async) {
		try {
			return scheduleWithQuartzService(schedule, async);
		} catch (ParseException | SchedulerException pe) {
			throw new RuntimeException(pe);
		} 
	}

	private QuartzTriggerHandle scheduleWithQuartzService(Schedule schedule, Asynchronous async) throws SchedulerException, ParseException {
		String jobName = nextUniqueName();
		String triggerName = nextUniqueName();

		JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(jobName).build();
		jobDetail.getJobDataMap().put("async", async);

		if (schedule instanceof CronSchedule) {
			CronSchedule cronSchedule = (CronSchedule) schedule;
			
			Date startTime = new Date();
			if (cronSchedule.getExpiration() != null) {
				startTime = cronSchedule.getExpiration();
			} else if (cronSchedule.getDuration() != null) {
				startTime = calculateDelayedDate(cronSchedule.getDuration());
			}
			
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule.getCron()))
					.endAt(cronSchedule.getFinalExpiration())
					.startAt(startTime)
					.build();
			
			

			scheduler.scheduleJob(jobDetail, trigger);
		} else if (schedule instanceof TimerSchedule) {
			TimerSchedule timerSchedule = (TimerSchedule) schedule;
			if (timerSchedule.getIntervalDuration() != null) {
				
				//SimpleTrigger(java.lang.String name, java.lang.String group, java.util.Date startTime, java.util.Date endTime, int repeatCount, long repeatInterval)
				Date startTime = new Date();
				Date endTime = new Date();
				if (timerSchedule.getExpiration() != null) {
					startTime = timerSchedule.getExpiration();
					endTime = timerSchedule.getFinalExpiration();
				} else if (timerSchedule.getDuration() != null) {
					startTime = calculateDelayedDate(timerSchedule.getDuration());
					endTime = timerSchedule.getFinalExpiration();
				} else {
					startTime = new Date();
					endTime = timerSchedule.getFinalExpiration();
				}
				Trigger trigger = TriggerBuilder.newTrigger()
						.withSchedule(SimpleScheduleBuilder.simpleSchedule().repeatForever().withIntervalInMilliseconds(timerSchedule.getIntervalDuration()))
						.startAt(startTime)
						.endAt(endTime)
						.build();
				scheduler.scheduleJob(jobDetail, trigger);
			} else {
				if (schedule.getExpiration() != null) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName).startAt(schedule.getExpiration()).build();
					scheduler.scheduleJob(jobDetail, trigger);

				} else if (schedule.getDuration() != null) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName).startAt(calculateDelayedDate(schedule.getDuration())).build();
					scheduler.scheduleJob(jobDetail, trigger);

				} else {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName).build();
					scheduler.scheduleJob(jobDetail, trigger);
				}
			}
		} else {
			throw new IllegalArgumentException("unrecognized schedule type");
		}

		return new QuartzTriggerHandle(triggerName);
	}

	private String nextUniqueName() {
		return (new UID()).toString();
	}

	@Destroy
	public void destroy() throws SchedulerException {
		scheduler.shutdown();
	}

	public static class QuartzJob implements Job {

		public QuartzJob() {
			super();
		}

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			Asynchronous async = (Asynchronous) dataMap.get("async");
			QuartzTriggerHandle handle = new QuartzTriggerHandle(context.getTrigger().getKey());
			try {
				async.execute(handle);
			} catch (Exception e) {
				async.handleException(e, handle);
			}
		}
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public static QuartzDispatcher instance() {
		return (QuartzDispatcher) AbstractDispatcher.instance();
	}

}
