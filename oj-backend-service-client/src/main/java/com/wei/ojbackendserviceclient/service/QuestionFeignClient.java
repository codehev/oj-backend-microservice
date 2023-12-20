package com.wei.ojbackendserviceclient.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.ojbackendmodel.model.entity.Question;
import com.wei.ojbackendmodel.model.dto.question.QuestionQueryRequest;
import com.wei.ojbackendmodel.model.entity.QuestionSubmit;
import com.wei.ojbackendmodel.model.vo.QuestionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

/**
 * @author wei
 * @description
 * @createDate 2023-11-04 21:38:03
 */
@FeignClient(name = "oj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    @GetMapping("/get/{id}")
    Question getQuestionById(@PathVariable("id") Long questionId);

    @GetMapping("/question_submit/get/{id}")
    QuestionSubmit getQuestionSubmitById(@PathVariable("id") Long questionSubmitId);

    @PostMapping("/question_submit/update")
    Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);


}
