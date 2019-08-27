package lcn;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import structs.Item;
import structs.Triple;

import java.util.HashSet;

/**
 * Author: wangqing31@midea.com
 * Date: 2019-8-7
 * Time: 10:13
 * Description:
 * ReturnValue:
 */

public class FragmentChecker {
    public IndexSearcher searcher;
    public QueryParser parser;
    public Analyzer analyzer;

    public static String indexPath = "./data/index/fragment_index";

    public FragmentChecker()
    {
        try
        {
            searcher = new IndexSearcher(indexPath);
            analyzer = new StandardAnalyzer();
            parser = new QueryParser("FragmentIndex", analyzer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean checkTriple(Triple triple)
    {
        Item subject = triple.subject, predicate = triple.predicate, object = triple.object;
        String subString = subject.toString(), preString = predicate.toString(), objString = object.toString();
        HashSet<String> candidates = new HashSet<String>();

        //VPE
        if (subject.type == Item.ItemType.variable &&
                predicate.type == Item.ItemType.relation &&
                object.type == Item.ItemType.entity)
        {
            candidates = searchFragmentInEdge(objString);
            if (candidates.contains(preString))
                return true;
            else
                return false;
        }
        //EPV
        else if (subject.type == Item.ItemType.entity &&
                predicate.type == Item.ItemType.relation &&
                object.type == Item.ItemType.variable)
        {
            candidates = searchFragmentOutEdge(subString);
            if (candidates.contains(preString))
                return true;
            else
                return false;
        }
        //VPL
        else if (subject.type == Item.ItemType.variable &&
                predicate.type == Item.ItemType.relation &&
                object.type == Item.ItemType.literal)
        {
            candidates = searchFragmentInEdge(objString);
            if (candidates.contains(preString))
                return true;
            else
                return false;
        }
        else if (subject.type == Item.ItemType.variable &&
                predicate.type == Item.ItemType.relation &&
                object.type == Item.ItemType.variable)
        {
            return true;
        }

        return false;
    }

    public HashSet<String> searchFragmentInEdge(String queryString)
    {
        HashSet<String> ret = new HashSet<String>();
        String index = null, inEdges = null;

        try
        {
            Query query = parser.parse(queryString);
            Hits hits = searcher.search(query);

            for (int i=0;i<hits.length() && i<10;i++)
            {
                index = hits.doc(i).get("FragmentIndex");
                if (index.equals(queryString))
                {
                    inEdges = hits.doc(i).get("InEdge");
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (inEdges != null)
        {
            String[] contents = inEdges.split(",");
            for (int i=0;i<contents.length;i++)
                if (!contents[i].equals("null"))
                    ret.add(contents[i]);
        }

        return ret;
    }

    public HashSet<String> searchFragmentOutEdge(String queryString)
    {
        HashSet<String> ret = new HashSet<String>();
        String index = null, outEdges = null;

        try
        {
            Query query = parser.parse(queryString);

            Hits hits = searcher.search(query);
            for (int i=0;i<hits.length() && i<10;i++)
            {
                index = hits.doc(i).get("FragmentIndex");
                if (index.equals(queryString))
                {
                    outEdges = hits.doc(i).get("OutEdge");
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ret;
        }

        if (outEdges != null)
        {
            String[] contents = outEdges.split(",");
            for (int i=0;i<contents.length;i++)
                if (!contents[i].equals("null"))
                    ret.add(contents[i]);
        }

        return ret;
    }

}
