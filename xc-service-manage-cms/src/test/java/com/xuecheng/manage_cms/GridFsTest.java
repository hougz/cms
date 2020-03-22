package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 使用GridFsTemplate存储文件测试代码
     */
    @Test
    public void testGridFs(){
        try {
            File file=new File("d:/index_banner.ftl");
            FileInputStream fileInputStream=new FileInputStream(file);
            ObjectId object = gridFsTemplate.store(fileInputStream, "轮播图测试文件01", "");
            String string = object.toString();
            System.out.println(string);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 使用GridFs读取文件
     */

    @Test
    public void queryFile(){
        try {
            GridFSFile gridFsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5e7235e68f18e92f24ab6f9a")));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFsFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource=new GridFsResource(gridFsFile,gridFSDownloadStream);
            //获取流中的数据
            String string = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
            System.out.println(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
