package com.wzy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.common.BaseContext;
import com.wzy.common.R;
import com.wzy.dto.OrdersDto;

import com.wzy.entity.OrderDetail;
import com.wzy.entity.Orders;
import com.wzy.service.OrderDetailService;
import com.wzy.service.OrdersService;
import com.wzy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wzy
 * @creat 2023-06-30-20:59
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;
    /**
     * 结算订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单详情：{}", orders.toString());
        ordersService.submit(orders);
        return R.success("用户下单成功");
    }

    /**
     * 历史订单和最新订单统一接口
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        Long userId = BaseContext.getCurrentId();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);

        //拷贝分页数据，但不拷贝数据
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        //查询当前用户id订单数据
        lqw.eq(userId != null, Orders::getUserId, userId);
        //按时间降序排序
        lqw.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, lqw);

        List<OrdersDto> ordersDtoList = new ArrayList<>();
        for (Orders order : pageInfo.getRecords()) {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(order, ordersDto);

            //获取orderId,然后根据这个id，去orderDetail表中查数据
            Long id = order.getId();
            LambdaQueryWrapper<OrderDetail> orderDeatillqw = new LambdaQueryWrapper<>();
            orderDeatillqw.eq(OrderDetail::getOrderId, id);
            List<OrderDetail> details = orderDetailService.list(orderDeatillqw);

            ordersDto.setOrderDetails(details);
            ordersDtoList.add(ordersDto);
        }

        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    /**
     * 后端订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> backendPage(int page, int pageSize, Long number, String beginTime, String endTime){
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        lqw.like(number!=null,Orders::getId, number);//订单条件查询
        lqw.ge(!StringUtils.isEmpty(beginTime),Orders::getOrderTime, beginTime);//起始时间
        lqw.le(!StringUtils.isEmpty(endTime),Orders::getOrderTime, endTime);//终止时间
        lqw.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, lqw);

        List<OrdersDto> orderDtoList = new ArrayList<>();

        for (Orders order : ordersService.list(lqw)) {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(order, ordersDto);

            Long userId = order.getUserId();
            String userName = userService.getById(userId).getName();
            ordersDto.setUserName(userName);
            orderDtoList.add(ordersDto);
        }

        ordersDtoPage.setRecords(orderDtoList);
        return R.success(ordersDtoPage);
    }

    /**
     * 订单状态的修改
     * @param order
     * @return
     */
    @PutMapping
    public R<String> stateChange(@RequestBody Orders order){
        log.info("前端传递的参数：{}", order.toString());
        Long id = order.getId();
        Orders realOrder = ordersService.getById(id);
        realOrder.setStatus(order.getStatus());
        ordersService.updateById(realOrder);
        return R.success("订单状态修改成功");
    }
}
