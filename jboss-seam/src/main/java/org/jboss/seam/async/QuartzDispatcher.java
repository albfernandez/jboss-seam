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
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
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

	@Create
	public void initScheduler() throws SchedulerException {
		StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();

		//TODO: magical properties files are *not* the way to config Seam apps!
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("seam.quartz.properties");
		if (is != null) {
			schedulerFactory.initialize(is);
			log.debug("Found seam.quartz.properties file. Using it for Quartz config.");
		} else {
			schedulerFactory.initialize();
			log.warn("No seam.quartz.properties file. Using in-memory job store.");
		}

		scheduler = schedulerFactory.getScheduler();
		scheduler.start();
	}

	public QuartzTriggerHandle scheduleAsynchronousEvent(String type, Object... parameters) {
		String jobName = nextUniqueName();
		String triggerName = nextUniqueName();

		JobDetail jobDetail = new JobDetail(jobName, null, QuartzJob.class);
		jobDetail.getJobDataMap().put("async", new AsynchronousEvent(type, parameters));

		SimpleTrigger trigger = new SimpleTrigger(triggerName);
		try {
			scheduler.scheduleJob(jobDetail, trigger);
			return new QuartzTriggerHandle(triggerName);
		} catch (Exception se) {
			log.debug("Cannot Schedule a Quartz Job");
			throw new RuntimeException(se);
		}
	}

	public QuartzTriggerHandle scheduleTimedEvent(String type, Schedule schedule, Object... parameters) {
		return scheduleWithQuartzServiceAndWrapExceptions(schedule, new AsynchronousEvent(type, parameters));
	}

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
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		} catch (SchedulerException se) {
			throw new RuntimeException(se);
		}
	}

	private QuartzTriggerHandle scheduleWithQuartzService(Schedule schedule, Asynchronous async) throws SchedulerException, ParseException {
		String jobName = nextUniqueName();
		String triggerName = nextUniqueName();

		JobDetail jobDetail = new JobDetail(jobName, null, QuartzJob.class);
		jobDetail.getJobDataMap().put("async", async);

		if (schedule instanceof CronSchedule) {
			CronSchedule cronSchedule = (CronSchedule) schedule;
			CronTrigger trigger = new CronTrigger(triggerName, null);
			trigger.setCronExpression(cronSchedule.getCron());
			trigger.setEndTime(cronSchedule.getFinalExpiration());

			if (cronSchedule.getExpiration() != null) {
				trigger.setStartTime(cronSchedule.getExpiration());
			} else if (cronSchedule.getDuration() != null) {
				trigger.setStartTime(calculateDelayedDate(cronSchedule.getDuration()));
			}

			scheduler.scheduleJob(jobDetail, trigger);
		} else if (schedule instanceof TimerSchedule) {
			TimerSchedule timerSchedule = (TimerSchedule) schedule;
			if (timerSchedule.getIntervalDuration() != null) {
				if (timerSchedule.getExpiration() != null) {
					SimpleTrigger trigger = new SimpleTrigger(triggerName, null, timerSchedule.getExpiration(),
							timerSchedule.getFinalExpiration(), SimpleTrigger.REPEAT_INDEFINITELY, timerSchedule.getIntervalDuration());
					scheduler.scheduleJob(jobDetail, trigger);

				} else if (timerSchedule.getDuration() != null) {
					SimpleTrigger trigger = new SimpleTrigger(triggerName, null, calculateDelayedDate(timerSchedule.getDuration()),
							timerSchedule.getFinalExpiration(), SimpleTrigger.REPEAT_INDEFINITELY, timerSchedule.getIntervalDuration());
					scheduler.scheduleJob(jobDetail, trigger);

				} else {
					SimpleTrigger trigger = new SimpleTrigger(triggerName, null, new Date(), timerSchedule.getFinalExpiration(),
							SimpleTrigger.REPEAT_INDEFINITELY, timerSchedule.getIntervalDuration());
					scheduler.scheduleJob(jobDetail, trigger);

				}
			} else {
				if (schedule.getExpiration() != null) {
					SimpleTrigger trigger = new SimpleTrigger(triggerName, null, schedule.getExpiration());
					scheduler.scheduleJob(jobDetail, trigger);

				} else if (schedule.getDuration() != null) {
					SimpleTrigger trigger = new SimpleTrigger(triggerName, null, calculateDelayedDate(schedule.getDuration()));
					scheduler.scheduleJob(jobDetail, trigger);

				} else {
					SimpleTrigger trigger = new SimpleTrigger(triggerName);
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
		private Asynchronous async;

		public QuartzJob() {
		}

		public void execute(JobExecutionContext context) throws JobExecutionException {
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			async = (Asynchronous) dataMap.get("async");
			QuartzTriggerHandle handle = new QuartzTriggerHandle(context.getTrigger().getName());
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
