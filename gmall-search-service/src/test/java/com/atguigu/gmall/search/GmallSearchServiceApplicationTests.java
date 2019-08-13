package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.beans.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
public class GmallSearchServiceApplicationTests {

	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;

	public  void indexTest() throws IOException {

		List<PmsSkuInfo> pmsSkuInfos = skuService.getAllSku();

		List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();


		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			// 将java的sku转化为es的sku
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
			String id = pmsSkuInfo.getId();
			long l = Long.parseLong(id);
			pmsSearchSkuInfo.setId(l);
			pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
		}


		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
			Index index = new Index.Builder(pmsSearchSkuInfo).index("gmall0225").type("PmsSearchSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();

			JestResult execute = jestClient.execute(index);
		}
	}

	public void searchTest() throws IOException {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(20);
		searchSourceBuilder.from(0);

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

		// 匹配
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "尚硅谷拯救世界");
		boolQueryBuilder.must(matchQueryBuilder);

		// 过滤
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "110");
		boolQueryBuilder.filter(termQueryBuilder);

		// 封装query
		searchSourceBuilder.query(boolQueryBuilder);

		System.err.println(searchSourceBuilder.toString());
		Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0225").addType("PmsSearchSkuInfo").build();


		// 查询方法
		SearchResult searchResult = jestClient.execute(search);

		List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);

		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
			PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
			pmsSearchSkuInfos.add(pmsSearchSkuInfo);
		}
		System.out.println(pmsSearchSkuInfos.size());

	}

	@Test
	public void contextLoads() throws IOException {
		indexTest();


	}

}
