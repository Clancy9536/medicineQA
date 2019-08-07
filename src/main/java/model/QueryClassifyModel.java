package model;

import nlp.DependencyTreeCore;
import structs.DependencyTreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 3:15 PM
 * Description:
 * ReturnValue:
 **/

public class QueryClassifyModel {
    public boolean containInstanceMedicine;
    public boolean containInstanceIllness ;
    public boolean containConceptMedicine;
    public boolean containConceptIllness;
    public boolean containPredicateMedicine;
    public boolean containPredicateIllness;
    public boolean containPredicateM2I;

    public queryType type;

    public ArrayList<String> medicinePredicateList = new ArrayList<String>();
    public ArrayList<String> illnessPredicateList = new ArrayList<String>();
    public ArrayList<String> m2iPredicateList = new ArrayList<String>();

    public enum queryType{askAttribute, askMedicine, askIllness, unresolved};
    private static ParaphraseModel pm;

    public QueryClassifyModel(ParaphraseModel pM)
    {
        QueryClassifyModel.pm = pM;
    }

    public queryType QueryClassify(DependencyTreeCore dtc)
    {
        init();
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(dtc.root == null)
            return queryType.unresolved;
        queue.add(dtc.root);
        DependencyTreeNode h,p;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }

            String name = h.word.name;
            String posTag = h.posTag;

            if(posTag.equals("entity-medicine"))
                containInstanceMedicine = true;
            if(posTag.equals("entity-illness"))
                containInstanceIllness = true;

            if(posTag.equals("type-medicine"))
                containConceptMedicine = true;
            if(posTag.equals("type-illness"))
                containConceptIllness = true;
            if(posTag.equals("predicate"))
            {
                String pre = pm.getRelatedRelation(name).item.name;
                if(medicinePredicateList.contains(pre))
                    containPredicateMedicine = true;
                if(illnessPredicateList.contains(pre))
                    containPredicateIllness = true;
                if(m2iPredicateList.contains(pre))
                    containPredicateM2I = true;
            }
        }

        //Medicine
        if(containInstanceMedicine && containInstanceIllness)
            type = queryType.unresolved;
        else if(containInstanceMedicine)    //感冒灵
        {
            if(containConceptIllness || containPredicateM2I)    //
                type = queryType.askIllness;
            else if(containPredicateMedicine)
                type = queryType.askAttribute;
            else
                type = queryType.askMedicine;
        }
        else if(containInstanceIllness)
        {
            if(containConceptMedicine)
                type = queryType.askMedicine;
            else if(containPredicateIllness)
                type = queryType.askAttribute;
            else
                type = queryType.askIllness;
        }
        else if(containConceptMedicine && containConceptIllness)
            type = queryType.unresolved;
        else if(containConceptMedicine)
            type = queryType.askMedicine;
        else if(containConceptIllness)
            type = queryType.askIllness;
        else
            type = queryType.unresolved;

        return type;
    }

    void init()
    {
        containInstanceMedicine = false;
        containInstanceIllness = false;
        containConceptMedicine = false;
        containConceptIllness = false;
        containPredicateMedicine = false;
        containPredicateIllness = false;
        containPredicateM2I = false;

        medicinePredicateList = new ArrayList<String>(Arrays.asList("适用","功效","成分","禁忌"));
        illnessPredicateList = new ArrayList<String>(Arrays.asList("特点","症状","危害","又名"));
        m2iPredicateList = new ArrayList<String>(Arrays.asList("适用","主治"));
    }
}
