package model;

import lcn.FragmentChecker;
import lcn.RelationSearcher;
import nlp.DependencyTreeCore;
import structs.DependencyTreeNode;
import structs.Item;
import structs.ItemAndScore;
import structs.Triple;

import java.util.*;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 3:16 PM
 * Description:
 * ReturnValue:
 **/

public class SemanticQueryGraph {
    public static ParaphraseModel pm;
    QueryClassifyModel.queryType type;
    public DependencyTreeCore ds;
    FragmentChecker fc = new FragmentChecker();
    public ArrayList<Triple> tripleList;

    public ArrayList<DependencyTreeNode> predicateList;
    public ArrayList<DependencyTreeNode> conditionList;
    public ArrayList<DependencyTreeNode> entityMedicineList;
    public ArrayList<DependencyTreeNode> entityIllnessList;
    public ArrayList<DependencyTreeNode> typeMedicineList;
    public ArrayList<DependencyTreeNode> typeIllnessList;
    public Map<DependencyTreeNode,ArrayList<DependencyTreeNode>> directConditionMap;
    public Map<String,ArrayList<String>> indirectConditionMap;

    String errMsg = "";
    boolean ready = false;
    static public ArrayList<String> predicatesM2I = new ArrayList<String>(Arrays.asList("适用","主治","利于"));

    public SemanticQueryGraph(ParaphraseModel pM)
    {
        SemanticQueryGraph.pm = pM;
    }

    public boolean buildSemanticQueryGraph(DependencyTreeCore dtc, QueryClassifyModel.queryType type)
    {
        reload();
        this.type = type;
        this.ds = dtc.clone();

        errMsg = "Only one node.";
        boolean flag = false;
        if(type == QueryClassifyModel.queryType.askAttribute)
            flag = buildAskAttribute();
        if(type == QueryClassifyModel.queryType.askMedicine)
            flag = buildAskMedicine();
        if(type == QueryClassifyModel.queryType.askIllness)
            flag = buildAskIllness();

        if(type == QueryClassifyModel.queryType.unresolved)
            errMsg = "Type is unresolved.";

        if(!flag)
            System.out.println("buildSemanticQueryGraph: "+errMsg);
        if(type != QueryClassifyModel.queryType.unresolved)
            ready = true;

        return ready;
    }

