package com.wzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wzy
 * @creat 2023-06-25-22:05
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
