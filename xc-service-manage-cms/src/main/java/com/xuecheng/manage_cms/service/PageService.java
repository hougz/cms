package com.xuecheng.manage_cms.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 页面查询方法
     * @param page 页码，从1开始记数
     * @param size 每页记录数
     * @param queryPageRequest 查询条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
       if(queryPageRequest==null){
           queryPageRequest=new QueryPageRequest();
       }
        //条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher=ExampleMatcher.matching().
                withMatcher("pageName",ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值
        CmsPage cmsPage=new CmsPage();
        //站点id
        if(StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        if(StringUtils.isNotEmpty(queryPageRequest.getPageName())){
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //创建条件实例
        Example<CmsPage> example=Example.of(cmsPage,exampleMatcher);
        //分页参数
        if(page <=0){
            page = 1;
        }
        page = page -1;
        if(size<=0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public CmsPageResult add(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1!=null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
            return null;
        }else{
            cmsPage.setPageId(null);//添加页面主键由spring data 自动生成
            cmsPageRepository.save(cmsPage);
            //返回结果
            CmsPageResult cmsPageResult=new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
        }
    }

      public CmsPage getById(String id){
          Optional<CmsPage> byId = cmsPageRepository.findById(id);
          if(byId.isPresent()){
              return byId.get();
          }
          return null;
      }

      public CmsPageResult update(String id,CmsPage cmsPage){
          CmsPage cmsPage1 = this.getById(id);
          if(cmsPage1!=null){
              //更新模板id
              cmsPage1.setTemplateId(cmsPage.getTemplateId());
              //更新所属站点             
              cmsPage1.setSiteId(cmsPage.getSiteId());
              //更新页面别名
              cmsPage1.setPageAliase(cmsPage.getPageAliase());
              //更新页面名称             
              cmsPage1.setPageName(cmsPage.getPageName());
              //更新访问路径             
              cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
              // 更新物理路径             
              cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
              // 执行更新             
              CmsPage save=cmsPageRepository.save(cmsPage1);
              if(save!=null){
                  CmsPageResult cmsPageResult=new CmsPageResult(CommonCode.SUCCESS,save);
                  return cmsPageResult;
              }
          }
          return new CmsPageResult(CommonCode.FAIL,null);
      }

      public ResponseResult delete(String id){
          CmsPage cmsPage = this.getById(id);
          if(cmsPage!=null){
              cmsPageRepository.deleteById(id);
              return new ResponseResult(CommonCode.SUCCESS);
          }
          return new ResponseResult(CommonCode.FAIL);
      }


      /**
         页面静态化
       */
      public String getPageHtml(String pageId){
          //获取模型数据
          Map model= this.getModelByPageId(pageId);
          if(model==null){
              ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
          }
          //获取页面模板
          String template= this.getTemplateByPageId(pageId);
          if(template==null){
              ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
          }
          //执行静态化
          String html = this.generateHtml(template, model);
          if(StringUtils.isEmpty(html)){
              ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
          }
          return html;
      }

    /**
     * 获取模型数据
     */
    private Map getModelByPageId(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataUrl         
        String dataUrl=cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
    获取页面模板
     */
    public String getTemplateByPageId(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = cmsPage.getTemplateId();
        if(StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(templateId);
        if(byId.isPresent()){
            CmsTemplate cmsTemplate = byId.get();
            //模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            GridFSFile gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFsFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource=new GridFsResource(gridFsFile,gridFSDownloadStream);
           try{
            //获取流中的数据
            String string = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
            return string;
            } catch (Exception e) {
            e.printStackTrace();
            }
        }
        return null;

    }
    /**
     * 执行静态化
     */
    public String generateHtml(String template,Map model){
        try {
            //生成配置类
            Configuration configuration=new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader=new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",template);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template1 = configuration.getTemplate("template");
            String string = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        }
          return null;
    }

}
