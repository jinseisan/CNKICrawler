import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.LogManager;

public class GetArticles_CNC {
    public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // 禁止日志的输出
        LogManager.getLogManager().reset();
        long time1 = System.currentTimeMillis();
        //System.out.println(getAllArticles("张志强", "南京大学"));
        System.out.print(ArticleInfo.getArticleInfo("遵循国际出版规律 遵守国内出版规定——ISMTE第2届亚太会议综述(Ⅱ);付国乐, 张志强, 颜帅;http://kns.cnki.net/kcms/detail/detail.aspx?filename=BJXB201802037&dbcode=CJFQ&dbname=CJFD2018&v="));
    }

    private static int count = 1;
    /**
     * 以专家的姓名为关键词使用知网的学者搜索获取专家的所有论文的链接
     *
     * @param author 作者姓名
     * @param expertOrg 作者机构
     * @return 论文对应链接的list集合
     * @throws FailingHttpStatusCodeException
     * @throws MalformedURLException
     * @throws IOException
     */
    public static List<String> getAllArticles(String author, String expertOrg)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // 获取客户端，禁止JS
        WebClient webClient = HtmlUtil.iniParam_Js();
        // 获取搜索页面
        @SuppressWarnings("deprecation")
        String url = "http://kns.cnki.net/kcms/detail/knetsearch.aspx?sfield=au&skey=" + URLEncoder.encode(author);
        //获取搜索页面，搜索页面包含多个学者，机构通常是非完全匹配，姓名是完全匹配的，我们需要对所有的学者进行匹配操作
        HtmlPage page = webClient.getPage(url);
        //获取搜索页面的机构对应的节点
        DomElement orgDom = page.getFirstByXPath("/html/body/div[7]/div[1]/div/p[1]");
        if (orgDom == null)// 当机构节点为空时表示没有搜索到结果，直接返回空
            return null;
        //获取机构名称
        String firstOrg = orgDom.asText();
        // 判断学者机构和专家机构是否匹配
        if (firstOrg.equals(expertOrg))
            // 如果匹配则返回该学者的所有论文链接集合
            return getArticlesOnCurAuthor(page);
        else {// 否则需要获取其他同名作者的机构并判断是否匹配并判断
            DomElement dupNameAuthorsDom = page.getFirstByXPath("/html/body/div[7]/div[2]/ul");//包含其他学者的节点
            if (dupNameAuthorsDom == null)
                return null;
            //循序每一个学者的节点并判断是否匹配
            for (DomElement authorDom : dupNameAuthorsDom.getChildElements()) {
                String curOrg = authorDom.getFirstElementChild().getNextElementSibling().asText();
                if (curOrg.equals(expertOrg)) {
                    String onclickAttr = authorDom.getFirstElementChild().getFirstElementChild()
                            .getAttribute("onclick");
                    String id = onclickAttr.substring(onclickAttr.indexOf(",") + 2, onclickAttr.lastIndexOf("'"));
                    String curUrl = url + "&code=" + id;
                    HtmlPage curPage = webClient.getPage(curUrl);
                    return getArticlesOnCurAuthor(curPage);
                }
            }
        }
        return null;
    }

    /**
     * 获取某个学者页面中的所有的论文，这里的论文包含期刊论文，会议论文和硕博论文
     *
     * @param page
     * @return
     * @throws IOException
     */
    public static List<String> getArticlesOnCurAuthor(HtmlPage page) throws IOException {
        List<String> articlesList = new LinkedList<>();
        WebClient webClient = page.getWebClient();
        page.getElementById("lcatalog_1").click();// 发表在期刊上的文献
        page.getElementById("lcatalog_3").click();// 发表在会议上的文献
        page.getElementById("lcatalog_2").click();// 发表在博硕上的文献
        webClient.waitForBackgroundJavaScriptStartingBefore(5000);// 设置等待时间为5s
        HtmlPage jPage = (HtmlPage) webClient.getWebWindowByName("framecatalog_1").getEnclosedPage();
        HtmlPage cPage = (HtmlPage) webClient.getWebWindowByName("framecatalog_3").getEnclosedPage();
        HtmlPage sPage = (HtmlPage) webClient.getWebWindowByName("framecatalog_2").getEnclosedPage();
        articlesList.addAll(getPageArticles(jPage, 1));
        articlesList.addAll(getPageArticles(cPage, 2));
        articlesList.addAll(getPageArticles(sPage, 3));
        return articlesList;
    }

    /**
     * 根据页面类型获取所有分页论文
     *
     * @param page 当前页面
     * @param type 页面类型 1表示期刊论文页面，2表示会议论文页面，3表示硕博论文页面
     * @return 所有论文列表的字符串形式
     * @throws IOException
     */
    public static List<String> getPageArticles(HtmlPage page, int type) throws IOException {
        List<String> articlesList = new LinkedList<>();
        // 获取不同类型页面分页数目
        String id = "";// 页面中论文总数节点的id属性值
        if (type == 1)
            id = "pc_CJFQ";
        else if (type == 2)
            id = "pc_CPFD";
        else if (type == 3)
            id = "pc_CMFD";
        DomElement aCountDom = page.getElementById(id);// 论文数量节点
        // 如果论文数量节点为空则直接返回空字符串
        if (aCountDom == null)
            return articlesList;
        HtmlPage tempPage = page;
        articlesList.addAll(getArticlesByNode(tempPage));// 将当前页面中的所有论文添加到
        while (true) {
            DomElement nextDom = null;// 获取下一个及节点
            try {
                nextDom = tempPage.getAnchorByText("下一页");
            } catch (ElementNotFoundException e) {
                // TODO: handle exception
            }
            if (nextDom == null)
                break;
            tempPage = nextDom.click();// 获取下一页页面
            page.getWebClient().waitForBackgroundJavaScriptStartingBefore(1000);// 设置等待时间为1S
            articlesList.addAll(getArticlesByNode(tempPage));// 将当前页面中的所有论文添加到
        }
        return articlesList;
    }

    /**
     * 根据节点获取单个页面的所有论文信息
     *
     * @param page
     *            页面，这里的页面指包含论文的页面，期刊论文页面、会议论文页面和硕博论文页面
     * @return
     */
    public static List<String> getArticlesByNode(HtmlPage page) {
        DomElement articlesNode = page.getFirstByXPath("/html/body/div[1]/div/div[2]/ul");
        List<String> articlesList = new LinkedList<>();
        for (DomElement li : articlesNode.getChildElements()) {
            HtmlAnchor anchor = (HtmlAnchor) li.getFirstElementChild().getNextElementSibling();
            String articleUrl = "http://kns.cnki.net" + anchor.getHrefAttribute();//连接
            String articleTitle = anchor.asText().replace(",","，").replace("\"","'");//题名
            String articleAuthors = anchor.getNextSibling().asText().split(" ")[1].replace(".","").trim().replaceAll("\\d+","").replace(",",", ").replace("\"","'");//作者
            String articleYear = articleUrl.substring(articleUrl.length()-7, articleUrl.length()-3);
            try{
                //System.out.println(articleTitle + ";" + articleAuthors + ";" + articleUrl + ";" + articleYear);
                articlesList.add(ArticleInfo.getArticleInfo(articleTitle + ";" + articleAuthors + ";" + articleUrl + ";" + articleYear));
                System.out.print(count+":");
                System.out.println(articlesList.get(articlesList.size()-1));
                count++;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return articlesList;
    }

    /**
     * 以姓名和机构为关键词，使用知网的高级搜索获取专家的所有论文
     *
     * @param author 专家姓名
     * @param org 专家机构
     * @return 论文list
     * @throws IOException
     */
    public static List<String> getArticlesByKeywords(String author, String org) throws IOException {
        HtmlPage researchPage = HtmlUtil.getPage_Js("http://kns.cnki.net/kns/brief/result.aspx?dbprefix=SCDB");// 获取知网的高级搜索页面
        HtmlTextInput authorInput = researchPage.getFirstByXPath("//*[@id=\"au_1_value1\"]");// 作者文本框
        authorInput.setText(author);// 将作者填入
        HtmlTextInput orgInput = researchPage.getFirstByXPath("//*[@id=\"au_1_value2\"]");// 机构文本框
        orgInput.setText(org);// 将机构填入
        HtmlButtonInput searchButton = researchPage.getFirstByXPath("//*[@id=\"btnSearch\"]");// 提交按钮
        searchButton.click();// 获取提交后的页面
        researchPage.getWebClient().waitForBackgroundJavaScriptStartingBefore(5000);// 等待5S
        // 获取文章的iFrame页面
        HtmlPage articlePage = (HtmlPage) researchPage.getWebClient().getWebWindowByName("iframeResult")
                .getEnclosedPage();
        // 获取第一篇文章节点
        DomElement firstArticleDom = ((DomElement) articlePage
                .getFirstByXPath("//*[@id=\"ctl00\"]/table/tbody/tr[2]/td/table/tbody")).getFirstElementChild()
                .getNextElementSibling();
        while (firstArticleDom != null) {
            HtmlPage authorPage = getMatchedUrl(firstArticleDom, org);
            if (authorPage != null)
                return getArticlesOnCurAuthor(authorPage);
            else {
                firstArticleDom = firstArticleDom.getNextElementSibling();
            }
        }
        return null;
    }

    /**
     * 根据机构名获取匹配的学者主页
     *
     * @param articleDom 文章节点
     * @param org 机构
     * @return 如果学者主页的机构名和专家机构名匹配则返回匹配的学者主页，否则返回null
     * @throws IOException
     */
    public static HtmlPage getMatchedUrl(DomElement articleDom, String org) throws IOException {
        DomElement authorDom = articleDom.getFirstElementChild().getNextElementSibling().getNextElementSibling();
        for (DomElement curAuthorAnchor : authorDom.getChildElements()) {
            if (curAuthorAnchor.getChildElementCount() != 0) {
                HtmlPage curPage = curAuthorAnchor.click();
                DomElement orgDom = curPage.getFirstByXPath("/html/body/div[7]/div[1]/div/p[1]/a");
                if (orgDom.asText().contains(org))
                    return curPage;
            }
        }
        return null;
    }

}
