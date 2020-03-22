package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;


public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

  /*  //根据页面名称查询
    CmsPage findByPageName(String pageName);
    //根据页面名称与类型
    CmsPage findByPageNameAndPageType(String pageName,String pageType);
    //根据站点和页面类型
    int countBySiteIdAndPageType(String siteId,String pageType);
    //站点和页面类型分页查询
    Page<CmsPage> findBySiteIdAndPageType(String sitId, String pageType, Pageable pageable);*/

    //根据页面名称，站点id，页面访问路径
    public CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);

}
