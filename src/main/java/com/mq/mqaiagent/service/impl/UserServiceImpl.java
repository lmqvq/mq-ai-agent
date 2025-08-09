package com.mq.mqaiagent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.constant.CommonConstant;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.manager.CosManager;
import com.mq.mqaiagent.mapper.UserMapper;
import com.mq.mqaiagent.model.dto.UserQueryRequest;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.model.enums.UserRoleEnum;
import com.mq.mqaiagent.model.vo.LoginUserVO;
import com.mq.mqaiagent.model.vo.UserVO;
import com.mq.mqaiagent.service.UserService;
import com.mq.mqaiagent.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.mq.mqaiagent.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author MQ
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-28 21:26:31
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Value("${cos.client.bucket-url:}")
    private String bucketUrl;

    @Resource
    private CosManager cosManager;
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 2. 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 3. 加密
            String encryptPassword = getEncryptPassword(userPassword);
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            int[] numbers = NumberUtil.generateRandomNumber(1, 1000, 1);
            user.setUserName(StrUtil.format("user_{}", numbers[0]));
            //user.setUserName("无名");
            //user.setUserName(StrUtil.format("无名{}", NumberUtil.generateRandomNumber(1,1000,1)));
            user.setUserProfile("这位朋友幽默风趣");
            user.setUserAvatar("https://s2.loli.net/2025/03/30/lomyT1DdqspgBxN.jpg");
            user.setUserRole(UserRoleEnum.USER.getValue());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "lmqicu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 上传用户头像
     *
     * @param file    文件
     * @param request 请求
     * @return 头像URL
     */
    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验文件
        long fileSize = file.getSize();
        String originalFilename = file.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("png", "jpg", "jpeg", "gif", "svg");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
        // 生成文件名
        String uuid = IdUtil.simpleUUID();
        String filename = uuid + "-" + originalFilename;
        // 生成存储路径：doj_avatars/userid/uuid-filename
        String filepath = String.format("ai_agent_avatars/%s/%s", loginUser.getId(), filename);
        File tempFile = null;
        try {
            // 上传到腾讯云
            tempFile = File.createTempFile(uuid, suffix);
            file.transferTo(tempFile);
            cosManager.putObject(filepath, tempFile);
            // 返回可访问地址
            String avatarUrl = bucketUrl + "/" + filepath;
            // 更新用户头像
            boolean result = updateUserAvatar(avatarUrl, request);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新头像失败");
            return avatarUrl;
        } catch (Exception e) {
            log.error("文件上传失败，" + e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (tempFile != null) {
                // 清理临时文件
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("临时文件删除失败，文件路径：{}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 更新用户头像
     *
     * @param avatarUrl 头像URL
     * @param request   请求信息
     * @return 是否成功
     */
    @Override
    public boolean updateUserAvatar(String avatarUrl, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(avatarUrl);
        boolean result = this.updateById(user);
        // 更新登录用户缓存
        if (result) {
            loginUser.setUserAvatar(avatarUrl);
            request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);
        }
        return result;
    }

}




