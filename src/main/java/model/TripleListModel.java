package model;

import nlp.DependencyTreeCore;
import structs.Argument;
import structs.DependencyTreeNode;
import structs.Triple;

import java.util.*;

/**
 * Author: wangqing9536@gmail.com
 * Date: 8/26/2019
 * Time: 4:02 PM
 * Description:
 * ReturnValue:
 **/

public class TripleListModel {
    public ArrayList<Triple> tripleList;
    public ArrayList<String> subjectLike = new ArrayList<String>(Arrays.asList("的字结构"));
    public ArrayList<String> objectLike = new ArrayList<String>(Arrays.asList("宾语"));
    PriorityQueue<Argument> subjectPriorityQueue = new PriorityQueue<Argument>(
            new Comparator<Argument>() {
                @Override
                public int compare(Argument s1, Argument s2) {
                    return s1.distence - s2.distence;
                }
            }
    );
    PriorityQueue<Argument> objectPriorityQueue = new PriorityQueue<Argument>(
            new Comparator<Argument>() {
                @Override
                public int compare(Argument s1, Argument s2) {
                    return s1.distence - s2.distence;
                }
            }
    );

    public ArrayList<Triple> getTripleList(DependencyTreeCore dtc) {
        tripleList = new ArrayList<Triple>();


        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        queue.add(dtc.root);
        DependencyTreeNode h;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            //String name = h.word.name;
            String posTag = h.posTag;
            if(posTag.equals("predicate")) {
                subjectPriorityQueue.clear();
                objectPriorityQueue.clear();
                tripleList.add(getTripleByRelate(h));
            }

        }

        return tripleList;
    }

    private Triple getTripleByRelate(DependencyTreeNode h) {
        Triple triple;
        Argument rel = new Argument(h.word.name, 0);
        // DFS search subject or object like relations between w and one of its children
        dfsChildren(h, 0);

        // find the parent of the relation root node withs its children
        while (objectPriorityQueue.isEmpty() || subjectPriorityQueue.isEmpty()) {
            if (h.father == null)
                break;
            h.used = true;
            DependencyTreeNode tmp = h;
            h = h.father;
            dfsChildren(h, 0);
            tmp.used = false;
        }

        Argument sub, obj;
        sub = subjectPriorityQueue.peek();
        obj = objectPriorityQueue.peek();
        if (sub == null)
            sub = new Argument("?", 0);
        if (obj == null)
            obj = new Argument("?", 0);
        triple = new Triple(sub, rel, obj);
        return triple;
    }

    private void dfsChildren(DependencyTreeNode h, int distance) {
        distance++;
        for (DependencyTreeNode chd : h.childrenList) {
            if (!chd.used) {
                if (objectLike.contains(chd.dep_father2child)) {
                    objectPriorityQueue.add(new Argument(chd.word.name, distance));
                }
                if (subjectLike.contains(chd.dep_father2child)) {
                    if (!chd.word.name.equals("的"))
                        subjectPriorityQueue.add(new Argument(chd.word.name, distance));
                }
                dfsChildren(chd, distance);
            }
        }
    }
}
