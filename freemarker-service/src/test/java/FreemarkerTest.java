import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {





    @Test
    public void testGridFs(){
        try {
            File file=new File("d:/index_banner.ftl");
            FileInputStream fileInputStream=new FileInputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
