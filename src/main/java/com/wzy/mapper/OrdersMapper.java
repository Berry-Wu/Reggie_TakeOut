package com.wzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wzy
 * @creat 2023-06-30-20:56
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
