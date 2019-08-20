package nlp;

import lcn.EntitySearcher;
import model.ParaphraseModel;
import org.fnlp.nlp.parser.dep.DependencyTree;
import structs.DependencyTreeNode;
import structs.Item;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/27/2019
 * Time: 9:43 AM
 * Description: convert the generated dependency tree using fudanNlp to the tree we want
 * ReturnValue:
 **/

public class DependencyTreeCore {
    public DependencyTreeNode root;
    public DependencyTree odt;
    public ArrayList<DependencyTreeNode> nodesList;

    public DependencyTreeCore clone()
    {
        DependencyTreeCore ret = null;
        try
        {
            ret = (DependencyTreeCore)super.clone();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //ret.nodesList = (ArrayList<DependencyTreeNode>) this.nodesList.clone();
        ret.nodesList = new ArrayList<DependencyTreeNode>();
        if(this.nodesList != null)
            for(DependencyTreeNode dtn: this.nodesList)
                ret.nodesList.add(dtn.clone());

        if(this.root != null)
            ret.root = this.root.clone();

        return ret;
    }

    public DependencyTreeCore (String sentence, NlpTool nlptool, ParaphraseModel pm, OutputStreamWriter writer)
    {
        odt = nlptool.getOrignalDSTree(sentence);
        if(odt == null)
        {
            //System.out.println("Error: query is empty.");
            return;
        }

        nodesList = new ArrayList<DependencyTreeNode>();

        String[] dsWordArray = odt.toString().split("\n");
//        0 可以 情态词 3 的字结构
//        1 治疗 动词 0 补语
//        2 感冒 名词 1 宾语
//        3 的 结构助词 5 的字结构
//        4 药 名词 5 的字结构
//        5 的 结构助词 7 状语
//        6 成分 名词 7 主语
//        7 是 动词 -1 核心词
//        8 什么 限定词 9 定语
//        9 ？ 名词 7 宾语

        // generate nodes, add nodes to nodeList
        for(String row: dsWordArray)
        {
            String[] tmpArray = row.split(" ");
            int id = Integer.parseInt(tmpArray[0]);
            String name = tmpArray[1];
            String posTag = tmpArray[2];
            Item word = new Item(name,null);
            word.position = id;
            DependencyTreeNode newNode = new DependencyTreeNode(word,posTag);
            nodesList.add(newNode);
            if(tmpArray[3].equals("-1"))
            {
                this.root = newNode;
                newNode.levelInTree = 0;
                //System.out.println("this root: "+this.root.levelInTree + " " + this.root.posTag);
            }
        }
        // add edges
        for(String row: dsWordArray)
        {
            String[] tmpArray = row.split(" ");
            int id = Integer.parseInt(tmpArray[0]);
            int fid = Integer.parseInt(tmpArray[3]);
            String e = tmpArray[4];
            DependencyTreeNode child = nodesList.get(id);
            if(fid == -1)
            {
                child.dep_father2child = e;
                // e father -> child
            }
            else
            {
                DependencyTreeNode father = nodesList.get(fid);
                child.father = father;
                father.childrenList.add(child);
                child.dep_father2child = e;
            }
        }
        // count level
        Stack<DependencyTreeNode> stack = new Stack<DependencyTreeNode>();
        stack.push(this.root);
        while (!stack.empty())
        {
            DependencyTreeNode dtn = stack.pop();
            if (dtn.father != null)
            {
                dtn.levelInTree = dtn.father.levelInTree + 1;
                dtn.sortChildrenList();
            }
            for (DependencyTreeNode chd : dtn.childrenList)
            {
                stack.push(chd);
            }
        }

        bfsDependencyTree(pm);
        
    }

    //BFS a dependency tree and find "entity"/"type"/"predicate"
    public void bfsDependencyTree(ParaphraseModel pm)
    {
        EntitySearcher es = new EntitySearcher();
        ArrayList<DependencyTreeNode> res = new ArrayList<DependencyTreeNode>();
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(this.root == null)
            return;
        queue.add(this.root);
        DependencyTreeNode h,p;
        while(queue.peek()!=null)
        {
            h = queue.poll();

            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            /*
             * 找出显式谓词, based on paraphrase_relation
             * */
            if(pm.getRelatedRelation(h.word.name) != null)
            {
                h.posTag = "predicate";
                res.add(h);
            }
            //找出命名实体 ???，Lucene inversed index based index already build
            String tmp = es.searchEntityAndType(h.word.name);
            if(tmp != null)
            {
                int st = tmp.indexOf("-");

                if(tmp.substring(st).equals("-药品"))
                    h.posTag = "entity-medicine";
                String sss = tmp.substring(st);
                String bbb = "症状";
                Boolean aaa = sss.equals(bbb);
                if(tmp.substring(st).equals("-症状"))
                    h.posTag = "entity-illness";
            }
            //找出type ???, based on type_paraphrase
            if(pm.getRelatedType(h.word.name) != null)
            {
                h.posTag = "type-medicine";

            }

            System.out.print("level:"+ h.levelInTree+" name:"+ h.word.name+" posTag:"+ h.posTag+" id:"+ h.word.position+" relation:"+ h.dep_father2child+" father:");
            String fa = null;
            if(h.father != null)
                fa = h.father.word.name;
            System.out.println(fa);
        }
    }
}
