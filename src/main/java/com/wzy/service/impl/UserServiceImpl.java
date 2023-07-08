package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.User;
import com.wzy.mapper.UserMapper;
import com.wzy.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-25-22:06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
}
