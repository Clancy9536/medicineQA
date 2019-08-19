package lcn;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Author: wangqing31@midea.com
 * Date: 2019-8-7
 * Time: 10:26
 * Description:
 * ReturnValue:
 */

public class BuildFragmentIndex {
    public void buildFragmentIndex()
    {
        System.out.println("buildFragmentIndex() ...");

        File inputFile = new File("./data/fragment/entity_fragment.txt");
        File outputFile = new File("./data/index/fragment_index");
        try
        {
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriter indexWriter = new IndexWriter(outputFile, analyzer,true);
            int mergeFactor = 100000;    //默认是10
            int maxBufferedDoc = 1000;  // 默认是10
            int maxMergeDoc = Integer.MAX_VALUE;  //默认无穷大

            //indexWriter.DEFAULT_MERGE_FACTOR = mergeFactor;
            indexWriter.setMergeFactor(mergeFactor);
            indexWriter.setMaxBufferedDocs(maxBufferedDoc);
            indexWriter.setMaxMergeDocs(maxMergeDoc);

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"utf-8"));
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null)
            {
                if ((++lineCount)%20000==0) System.out.println("line="+lineCount);

                String[] contents = line.split("\t");
                String index = contents[0];
                String inEdge = contents[1];
                String outEdge = contents[2];
                Document doc = new Document();
                Field indexField = new Field("FragmentIndex", index, Field.Store.YES,
                        Field.Index.TOKENIZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS);
                Field inEdgeField = new Field("InEdge", inEdge,
                        Field.Store.YES, Field.Index.NO);
                Field outEdgeField = new Field("OutEdge", outEdge,
                        Field.Store.YES, Field.Index.NO);
                doc.add(indexField);
                doc.add(inEdgeField);
                doc.add(outEdgeField);
                indexWriter.addDocument(doc);
            }

            indexWriter.optimize();
            indexWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("buildFragmentIndex() done.");
    }

    public static void main(String[] args)
    {
        BuildFragmentIndex bfi = new BuildFragmentIndex();
        bfi.buildFragmentIndex();
    }
}
