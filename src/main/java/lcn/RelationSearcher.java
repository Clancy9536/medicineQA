package lcn;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

/**
 * Author: wangqing31@midea.com
 * Date: 2019-8-7
 * Time: 10:40
 * Description:
 * ReturnValue:
 */

public class RelationSearcher {
    public IndexSearcher searcher;
    public QueryParser parser;
    public Analyzer analyzer;

    public static String indexPath = "./data/index/relation_index";

    public RelationSearcher()
    {
        try
        {
            searcher = new IndexSearcher(indexPath);
            analyzer = new StandardAnalyzer();
            parser = new QueryParser("Index", analyzer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String searchRelation(String edgeDir, String entity, String type)
    {
        if (entity==null) return null;

        String queryString = edgeDir + "\t" + entity + "\t" + type;
        String relation = null;
        double maxScore = 0.0;

        try
        {
            Query query = parser.parse(queryString);

            Hits hits = searcher.search(query);
            for (int i=0;i<hits.length() && i<20;i++)
            {
                String index = hits.doc(i).get("Index");
                if (hits.score(i) > maxScore || index.equals(queryString))
                {
                    maxScore = hits.score(i);
                    if (index.equals(queryString)) maxScore=1.0;
                    relation = hits.doc(i).get("Relation");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (maxScore >= 0.7)
            return relation;
        else
            return null;
    }
}
