package com.wei.ojbackendquestionservice.controller.inner;

import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendmodel.model.entity.Question;
import com.wei.ojbackendmodel.model.entity.QuestionSubmit;
import com.wei.ojbackendquestionservice.service.QuestionService;
import com.wei.ojbackendquestionservice.service.QuestionSubmitService;
import com.wei.ojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 该服务仅供内部调用
 *
 * @author wei
 * @email 2529799312@qq.com
 * @description TODO
 * @date 2023-12-19 21:12
 */
@RestController("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    QuestionSubmitService questionSubmitService;

    @Override
    @GetMapping("/get/{id}")
    public Question getQuestionById(@PathVariable("questionId") Long questionId) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(questionId);
        if (question != null) {
            return question;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

    @Override
    @GetMapping("/question_submit/get/{id}")
    public QuestionSubmit getQuestionSubmitById(@PathVariable("questionId") Long questionSubmitId) {
        if (questionSubmitId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit != null) {
            return questionSubmit;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

    @Override
    @PostMapping("/question_submit/update")
    public Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean updateById = questionSubmitService.updateById(questionSubmit);
        if (updateById) {
            return true;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
}
