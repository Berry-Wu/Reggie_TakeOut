package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.Employee;
import com.wzy.mapper.EmployeeMapper;
import com.wzy.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-11-19:38
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
