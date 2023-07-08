package com.wzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzy.entity.Category;

/**
 * @author wzy
 * @creat 2023-06-19-21:28
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
