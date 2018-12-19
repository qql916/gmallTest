package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.*;

import java.util.List;

public interface ManageService {
    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);


}
