package com.example.demo.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduleConfig {

	private final JobLauncher jobLauncher;
	private final Job resetTaskletJob;

	public ScheduleConfig(JobLauncher jobLauncher, 
	                      @Qualifier("resetTaskletJob") Job resetTaskletJob) {
		this.jobLauncher = jobLauncher;
		this.resetTaskletJob = resetTaskletJob;
	}

//	@Scheduled(cron = "*/5 * * * * *")  // 毎5秒ごとに実行
//	public void scheduleReset() throws Exception {
//		JobParameters jobParameters = new JobParametersBuilder()
//				.addLong("time", System.currentTimeMillis())
//				.toJobParameters();
//		jobLauncher.run(resetTaskletJob, jobParameters);
//	}
}
