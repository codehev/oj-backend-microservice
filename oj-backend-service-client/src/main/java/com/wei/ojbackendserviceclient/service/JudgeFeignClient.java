package com.wei.ojbackendserviceclient.service;


import com.wei.ojbackendmodel.model.vo.QuestionSubmitVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 定义单独的 judgeService 类，
 * 而不是把所有判题相关的代码写到 questionSubmitService 里，有利于后续的模块抽离、微服务改造。
 * @author wei
 */
@FeignClient(name = "oj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/do/")
    QuestionSubmitVO doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
