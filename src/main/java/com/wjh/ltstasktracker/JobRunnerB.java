package com.wjh.ltstasktracker;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

public class JobRunnerB implements JobRunner {
    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Job job = jobContext.getJob();
        //  TODO A类型Job的逻辑
        System.out.println("我是Runner A");
        return null;
    }
}
