package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.entity.User;
import com.resource.platform.mapper.UserMapper;
import com.resource.platform.service.UserService;
import com.resource.platform.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 
 * 功能说明：
 * - 实现用户认证的核心业务逻辑
 * - 处理用户密码的加密验证
 * - 管理用户状态的检查和验证
 * - 提供用户信息的查询服务
 * 
 * @author 系统
 * @since 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 验证用户凭证
     * 
     * 业务逻辑：
     * 1. 根据用户名查询用户信息
     * 2. 验证用户是否存在
     * 3. 检查用户账号状态是否正常
     * 4. 验证密码是否正确
     * 5. 返回用户信息（不包含密码）
     * 
     * @param username 用户名
     * @param password 密码（明文）
     * @return 用户信息对象，不包含敏感信息
     * @throws RuntimeException 当用户不存在、被禁用或密码错误时抛出
     */
    @Override
    public UserVO authenticate(String username, String password) {
        // 记录认证开始，注意：不记录密码
        log.info("开始验证用户凭证: username={}", username);
        
        // 步骤1：构建查询条件
        // 使用Lambda表达式构建类型安全的查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        
        // 步骤2：从数据库查询用户信息
        log.debug("查询用户信息: username={}", username);
        User user = userMapper.selectOne(wrapper);
        
        // 步骤3：验证用户是否存在
        // 如果用户不存在，抛出异常终止认证流程
        if (user == null) {
            log.warn("用户不存在: username={}", username);
            throw new RuntimeException("用户不存在");
        }
        
        // 记录查询到的用户ID
        log.debug("查询到用户: username={}, userId={}", username, user.getId());
        
        // 步骤4：检查用户账号状态
        // status=0表示账号已被禁用，不允许登录
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: username={}, userId={}", username, user.getId());
            throw new RuntimeException("用户已被禁用");
        }
        
        // 步骤5：验证密码是否正确
        // 使用PasswordEncoder的matches方法比对明文密码和加密后的密码
        log.debug("验证用户密码: username={}", username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码错误: username={}", username);
            throw new RuntimeException("密码错误");
        }
        
        // 步骤6：构建返回对象
        // 将User实体转换为UserVO，避免返回敏感信息（如密码）
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 记录认证成功
        log.info("用户凭证验证成功: username={}, userId={}", username, user.getId());
        
        return userVO;
    }

    /**
     * 根据用户名查询用户
     * 
     * 业务逻辑：
     * 1. 根据用户名构建查询条件
     * 2. 从数据库查询用户信息
     * 3. 将实体对象转换为VO对象
     * 4. 返回用户信息（不包含密码）
     * 
     * @param username 用户名
     * @return 用户信息对象，如果用户不存在则返回null
     */
    @Override
    public UserVO getUserByUsername(String username) {
        // 记录查询开始
        log.info("根据用户名查询用户: username={}", username);
        
        // 步骤1：构建查询条件
        // 使用Lambda表达式构建类型安全的查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        
        // 步骤2：从数据库查询用户信息
        log.debug("执行数据库查询: username={}", username);
        User user = userMapper.selectOne(wrapper);
        
        // 步骤3：处理查询结果
        // 如果用户不存在，返回null
        if (user == null) {
            log.warn("用户不存在: username={}", username);
            return null;
        }
        
        // 步骤4：转换为VO对象
        // 将User实体转换为UserVO，避免返回敏感信息（如密码）
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 记录查询成功
        log.info("查询用户成功: username={}, userId={}", username, user.getId());
        
        return userVO;
    }
}
