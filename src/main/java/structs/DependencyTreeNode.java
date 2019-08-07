package structs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Author: wangqing31@midea.com
 * Date: 2019-8-6
 * Time: 14:47
 * Description:
 * ReturnValue:
 */

public class DependencyTreeNode {


    public int levelInTree = -1;
    public String dep_father2child;
    public DependencyTreeNode father;
    public ArrayList<DependencyTreeNode> childrenList;
    public String posTag;
    public Item word;
    public boolean used = false;

    public DependencyTreeNode(Item w, String p)
    {
        this.word = w;
        this.posTag = p;
        this.childrenList = new ArrayList<DependencyTreeNode>();
    }

    public DependencyTreeNode clone()
    {
        DependencyTreeNode ret = null;
        try
        {
            ret = (DependencyTreeNode)super.clone();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ret.word = this.word.clone();
        ret.dep_father2child = this.dep_father2child;
        ret.posTag = this.posTag;
        ret.levelInTree = this.levelInTree;
        //ret.childrenList = (ArrayList<DependencyTreeNode>) this.childrenList.clone();
        ret.childrenList = new ArrayList<DependencyTreeNode>();
        for(DependencyTreeNode dtn: this.childrenList)
        {
            ret.childrenList.add(dtn.clone());
        }
        if(this.father != null)
            ret.father = this.father;//这里没有完全深度clone，否则会提示stackOverflow

        return ret;
    }
    public void sortChildrenList () {
        childrenList.trimToSize();
        Collections.sort(childrenList, new DependencyTreeNodeComparator());
    }
}
class DependencyTreeNodeComparator implements Comparator<DependencyTreeNode> {

    public int compare(DependencyTreeNode n1, DependencyTreeNode n2) {
        return n1.word.position - n2.word.position;
    }

}