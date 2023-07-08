package com.wzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.entity.Orders;

/**
 * @author wzy
 * @creat 2023-06-30-20:57
 */
public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
