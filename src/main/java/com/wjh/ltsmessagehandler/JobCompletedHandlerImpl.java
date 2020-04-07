package com.wjh.ltsmessagehandler;

import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.JobResult;
import com.github.ltsopensource.core.logger.Logger;
import com.github.ltsopensource.core.logger.LoggerFactory;
import com.github.ltsopensource.jobclient.support.JobCompletedHandler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 任务完成处理类
 */
@Component
public class JobCompletedHandlerImpl implements JobCompletedHandler {

    private static final Logger log = LoggerFactory.getLogger(JobCompletedHandlerImpl.class);

    @Override
    public void onComplete(List<JobResult> jobResults) {
        //对任务执行结果进行处理 打印相应的日志信息
        if (CollectionUtils.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                log.info("任务执行完成taskId={}, 执行完成时间={}, job={}",
                        jobResult.getJob().getTaskId(), time, jobResult.getJob().toString());
            }
        }
    }

}