package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.CustomerOrderMapper;
import com.xxxx.crm.query.CustomerOrderQuery;
import com.xxxx.crm.query.CustomerQuery;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.vo.CustomerOrder;
import com.xxxx.crm.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerOrderService extends BaseService<CustomerOrder,Integer> {

    @Autowired
    private CustomerOrderMapper orderMapper;

    public Map<String,Object> queryCustomerOrdersByParams(CustomerOrderQuery customerQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(customerQuery.getPage(),customerQuery.getLimit());
        PageInfo<CustomerOrder> pageInfo=new PageInfo<CustomerOrder>(selectByParams(customerQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }

    public Map<String,Object> queryCustomerOrderByOrderId(Integer orderId) {
       return  orderMapper.queryCustomerOrderByOrderId(orderId);
    }
}
