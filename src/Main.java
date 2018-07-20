import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.LogManager;

public class Main {

    public static void main(String[] args)throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // 禁止日志的输出
        LogManager.getLogManager().reset();
        long time1 = System.currentTimeMillis();
        System.out.println("开始获取作者论文基本信息……");
        List<String> articles = GetArticles_CNC.getAllArticles("张梅山", "哈尔滨工业大学");
        //List<String> articles = new LinkedList<>();
        //articles.add(ArticleInfo.getArticleInfo("遵循国际出版规律 遵守国内出版规定——ISMTE第2届亚太会议综述(Ⅱ);付国乐, 张志强, 颜帅;http://kns.cnki.net/kcms/detail/detail.aspx?filename=BJXB201802037&dbcode=CJFQ&dbname=CJFD2018&v=;2018"));
        //System.out.print(articles.get(0));
        System.out.print("论文详细信息获取完毕，开始写入文件……");
        String path = "D:\\作业\\大三下\\数据仓库与数据挖掘\\test.csv";
        FileAccess.fileWriter(path,articles);
        System.out.print("写入完毕！");
    }
}