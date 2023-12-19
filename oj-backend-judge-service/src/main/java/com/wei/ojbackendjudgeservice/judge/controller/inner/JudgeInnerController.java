package com.wei.ojbackendjudgeservice.judge.controller.inner;

import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendjudgeservice.judge.JudgeService;
import com.wei.ojbackendmodel.model.vo.QuestionSubmitVO;
import com.wei.ojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 该服务仅供内部调用
 *
 * @author wei
 * @email 2529799312@qq.com
 * @description TODO
 * @date 2023-12-19 21:12
 */
@RestController()
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;


    @Override
    @PostMapping("/do/")
    public QuestionSubmitVO doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        QuestionSubmitVO questionSubmitVO = judgeService.doJudge(questionSubmitId);
        if (questionSubmitVO != null) {
            return questionSubmitVO;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
}
