package com.wzy.dto;

import com.wzy.entity.OrderDetail;
import com.wzy.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee; //收货人

    private List<OrderDetail> orderDetails;
	
}
