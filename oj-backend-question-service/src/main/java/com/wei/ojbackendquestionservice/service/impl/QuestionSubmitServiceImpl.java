package com.wei.ojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.ojbackendcommon.common.ErrorCode;
import com.wei.ojbackendcommon.constant.CommonConstant;
import com.wei.ojbackendcommon.exception.BusinessException;
import com.wei.ojbackendcommon.utils.SqlUtils;
import com.wei.ojbackendmodel.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.wei.ojbackendmodel.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.wei.ojbackendmodel.model.entity.Question;
import com.wei.ojbackendmodel.model.entity.QuestionSubmit;
import com.wei.ojbackendmodel.model.entity.User;
import com.wei.ojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.wei.ojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.wei.ojbackendmodel.model.vo.QuestionSubmitVO;
import com.wei.ojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.wei.ojbackendquestionservice.service.QuestionService;
import com.wei.ojbackendquestionservice.service.QuestionSubmitService;
import com.wei.ojbackendserviceclient.service.JudgeFeignClient;
import com.wei.ojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author whw12
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-11-04 21:39:08
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy//存在循环调用，所以懒加载（延迟加载）
    private JudgeFeignClient judgeFeignClient;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {

        // todo 校验编程语言
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageValue = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }

        // 判断实体是否存在（判断题目是否存在），根据类别获取实体
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 是否已提交题目
        long userId = loginUser.getId();
        //每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // todo 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // todo 异步执行判题服务
        CompletableFuture.runAsync(() -> {
            judgeFeignClient.doJudge(questionSubmitId);
        });
        return questionSubmitId;
    }

    /**
     * 获取查询包装类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件，第一个参数判断字段是否为空，达到模糊查询的效果
        queryWrapper.eq(StringUtils.isNotEmpty(language), "language", language);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);

//        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        //提交记录用户不是当前登录用户，或不是管理员，进行脱敏
        if (!userId.equals(questionSubmit.getUserId()) && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
/*        // 1. 关联查询用户信息
        Long userId = questionSubmit.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        //将信息脱敏（有选择性的展示字段）
        UserVO userVO = userService.getUserVO(user);
        questionSubmitVO.setUserVO(userVO);*/
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            return getQuestionSubmitVO(questionSubmit, loginUser);
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }


}




