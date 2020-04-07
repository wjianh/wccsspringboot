package com.wjh.controller;

import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.domain.Response;
import com.wjh.entity.XtGlyxx;
import com.wjh.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class test {
    @Autowired
    TestService testService;

    @Autowired
    private JobClient jobClient;

    @RequestMapping("/test01")
    public Map<String, Object> test01() {
        //模拟提交一个任务
        Job job = new Job();
        job.setTaskId("hello"); //jobId
        job.setCronExpression("0/3 * * * * ?"); //任务类型
        //设置任务类型 区分不同的任务 执行不同的业务逻辑
        job.setParam("type", "aType"); //设置参数
        job.setNeedFeedback(true);
        //任务触发时间 如果设置了 cron 则该设置无效
//        job.setTriggerTime(DateUtils.addDay(new Date(), 1).getTime());
        //任务执行节点组
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        //当任务队列中存在这个任务的时候，是否替换更新
        job.setReplaceOnExist(false);
        Map<String, Object> submitResult = new HashMap<String, Object>(4);
        try {
            //任务提交返回值 response
            Response response = jobClient.submitJob(job);//提交任务
            submitResult.put("success", response.isSuccess());
            submitResult.put("msg", response.getMsg());
            submitResult.put("code", response.getCode());
        } catch (Exception e) {
            System.out.println("提交任务失败");
            throw new RuntimeException("提交任务失败");
        }
        return submitResult;
    }

    @RequestMapping("/test02")
    public Map<String, Object> test02() {
        //模拟提交一个任务
        Job job = new Job();
        job.setTaskId("task-BBBBBBBBBBBBBBB");
        job.setCronExpression("0/6 * * * * ?");
        //设置任务类型 区分不同的任务 执行不同的业务逻辑
        job.setParam("type", "bType");
        job.setNeedFeedback(true);
        //任务触发时间 如果设置了 cron 则该设置无效
//        job.setTriggerTime(DateUtils.addDay(new Date(), 1).getTime());
        //任务执行节点组
        job.setTaskTrackerNodeGroup("test_trade_TaskTracker");
        //当任务队列中存在这个任务的时候，是否替换更新
        job.setReplaceOnExist(false);
        Map<String, Object> submitResult = new HashMap<String, Object>(4);
        try {
            Response response = jobClient.submitJob(job);
            submitResult.put("success", response.isSuccess());
            submitResult.put("msg", response.getMsg());
            submitResult.put("code", response.getCode());
        } catch (Exception e) {
            System.out.println("提交任务失败");
            throw new RuntimeException("提交任务失败");
        }
        return submitResult;
    }

    @RequestMapping("/login.do")
    @ResponseBody
    public void testJsp(){
        System.out.println("======================");
        Map<String,Object> map = new HashMap<>();
        List<XtGlyxx> xtGlyxxList = testService.findAdmin();
        for (XtGlyxx a:xtGlyxxList) {
            System.out.println(a);
        }
        map.put("hello","hello");
    }
}
