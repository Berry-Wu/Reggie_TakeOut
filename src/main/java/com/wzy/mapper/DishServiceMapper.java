package com.wzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wzy
 * @creat 2023-06-21-21:11
 */
@Mapper
public interface DishServiceMapper extends BaseMapper<Dish> {
}
