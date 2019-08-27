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
 * Date: 2019-8-6
 * Time: 16:40
 * Description:
 * ReturnValue:
 */

public class BuildEntityIndex {
    public void buildEntityIndex()
    {
        System.out.println("buildEntityIndex() ...");
        File inputFile = new File("./data/knowledgebase/medicine_data.txt");
        File outputFile = new File("./data/index/entity_index");
        try
        {
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriter indexWriter = new IndexWriter(outputFile, analyzer,true);
            int mergeFactor = 100000;    //默认是10
            int maxBufferedDoc = 1000;  // 默认是10
            int maxMergeDoc = Integer.MAX_VALUE;  //默认无穷大

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
                String sub = contents[0].substring(1, contents[0].length()-1);
                String prd = contents[1].substring(1, contents[1].length()-1);
                //新版gstore每行结尾多一个符号“.”,为保险起见，运行代码时保证数据是没有"."的。
                if(1>=contents[2].length()-1)
                    continue;
                String obj = contents[2].substring(1, contents[2].length()-1);


                Document doc = new Document();
                Field entityName = new Field("EntityName", sub, Field.Store.YES,
                        Field.Index.TOKENIZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS);
                Field entityType = new Field("EntityType", obj,
                        Field.Store.YES, Field.Index.NO);
                doc.add(entityName);
                doc.add(entityType);
                indexWriter.addDocument(doc);
            }
            indexWriter.optimize();
            indexWriter.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("buildEntityIndex() done.");
    }

    public static void main(String[] args)
    {
        BuildEntityIndex bei = new BuildEntityIndex();
        bei.buildEntityIndex();
    }
}
