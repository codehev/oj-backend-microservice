package com.wei.ojbackendjudgeservice.judge.codesandbox.impl;



import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.wei.ojbackendmodel.model.dto.questionSubmit.JudgeInfo;
import com.wei.ojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.wei.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("示例代码沙箱");
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setJudgeInfo(judgeInfo);
        
        return executeCodeResponse;
    }
}
