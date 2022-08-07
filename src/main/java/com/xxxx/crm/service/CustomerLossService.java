package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.query.CustomerLossQuery;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.CustomerLoss;
import com.xxxx.crm.vo.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerLossService extends BaseService<CustomerLoss,Integer> {

    public Map<String,Object> queryCustomerLosssByParams(CustomerLossQuery customerLossQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(customerLossQuery.getPage(),customerLossQuery.getLimit());
        PageInfo<CustomerLoss> pageInfo=new PageInfo<CustomerLoss>(selectByParams(customerLossQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }

    public void updateCustomerLossStateById(Integer id, String lossReason) {
        CustomerLoss customerLoss =selectByPrimaryKey(id);
        AssertUtil.isTrue(null==customerLoss,"待流失的客户记录不存在!");
        customerLoss.setState(1);// 确认流失
        customerLoss.setLossReason(lossReason);
        customerLoss.setConfirmLossTime(new Date());
        customerLoss.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(customerLoss)<1,"确认流失失败!");
    }
}
