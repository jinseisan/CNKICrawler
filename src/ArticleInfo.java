import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class ArticleInfo {
    public static String getArticleInfo(String basicInfo)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String articleInfo;
        //分离基本信息
        String[] info = basicInfo.split(";");
        String title = info[0];
        String authors = info[1];
        String url = info[2];
        String year = info[3];
        // 获取客户端，禁止JS
        WebClient webClient = HtmlUtil.iniParam_Js();
        // 获取搜索页面
        @SuppressWarnings("deprecation")
        //获取文献信息页面
        HtmlPage page = webClient.getPage(url);
        //获取文献的引证的节点
        DomNode citesDom = page.getElementById("MapArea").getFirstElementChild().getFirstElementChild().getLastChild().getPreviousSibling().getLastChild();
        String cites;
        if(citesDom != null){
            cites = citesDom.asText().replace("(","").replace(")","");
        }else{
            cites = "0";
        }
        //System.out.println(cites);
        //获取文献发表年份节点
        //DomElement yearDom = page.getElementById("AxisFrameDivCurrent").getFirstElementChild().getFirstElementChild().getLastElementChild();
        //String year = yearDom.asText();
        articleInfo = cites + ",\"" + authors + "\","+ title + "," + year + ",,,,,,,,,,,,,,,"+ cites + ",,,,";
        return articleInfo;

    }
}