    private boolean buildAskIllness() {
        boolean hasEntity = findEntities();
        boolean hasPredicate = findPredicates();
        boolean hasType = findTypes();
        boolean hasCondition = findConditions();
        ArrayList<DependencyTreeNode> neiborsList = new ArrayList<DependencyTreeNode>();
        boolean hasFind = false;

        //如果有类型词，先把类型放入条件集合
        if(hasType)
        {
            if(typeIllnessList.size()>0)
            {
                hasFind = true;
                DependencyTreeNode tmpRelNode = new DependencyTreeNode(new Item("症状", Item.ItemType.relation), "predicate");
                directConditionMap.put(tmpRelNode, typeIllnessList);
            }
        }

        //通过谓词找属性
        if(hasPredicate)
        {
            for(DependencyTreeNode dtn: predicateList)
            {
                DependencyTreeNode p = dtn;

                //pName 是通过词典标准化的谓词
                ItemAndScore pItem = pm.getRelatedRelation(p.word.name);
                String pName = pItem.item.name;

                //找出 谓词 两步之内的邻居，并列关系的距离算作零
                //两步之内的亲密关系包含了：直接相连、的字结构、共同指向
                //因为自定义tag导致依存关系混乱，先直接认为所有节点都是neighbor

                neiborsList = findNeibors(p,3,true);
                //neiborsList = ds.nodesList;

                ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                for(DependencyTreeNode tmp: neiborsList)
                {
//					System.out.println(pName+" neighbors: "+tmp.word.name);

                    //M2P: <?x><predicate><neighbors>
                    //other predicates：<?x><predicate>"neighbors"
                    Item.ItemType objType = Item.ItemType.literal;
                    if(predicatesM2I.contains(pName))
                        objType = Item.ItemType.entity;

                    Triple t = new Triple(new Item("?x", Item.ItemType.variable),
                            new Item(pName, Item.ItemType.relation),
                            new Item(tmp.word.name,objType));

                    boolean flag = fc.checkTriple(t);
                    if(flag)
                    {
                        System.out.println("check triple ok.");
                        tmp.used = true;
                        attributeList.add(tmp);
                    }
                    else
                        System.out.println("check triple error.");
                }
                if(attributeList.size()>0)
                {
                    hasFind = true;
                    directConditionMap.put(p, attributeList);
                }

            }
        }

        //根据省略谓词的属性，还原完整条件
        if(hasCondition)
        {
            for(DependencyTreeNode dtn: conditionList)
            {
                ArrayList<String> tmp = pm.getRelatedCondition(dtn.word.name);
                //pName 是标准化的谓词，oName 是标准化的宾语
                String pName = tmp.get(0) , oName = tmp.get(1);

                DependencyTreeNode tmpPreNode = new DependencyTreeNode(new Item(pName, Item.ItemType.relation),"predicate");
                //这里暂时认为条件都是literary的
                DependencyTreeNode tmpObjNode = new DependencyTreeNode(new Item(oName, Item.ItemType.literal),"condition");
                ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                attributeList.add(tmpObjNode);
                directConditionMap.put(tmpPreNode, attributeList);
                dtn.used = true;
                hasFind = true;
            }
        }

        //处理包含实体但省略了之间关系的情况
        if(hasEntity && entityIllnessList.size()>0)
        {
            RelationSearcher rs = new RelationSearcher();
            String type = "all";
            if(hasType && typeMedicineList.size()>0)
                type = typeMedicineList.get(0).word.name;
            for(DependencyTreeNode dtn: entityIllnessList)
            {
                if(dtn.used == false)
                {
                    //按照频率选择角色
                    String rel = rs.searchRelation("IN", dtn.word.name, type);
                    if(rel != null)
                    {
                        hasFind = true;
                        DependencyTreeNode tmpRelNode = new DependencyTreeNode(new Item(rel, Item.ItemType.relation), "predicate");
                        ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                        attributeList.add(dtn);
                        directConditionMap.put(tmpRelNode, attributeList);
                    }
                }
            }
        }

        if(!hasFind)
            return false;
        return true;

    }

    private boolean buildAskMedicine(){
        boolean hasEntity = findEntities();
        boolean hasPredicate = findPredicates();
        boolean hasType = findTypes();
        boolean hasCondition = findConditions();
        ArrayList<DependencyTreeNode> neiborsList = new ArrayList<DependencyTreeNode>();
        boolean hasFind = false;
        //如果有类型词，先把类型放入条件集合
        if(hasType)
        {
            if(typeMedicineList.size()>0)
            {
                hasFind = true;
                DependencyTreeNode tmpRelNode = new DependencyTreeNode(new Item("适用", Item.ItemType.relation), "predicate");
                directConditionMap.put(tmpRelNode, typeMedicineList);
            }
        }

        //通过谓词找属性
        if(hasPredicate)
        {
            for(DependencyTreeNode dtn: predicateList)
            {
                DependencyTreeNode p = dtn;

                //pName 是通过词典标准化的谓词
                ItemAndScore pItem = pm.getRelatedRelation(p.word.name);
                String pName = pItem.item.name;

                if(predicatesM2I.contains(pName))
                {
                    //同时包含病实体和谓词【药品-病】，直接认为两者匹配。
                    if(hasEntity && entityIllnessList.size()>0)
                    {
                        hasFind = true;
                        directConditionMap.put(p, entityIllnessList);
                        System.out.println("predicate:"+pName+" movie:"+entityIllnessList.get(0).word.name);
                    }
                    else
                    {
                        System.out.println("predicate[illness-medicine] is recognized to type[medicine].");
                        p.posTag = "type-medicine";
                    }
                }
                else
                {
                    //找出 谓词 两步之内的邻居，并列关系的距离算作零
                    //两步之内的亲密关系包含了：直接相连、的字结构、共同指向
                    //先直接认为所有节点都是neighbor

                    //neiborsList = findNeibors(p,2,true);
                    neiborsList = ds.nodesList;

                    ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                    for(DependencyTreeNode tmp: neiborsList)
                    {
                        System.out.println(pName+" neighbors: "+ tmp.word.name);
                        //这里的谓词不包含M2P，所以顺序为：<?x><predicate>"neighbors"
                        //这里人物的obj是entity的只有"<?x> <类型> <人物>"，但是这条三元组基本不会用到，故忽略
                        //不同于电影的<类型>对应于很多entity，人物的 <职业>对应的为literary。
                        Triple t = new Triple(new Item("?x", Item.ItemType.variable),
                                new Item(pName, Item.ItemType.relation),
                                new Item(tmp.word.name, Item.ItemType.literal));
                        boolean flag = fc.checkTriple(t);
                        if(flag)
                        {
                            System.out.println("check triple ok.");
                            attributeList.add(tmp);
                        }
                        else
                            System.out.println("check triple error.");
                    }
                    if(attributeList.size()>0)
                    {
                        hasFind = true;
                        directConditionMap.put(p, attributeList);
                    }
                }
            }
        }

        //根据省略谓词的属性，还原完整条件
        if(hasCondition)
        {
            for(DependencyTreeNode dtn: conditionList)
            {
                ArrayList<String> tmp = pm.getRelatedCondition(dtn.word.name);
                //pName 是标准化的谓词，oName 是标准化的宾语
                String pName = tmp.get(0) , oName = tmp.get(1);
                DependencyTreeNode tmpPreNode = new DependencyTreeNode(new Item(pName, Item.ItemType.relation),"predicate");
                //这里暂时认为条件都是literary的
                DependencyTreeNode tmpObjNode = new DependencyTreeNode(new Item(oName, Item.ItemType.literal),"condition");
                ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                attributeList.add(tmpObjNode);
                directConditionMap.put(tmpPreNode, attributeList);
                hasFind = true;
            }
        }

        if(!hasFind)
            return false;
        return true;
    }

