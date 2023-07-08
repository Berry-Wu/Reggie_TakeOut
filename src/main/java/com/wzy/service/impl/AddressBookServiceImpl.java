package com.wzy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzy.entity.AddressBook;
import com.wzy.mapper.AddressBookMapper;
import com.wzy.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @author wzy
 * @creat 2023-06-26-22:11
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
