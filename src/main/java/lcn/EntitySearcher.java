package lcn;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import utils.EditDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Author: wangqing31@midea.com
 * Date: 2019-8-6
 * Time: 15:24
 * Description:
 * ReturnValue:
 */

public class EntitySearcher {
    public QueryParser parser;
    public StandardAnalyzer analyzer;
    public IndexSearcher searcher;
    public HashSet<String> stopWordSet;


    File banFile = new File("./models/banWords.txt");
    public static String indexPath = "./data/index/entity_index";

    public EntitySearcher()
    {
        try
        {
            System.out.println("indexpath:"+indexPath);
            searcher = new IndexSearcher(indexPath);
            analyzer = new StandardAnalyzer();
            parser = new QueryParser("EntityName", analyzer);

            stopWordSet = new HashSet<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(banFile), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null)
            {
                stopWordSet.add(line);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String searchEntityAndType(String queryString)
    {
        String entityName = null, entityType = null;
        double maxScore = 0.0;

        if (stopWordSet.contains(queryString))
            return null;
        try
        {
            Query query = parser.parse(queryString);

            Hits hits = searcher.search(query);
            for (int i=0;i<hits.length() && i<20;i++)
                if (hits.score(i) > maxScore|| EditDistance.calcutateEditDistance(queryString, hits.doc(i).get("EntityName"))==0)
                {
                    maxScore = hits.score(i);
                    entityName = hits.doc(i).get("EntityName");
                    entityType = hits.doc(i).get("EntityType");
                }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (maxScore >= 0.7 && EditDistance.calcutateEditDistance(queryString, entityName) * 4 <= queryString.length())
            return entityName + "-" + entityType;
        else
            return null;
    }
    
}
