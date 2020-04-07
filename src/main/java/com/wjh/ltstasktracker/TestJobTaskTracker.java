package com.wjh.ltstasktracker;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.spring.boot.annotation.JobRunner4TaskTracker;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

@JobRunner4TaskTracker
public class TestJobTaskTracker implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();
        String type = job.getParam("type");
        return run(jobContext);
    }
}
