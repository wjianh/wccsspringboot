package com.wjh.ltstasktracker;

import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.spring.tasktracker.JobRunnerItem;
import com.github.ltsopensource.spring.tasktracker.LTS;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.logger.BizLogger;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Robert HG (254963746@qq.com) on 10/20/15.
 */
@LTS
public class JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    @Autowired
    SpringBean springBean;

    @JobRunnerItem(shardValue = "hellowjh")
    public Result runJob1(JobContext jobContext) throws Throwable {
        try {

            springBean.hello();

            LOGGER.info("runJob1 我要执行：" + jobContext.getJob());
            BizLogger bizLogger = jobContext.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }

    @JobRunnerItem(shardValue = "hello01")
    public Result runhello(JobContext jobContext) throws Throwable {
        try {
            springBean.hello();
            System.out.println("我终于成功了！，哈哈哈哈哈哈哈哈哈");
            LOGGER.info("runJob1 我要执行：" + jobContext.getJob());
            BizLogger bizLogger = jobContext.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }

    @JobRunnerItem(shardValue = "222")
    public void runJob2(JobContext jobContext) throws Throwable {
        try {
            springBean.hello();

            LOGGER.info("runJob2 我要执行");
            BizLogger bizLogger = jobContext.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");
        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
        }
    }

}
