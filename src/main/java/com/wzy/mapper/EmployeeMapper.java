package com.wzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wzy
 * @creat 2023-06-11-19:34
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
