package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.OrderDetail;
import com.wzy.mapper.OrderDetailMapper;
import com.wzy.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-30-20:58
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
