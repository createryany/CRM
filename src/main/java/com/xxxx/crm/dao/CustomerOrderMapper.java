package com.xxxx.crm.dao;

import com.xxxx.base.BaseMapper;
import com.xxxx.crm.vo.CustomerOrder;

import java.util.Map;

public interface CustomerOrderMapper extends BaseMapper<CustomerOrder,Integer> {

    Map<String, Object>  queryCustomerOrderByOrderId(Integer orderId);
    public CustomerOrder  queryLastCustomerOrderByCusId(Integer cusId);
}