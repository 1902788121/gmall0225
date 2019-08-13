package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.beans.PmsSearchParam;
import com.atguigu.gmall.beans.PmsSearchSkuInfo;
import com.atguigu.gmall.beans.PmsSkuAttrValue;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {
        String searchQueryStr = getSearchQueryStr(pmsSearchParam);
        System.err.println(searchQueryStr);
        Search search = new Search.Builder(searchQueryStr).addIndex("gmall0225").addType("PmsSearchSkuInfo").build();

        // 查询方法
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);

        if(hits!=null&&hits.size()>0){
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
                // 解析高亮
                Map<String, List<String>> highlight = hit.highlight;
                if(highlight!=null&&!highlight.isEmpty()){
                    List<String> skuName = highlight.get("skuName");
                    pmsSearchSkuInfo.setSkuName(skuName.get(0));
                }
                pmsSearchSkuInfos.add(pmsSearchSkuInfo);
            }
            System.out.println(pmsSearchSkuInfos.size());
        }

        // 解析聚合结果
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            Long count = bucket.getCount();
            System.out.println("key:"+valueId+",value:"+count);
        }

        return pmsSearchSkuInfos;
    }

    public String getSearchQueryStr(PmsSearchParam pmsSearchParam){

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 封装分页
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 匹配
        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword );
            boolQueryBuilder.must(matchQueryBuilder);
        }


        // 过滤
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        String[] valueIds= pmsSearchParam.getValueId();
        if(valueIds!=null&&valueIds.length>0){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        // 封装query
        searchSourceBuilder.query(boolQueryBuilder);

        // 封装高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        // 聚合函数
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);


        return searchSourceBuilder.toString();
    }
}
