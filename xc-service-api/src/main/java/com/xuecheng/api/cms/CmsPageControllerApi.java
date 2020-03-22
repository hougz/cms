package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms页面管理接口",description="cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {

    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value="页 码",
            required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value="每页记录数",
                    required=true,paramType="path",dataType="int")})
    //required  参数是否必传
    // dataType 参数的数据类型 只作为标志说明，并没有实际验证
    // paramType 查询参数类型
        //body  以流的形式提交 仅支持POST
        //path  以地址的形式提交数据
        //query  直接跟参数完成自动映射赋值
        //from   以form表单的形式提交 仅支持POST
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);


    //新增页面
    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);

    @ApiOperation("通过ID查询页面")
    public CmsPage findById(String id);

    @ApiOperation("修改页面")
    public CmsPageResult edit(String id,CmsPage cmsPage);

    @ApiOperation("删除页面")
    public ResponseResult delete(String id);
}