    private boolean buildAskAttribute()
    {
        boolean hasEntity = findEntities();
        boolean hasPredicate = false;
        if(hasEntity)
        {
            hasPredicate = findPredicates();
            if(!hasPredicate)
                errMsg = "Type is askAttribute, has entities, but no predicates.";
        }
        else
        {
            errMsg = "Type is askAttribute but has no entities.";
        }
        if(hasEntity && hasPredicate)
            return true;
        else
            return false;
    }

    private boolean findTypes()
    {
        typeIllnessList.clear();
        typeMedicineList.clear();
        boolean hasFind = false;
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(ds.root == null)
            return false;
        queue.add(ds.root);
        DependencyTreeNode h;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            /*
             * 找出类型词
             * 将类型词放入typeList
             * */
            if(h.posTag == "type-illness")
            {
                hasFind = true;
                typeIllnessList.add(h);
            }
            if(h.posTag == "type-medicine")
            {
                hasFind = true;
                typeMedicineList.add(h);
            }
        }
        return hasFind;
    }

    private boolean findPredicates()
    {
        boolean hasFind = false;
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(ds.root == null)
            return false;
        queue.add(ds.root);
        DependencyTreeNode h;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            /*
             * 找出显式谓词，这里用近义词词典
             * 将谓词放入predicateList
             * */
            if(pm.getRelatedRelation(h.word.name) != null)
            {
                hasFind = true;
                predicateList.add(h);
            }
        }
        return hasFind;
    }

    private boolean findEntities()
    {
        boolean hasFind = false;
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(ds.root == null)
            return false;
        queue.add(ds.root);
        DependencyTreeNode h;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            /*
             * 找出病实体和药品实体,这里直接用PosTag判断，可能出现 药名 并不在数据集中也会认作 药品实体 的情况。
             * 将实体直接加入entityMedicineList和entityIllnessList
             * */
            if(h.posTag.equals("entity-medicine"))
            {
                hasFind = true;
                entityMedicineList.add(h);
            }
            if(h.posTag.equals("entity-illness"))
            {
                hasFind = true;
                entityIllnessList.add(h);
            }
        }
        return hasFind;
    }

    private boolean findConditions()
    {
        boolean hasFind = false;
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        if(ds.root == null)
            return false;
        queue.add(ds.root);
        DependencyTreeNode h;
        while(queue.peek()!=null)
        {
            h = queue.poll();
            //System.out.print("level:"+h.levelInTree+" name:"+h.word.name+" posTag:"+h.posTag+" id:"+h.word.position+" relation:"+h.dep_father2child+" father:");
            for(DependencyTreeNode chd: h.childrenList)
            {
                queue.add(chd);
            }
            /*
             * 找出可以作为宾语（条件）的词
             * 将条件词加入conditionList
             * */
            String name = h.word.name;
            if( pm.conditionParaphraseDict.get(name)!=null )
            {
                hasFind = true;
                conditionList.add(h);
            }

        }
        return hasFind;
    }

    private ArrayList<DependencyTreeNode> findNeibors(DependencyTreeNode r,int maxDep, boolean considerCoordinative)
    {
        if(r == null)
            return null;

        ArrayList<DependencyTreeNode> ret = new ArrayList<DependencyTreeNode>();
        Queue<DependencyTreeNode> queue = new LinkedList<DependencyTreeNode>();
        Map<DependencyTreeNode,Integer> dep = new HashMap<DependencyTreeNode,Integer>();
        Map<DependencyTreeNode,DependencyTreeNode> from = new HashMap<DependencyTreeNode,DependencyTreeNode>();
        DependencyTreeNode h;

        queue.add(r);
        from.put(r, null);
        dep.put(r, 0);

        while(queue.peek()!=null)
        {
            h = queue.poll();
            int hDep = dep.get(h), curDep;
            if(h.father != null && !dep.containsKey(h.father) && !h.father.equals(from.get(h)))
            {
                //考虑并列关系，并列边距离为0
                if(considerCoordinative && h.dep_father2child.equals("并列"))
                    curDep = hDep;
                else
                    curDep = hDep + 1;
                if(curDep <= maxDep)
                {
                    queue.add(h.father);
                    dep.put(h.father, curDep);
                    from.put(h.father, h);
                }
            }
            for(DependencyTreeNode chd: h.childrenList)
            {
                if(!dep.containsKey(chd) && !chd.equals(from.get(h)))
                {
                    if(considerCoordinative && chd.dep_father2child.equals("并列"))
                        curDep = hDep;
                    else
                        curDep = hDep + 1;
                    if(curDep <= maxDep)
                    {
                        queue.add(chd);
                        dep.put(chd, curDep);
                        from.put(chd, h);
                    }
                }
            }
            ret.add(h);
        }
//		System.out.println("ret size: "+ret.size());
        return ret;
    }

    public void reload()
    {
        predicateList = new ArrayList<DependencyTreeNode>();
        conditionList = new ArrayList<DependencyTreeNode>();
        entityMedicineList = new ArrayList<DependencyTreeNode>();
        entityIllnessList = new ArrayList<DependencyTreeNode>();
        typeMedicineList = new ArrayList<DependencyTreeNode>();
        typeIllnessList = new ArrayList<DependencyTreeNode>();
        // map initial
        directConditionMap = new HashMap<DependencyTreeNode,ArrayList<DependencyTreeNode>>();
        indirectConditionMap = new HashMap<String,ArrayList<String>>();
    }

    public ArrayList<Triple> getTripleList()
    {
        tripleList = new ArrayList<Triple>();
        Triple triple;
        if(!ready)
        {
            errMsg = "SQG has not been built.";
            System.out.println("getSPARQL: "+errMsg);
            return null;
        }
        else if(type == QueryClassifyModel.queryType.askAttribute)
        {
            Item sub,pre,obj;
            ArrayList<String> entityStringList = new ArrayList<String>();
            ArrayList<String> predicateStringList = new ArrayList<String>();
            if(entityIllnessList!=null && entityIllnessList.size()>0)
            {
                entityStringList.add(entityIllnessList.get(0).word.name);
            }
            if(entityMedicineList!=null && entityMedicineList.size()>0)
            {
                entityStringList.add(entityMedicineList.get(0).word.name);
            }
            if(predicateList!=null && predicateList.size()>0)
            {
                predicateStringList.add(predicateList.get(0).word.name);
            }
            String relation = pm.getRelatedRelation(predicateStringList.get(0)).item.name;
            System.out.println("predicate:"+ predicateStringList.get(0)+"mapping relation:"+ relation);

            sub = new Item(entityStringList.get(0), Item.ItemType.entity);
            pre = new Item(relation, Item.ItemType.relation);
            obj = new Item("?x", Item.ItemType.variable);

            triple = new Triple(sub,pre,obj);
            tripleList.add(triple);

            System.out.println("Find triple: "+triple.toString());
        }

        else if(type == QueryClassifyModel.queryType.askIllness) {
            Item sub,pre,obj;
            ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
            ArrayList<String> predicateStringList = new ArrayList<String>();


            if(entityIllnessList.size()>0)
            {
                sub = new Item("?x", Item.ItemType.variable);
                pre = new Item("适用", Item.ItemType.relation);
                obj = new Item(entityIllnessList.get(0).word.name, Item.ItemType.literal);

                triple = new Triple(sub,pre,obj);
                tripleList.add(triple);
            }
            if(directConditionMap.size()>0)
            {
                for(DependencyTreeNode dtn: directConditionMap.keySet())
                {
                    attributeList = directConditionMap.get(dtn);
                    ItemAndScore pItem = pm.getRelatedRelation(dtn.word.name);
                    String pName = pItem.item.name;

                    Item.ItemType objType = Item.ItemType.literal;
                    if(predicatesM2I.contains(pName)||pName.equals("适用"))
                        objType = Item.ItemType.entity;

                    for(DependencyTreeNode tmp: attributeList)
                    {
                        sub = new Item("?x", Item.ItemType.variable);
                        pre = new Item(pName, Item.ItemType.relation);
                        obj = new Item(tmp.word.name,objType);

                        if(pName.equals("适用"))
                        {
                            String objStr = pm.getRelatedType(tmp.word.name).item.name;
                            obj = new Item(objStr,objType);
                        }

                        triple = new Triple(sub,pre,obj);
                        tripleList.add(triple);

                        System.out.println("Find triple: "+triple.toString());
                    }
                }
            }
            else
            {
                System.out.println("getTripleList: "+"askIllness triple is null.");
            }
        }

        else if(type == QueryClassifyModel.queryType.askMedicine)
        {
            Item sub,pre,obj;
            ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
            ArrayList<String> predicateStringList = new ArrayList<String>();
            if(entityMedicineList.size()>0)
            {
                //sub = new Item(entityStringList.get(0),ItemType.entity);
                //pre = new Item("简介",ItemType.relation);
                //obj = new Item("?x",ItemType.variable);

                sub = new Item("?x", Item.ItemType.variable);
                pre = new Item("姓名", Item.ItemType.relation);
                obj = new Item(entityMedicineList.get(0).word.name, Item.ItemType.literal);

                triple = new Triple(sub,pre,obj);
                tripleList.add(triple);
            }
            if(directConditionMap.size()>0)
            {
                for(DependencyTreeNode dtn: directConditionMap.keySet())
                {
                    attributeList = directConditionMap.get(dtn);
                    ItemAndScore pItem = pm.getRelatedRelation(dtn.word.name);
                    String pName = pItem.item.name;
                    if(predicatesM2I.contains(pName))
                    {
                        for(DependencyTreeNode tmp: attributeList)
                        {
                            sub = new Item(tmp.word.name, Item.ItemType.entity);
                            pre = new Item(pName, Item.ItemType.relation);
                            obj = new Item("?x", Item.ItemType.variable);
                            triple = new Triple(sub,pre,obj);
                            tripleList.add(triple);

                            System.out.println("Find triple: "+triple.toString());
                        }
                    }
                    else
                    {
                        for(DependencyTreeNode tmp: attributeList)
                        {
                            sub = new Item("?x", Item.ItemType.variable);
                            pre = new Item(pName, Item.ItemType.relation);
                            obj = new Item(tmp.word.name, Item.ItemType.literal);
                            triple = new Triple(sub,pre,obj);
                            tripleList.add(triple);

                            System.out.println("Find triple: "+triple.toString());
                        }
                    }
                }
            }
            else
            {
                System.out.println("getTripleList: "+"askPeople triple is null.");
            }
        }

        return tripleList;
    }
}
