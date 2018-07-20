import java.io.BufferedWriter;
import java.io.*;
import java.util.List;

public class FileAccess {
    public static String head = "Cites,Authors,Title,Year,Source,Publisher,ArticleURL,CitesURL,GSRank,QueryDate,Type,DOI,ISSN,CitationURL,Volume,Issue,StartPage,EndPage,ECC,CitesPerYear,CitesPerAuthor,AuthorCount,Age\n";

    public static void fileWriter(String filePath, List<String> contents){
        if(contents == null) return;
        try{
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"GBk"));
            writer.write(head);
            for(String i: contents){
                writer.append(i + "\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
