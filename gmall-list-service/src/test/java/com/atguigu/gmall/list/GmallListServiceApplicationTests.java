package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.SkuInfo;
import com.atguigu.gmall.user.bean.SkuLsInfo;
import com.atguigu.gmall.user.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;
	@Test
	public void contextLoads() throws IOException {
		List<SkuInfo> skuInfos = skuService.getMySkuInfo("61");
		System.out.println(skuInfos);
		List<SkuLsInfo> skuLsInfos = new ArrayList<>();

		for (SkuInfo skuInfo : skuInfos) {
			SkuLsInfo skuLsInfo = new SkuLsInfo();
			BeanUtils.copyProperties(skuInfo,skuLsInfo);

			skuLsInfos.add(skuLsInfo);
		}

		for (SkuLsInfo skuLsInfo : skuLsInfos) {
			Index build = new Index.Builder(skuLsInfo).index("gmall0725").type("SkuLsInfo").id(skuLsInfo.getId()).build();
			jestClient.execute(build);
		}
	}

}
