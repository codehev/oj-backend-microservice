package com.wei.ojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBoxFactory;
import com.wei.ojbackendjudgeservice.judge.codesandbox.CodeSandBoxProxy;
import com.wei.ojbackendjudgeservice.judge.strategy.JudgeContext;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.wei.ojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.wei.ojbackendmodel.model.dto.question.JudgeCase;
import com.wei.ojbackendmodel.model.dto.questionSubmit.JudgeInfo;
import com.wei.ojbackendmodel.model.entity.Question;
import com.wei.ojbackendmodel.model.entity.QuestionSubmit;
import com.wei.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.wei.ojbackendmodel.model.vo.QuestionSubmitVO;
import com.wei.ojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    /**
     * 判题管理（简化调用）
     */
    @Resource
    private JudgeManager judgeManager;

    @Value("${codeSandBox.type:example}")
    private String codeSandBoxType;
    @Value("${codeSandBox.auth.request.header:auth}")
    private String authRequestHeader;
    @Value("${codeSandBox.auth.request.secret:secretkey}")
    private String authRequestSecretKey;
    @Value("${codeSandBox.url:http://127.0.0.1:8105/api/executeCode}")
    private String codeSandboxUrl;

    @Override
    public QuestionSubmitVO doJudge(long questionSubmitId) {

        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }

        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在不存在");
        }

        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已在判题中");
        }

        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean updateById = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!updateById) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 4）调用沙箱，获取到执行结果
        //返回具体的代码沙箱实例
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(codeSandBoxType, authRequestHeader, authRequestSecretKey, codeSandboxUrl);
        //把沙箱实例作为参数，生成一个代理类，也是CodeSandBox类型，直接覆盖codeSandBox
        codeSandBox = new CodeSandBoxProxy(codeSandBox);

        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        //JudgeCase是json字符串，得转换
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        //包含输入用例和输出用例，现在只要输入用例
        //将一个列表judgeCaseList中的对象进行流式处理，并将每个对象的getInput()方法返回的结果收集到一个新的列表中，并将该列表赋值给inputList变量。
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());

        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);

        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
//        //自定义的策略类
//        DefaultJudgeStrategy defaultJudgeStrategy = new DefaultJudgeStrategy();
//        JudgeInfo judgeInfo = defaultJudgeStrategy.doJudge(judgeContext);
        
       /* //1 先判断沙箱执行的结果输出数量是否和预期输出数量相等
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.WAITING;
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            return null;
        }
        //2 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                return null;
            }
        }
        //3 判题题目的限制是否符合要求
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needTimeLimit = judgeConfig.getTimeLimit();
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        if (memory > needMemoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            return null;
        }
        if (time > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            return null;
        }
        //4 可能还有其他的异常情况

        */

        //修改数据库中判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        updateById = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!updateById) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmitResult);
        return questionSubmitVO;
    }
}
