package model;

import structs.StructuredQuery;
import structs.Triple;

import java.util.ArrayList;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 3:15 PM
 * Description:
 * ReturnValue:
 **/

public class QueryMappingModel {

    public ArrayList<StructuredQuery> queryList;
    public ArrayList<StructuredQuery> getSparqlList(ArrayList<Triple> tripleList)
    {
        if(tripleList==null || tripleList.size()<1)
        {
            System.out.println("QueryMappingModel: "+"tripleList is empty.");
            return null;
        }

        StructuredQuery query = new StructuredQuery();
        queryList = new ArrayList<StructuredQuery>();

        for(Triple t: tripleList)
        {
            query.addTriple(t);
        }

        queryList.add(query);
        return queryList;
    }
}
