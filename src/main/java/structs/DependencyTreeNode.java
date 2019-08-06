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

    public DependencyTreeNode(Item w, String p)
    {
        this.word = w;
        this.posTag = p;
        this.childrenList = new ArrayList<DependencyTreeNode>();
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