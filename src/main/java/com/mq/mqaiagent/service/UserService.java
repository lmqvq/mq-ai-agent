package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.dto.UserQueryRequest;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.model.vo.LoginUserVO;
import com.mq.mqaiagent.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author MQ
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-28 21:26:32
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户退出登录
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户上传头像
     *
     * @param file    头像文件
     * @param request 请求信息
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file, HttpServletRequest request);

    /**
     * 更新用户头像
     *
     * @param avatarUrl 头像URL
     * @param request   请求信息
     * @return 是否成功
     */
    boolean updateUserAvatar(String avatarUrl, HttpServletRequest request);
}
