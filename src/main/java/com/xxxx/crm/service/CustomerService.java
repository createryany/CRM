package com.xxxx.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxxx.base.BaseService;
import com.xxxx.crm.dao.CustomerLossMapper;
import com.xxxx.crm.dao.CustomerMapper;
import com.xxxx.crm.dao.CustomerOrderMapper;
import com.xxxx.crm.query.CustomerQuery;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.vo.Customer;
import com.xxxx.crm.vo.CustomerLoss;
import com.xxxx.crm.vo.CustomerOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CustomerService extends BaseService<Customer,Integer> {


    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerLossMapper customerLossMapper;

    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    public Map<String,Object> queryCustomersByParams(CustomerQuery customerQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(customerQuery.getPage(),customerQuery.getLimit());
        PageInfo<Customer> pageInfo=new PageInfo<Customer>(selectByParams(customerQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }

    public  void saveCustomer(Customer customer){
        /**
         * 1.参数校验
         *     客户名称  name 非空 不可重复
         *     phone 联系电话  非空 格式合法
         *     法人  fr 非空
         * 2.参数默认值
         *     isValid
         *     createDate
         *     updateDate
         *     state  流失状态  0-未流失 1-已流失
         *3.执行添加 判断结果
         */
        checkParams(customer.getName(),customer.getPhone(),customer.getFr());
        AssertUtil.isTrue(null !=customerMapper.queryCustomerByName(customer.getName()),"该客户已存在!");

        customer.setIsValid(1);
        customer.setCreateDate(new Date());
        customer.setUpdateDate(new Date());
        customer.setState(0);

        // 设置客户编号
        String khno ="KH_"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        customer.setKhno(khno);
        AssertUtil.isTrue(insertSelective(customer)<1,"客户记录添加失败!");
    }

    private void checkParams(String name, String phone, String fr) {
        AssertUtil.isTrue(StringUtils.isBlank(name),"请指定客户名称!");
        AssertUtil.isTrue(!(PhoneUtil.isMobile(phone)),"手机号格式非法!");
        AssertUtil.isTrue(StringUtils.isBlank(fr),"请指定公司法人!");
    }


    public  void updateCustomer(Customer customer){
        /**
         * 1.参数校验
         *     id 存在性校验
         *     客户名称  name 非空 不可重复
         *     phone 联系电话  非空 格式合法
         *     法人  fr 非空
         * 2.参数默认值
         *     updateDate
         *3.执行更新 判断结果
         */
        Customer temp =selectByPrimaryKey(customer.getId());
        AssertUtil.isTrue(null ==temp,"待更新的客户记录不存在!");
        checkParams(customer.getName(),customer.getPhone(),customer.getFr());
        temp =customerMapper.queryCustomerByName(customer.getName());
        AssertUtil.isTrue(null !=temp && !(temp.getId().equals(customer.getId())),"该客户已存在!");
        customer.setUpdateDate(new Date());
        AssertUtil.isTrue(updateByPrimaryKeySelective(customer)<1,"客户记录更新失败!");
    }

    public void deleteCustomer(Integer id) {
        Customer customer = selectByPrimaryKey(id);
        AssertUtil.isTrue(null == customer,"待删除的客户记录不存在!");
        customer.setIsValid(0);
        AssertUtil.isTrue(updateByPrimaryKeySelective(customer)<1,"客户记录删除失败!");
    }


    public void updateCustomerState(){
        /**
         * 1.查询待流失的客户数据
         *
         * 2.将流失的客户数据批量化添加到客户流失表中
         *
         * 3.批量更新客户流失状态
         *
         * 4.通过定时任务 定时流转流失客户数据到客户流失表中
         */
        List<Customer> customers =customerMapper.queryLossCustomers();
        if(null !=customers && customers.size()>0){
            List<CustomerLoss> customerLosses=new ArrayList<CustomerLoss>();
            List<Integer> lossCusIds=new ArrayList<Integer>();
            customers.forEach(c->{
                CustomerLoss customerLoss=new CustomerLoss();
                // 0-暂缓流失状态   1-确认流失状态
                customerLoss.setState(0);
                customerLoss.setCreateDate(new Date());
                customerLoss.setCusManager(c.getCusManager());
                customerLoss.setCusName(c.getName());
                customerLoss.setCusNo(c.getKhno());
                customerLoss.setIsValid(1);
                customerLoss.setUpdateDate(new Date());
                CustomerOrder customerOrder = customerOrderMapper.queryLastCustomerOrderByCusId(c.getId());
                if(null !=customerOrder){
                    customerLoss.setLastOrderTime(customerOrder.getOrderDate());
                }
                customerLosses.add(customerLoss);
                lossCusIds.add(c.getId());
            });
            AssertUtil.isTrue(customerLossMapper.insertBatch(customerLosses)!=customerLosses.size(),"客户数据流转失败!");
            AssertUtil.isTrue(customerMapper.updateCustomerStateByIds(lossCusIds)!=lossCusIds.size(),"客户数据流转失败!");
        }

    }


    public Map<String,Object> queryCustomerContributionByParams(CustomerQuery customerQuery){
        Map<String,Object> map=new HashMap<String,Object>();
        PageHelper.startPage(customerQuery.getPage(),customerQuery.getLimit());
        PageInfo<Map<String,Object>> pageInfo=new PageInfo<Map<String,Object>>(customerMapper.queryCustomerContributionByParams(customerQuery));
        map.put("code",0);
        map.put("msg","");
        map.put("count",pageInfo.getTotal());
        map.put("data",pageInfo.getList());
        return  map;
    }

    public Map<String,Object> countCutomerMake(){
        Map<String,Object> result=new HashMap<String,Object>();
        List<Map<String,Object>> list= customerMapper.countCustomerMake();
        List<String> data1=new ArrayList<String>();
        List<Integer> data2=new ArrayList<Integer>();
        /**
         * result
         *    data1:["大客户","合作伙伴"]
         *    data2:[10,20]
         */
        list.forEach(m->{
            data1.add(m.get("level").toString());
            data2.add(Integer.parseInt(m.get("total").toString()));
        });
        result.put("data1",data1);
        result.put("data2",data2);
        return result;
    }


    public Map<String,Object> countCutomerMake02(){
        Map<String,Object> result=new HashMap<String,Object>();
        List<Map<String,Object>> list= customerMapper.countCustomerMake();
        List<String> data1=new ArrayList<String>();
        List<Map<String,Object>> data2=new ArrayList<Map<String,Object>>();
        list.forEach(m->{
            data1.add(m.get("level").toString());
            Map<String,Object> temp=new HashMap<String,Object>();
            temp.put("name",m.get("level"));
            temp.put("value",m.get("total"));
            data2.add(temp);
        });
        result.put("data1",data1);
        result.put("data2",data2);
        return result;
    }


}
