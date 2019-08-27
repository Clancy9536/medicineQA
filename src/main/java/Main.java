import model.*;
import nlp.DependencyTreeCore;
import nlp.NlpTool;
import structs.StructuredQuery;
import structs.Triple;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/27/2019
 * TIME: 8:53 AM
 * Description:
 * ReturnValue:
 **/

public class Main {
    private static boolean isLoaded = false;
    private static NlpTool nplTool;
    private static ParaphraseModel pm;
    private static SemanticQueryGraph sqgModel;
    private static QueryClassifyModel qcModel;
    private static QueryMappingModel qmModel;
    private static TripleListModel tlModel;

    public static void main (String[] args) {
        load();
        //String question = "感冒灵适用的病是什么？";
        String question = "可以治疗感冒的药的成分是什么？";
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        ArrayList<StructuredQuery> sparql = getStructuredQueryList(question, writer);


    }

    private static ArrayList<StructuredQuery> getStructuredQueryList(String sentence, OutputStreamWriter writer) {
        File outputFile = new File("./data/knowledgebase/query_out.txt");
        ArrayList<Triple> tripleList;
        ArrayList<StructuredQuery> sparqlRankedList = null;
        long t  = System.currentTimeMillis();
        // step 1: generate dependency tree
        DependencyTreeCore ds = new DependencyTreeCore(sentence, nplTool, pm, writer);


        // get tripleList
        tripleList = tlModel.getTripleList(ds);

        // generate sparqlList
        System.out.println("generate sparql...");
        for (Triple triple : tripleList) {
            System.out.println(triple);
        }

//        // step 2: query classify
//        QueryClassifyModel.queryType type = qcModel.QueryClassify(ds);
//        System.out.println("Query classify: " + type);
//
//        // step 3: generate semantic query graph
//        sqgModel.buildSemanticQueryGraph(ds,type);
//        tripleList = sqgModel.getTripleList();
//
//        // step 4: generate SPARQL
//        sparqlRankedList = qmModel.getSparqlList(tripleList);
//
//        int sqNum=0;
//        if(sparqlRankedList != null)
//            for (StructuredQuery sq : sparqlRankedList)
//            {
//                System.out.println("["+ (++sqNum) + "] " + sq.score);
//                System.out.println(sq);
//				//writer.write("sparql:\n"+sq);
//            }


        System.out.println("sparql generating time = "+(System.currentTimeMillis()-t)+"ms");
        return sparqlRankedList;
    }

    private static void load() {
        if (!isLoaded) {
            nplTool = new NlpTool();
            pm = new ParaphraseModel();
            sqgModel = new SemanticQueryGraph(pm);
            qcModel = new QueryClassifyModel(pm);
            qmModel = new QueryMappingModel();
            tlModel = new TripleListModel();
            isLoaded = true;
        }


    }
}
