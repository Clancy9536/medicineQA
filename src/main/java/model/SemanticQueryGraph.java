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
    static public ArrayList<String> predicatesM2I = new ArrayList<String>(Arrays.asList("����","����","����"));

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

        //��������ʹʣ��Ȱ����ͷ�����������
        if(hasType)
        {
            if(typeIllnessList.size()>0)
            {
                hasFind = true;
                DependencyTreeNode tmpRelNode = new DependencyTreeNode(new Item("֢״", Item.ItemType.relation), "predicate");
                directConditionMap.put(tmpRelNode, typeIllnessList);
            }
        }

        //ͨ��ν��������
        if(hasPredicate)
        {
            for(DependencyTreeNode dtn: predicateList)
            {
                DependencyTreeNode p = dtn;

                //pName ��ͨ���ʵ��׼����ν��
                ItemAndScore pItem = pm.getRelatedRelation(p.word.name);
                String pName = pItem.item.name;

                //�ҳ� ν�� ����֮�ڵ��ھӣ����й�ϵ�ľ���������
                //����֮�ڵ����ܹ�ϵ�����ˣ�ֱ�����������ֽṹ����ָͬ��
                //��Ϊ�Զ���tag���������ϵ���ң���ֱ����Ϊ���нڵ㶼��neighbor

                neiborsList = findNeibors(p,3,true);
                //neiborsList = ds.nodesList;

                ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                for(DependencyTreeNode tmp: neiborsList)
                {
//					System.out.println(pName+" neighbors: "+tmp.word.name);

                    //M2P: <?x><predicate><neighbors>
                    //other predicates��<?x><predicate>"neighbors"
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

        //����ʡ��ν�ʵ����ԣ���ԭ��������
        if(hasCondition)
        {
            for(DependencyTreeNode dtn: conditionList)
            {
                ArrayList<String> tmp = pm.getRelatedCondition(dtn.word.name);
                //pName �Ǳ�׼����ν�ʣ�oName �Ǳ�׼���ı���
                String pName = tmp.get(0) , oName = tmp.get(1);

                DependencyTreeNode tmpPreNode = new DependencyTreeNode(new Item(pName, Item.ItemType.relation),"predicate");
                //������ʱ��Ϊ��������literary��
                DependencyTreeNode tmpObjNode = new DependencyTreeNode(new Item(oName, Item.ItemType.literal),"condition");
                ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                attributeList.add(tmpObjNode);
                directConditionMap.put(tmpPreNode, attributeList);
                dtn.used = true;
                hasFind = true;
            }
        }

        //�������ʵ�嵫ʡ����֮���ϵ�����
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
                    //����Ƶ��ѡ���ɫ
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
        //��������ʹʣ��Ȱ����ͷ�����������
        if(hasType)
        {
            if(typeMedicineList.size()>0)
            {
                hasFind = true;
                DependencyTreeNode tmpRelNode = new DependencyTreeNode(new Item("����", Item.ItemType.relation), "predicate");
                directConditionMap.put(tmpRelNode, typeMedicineList);
            }
        }

        //ͨ��ν��������
        if(hasPredicate)
        {
            for(DependencyTreeNode dtn: predicateList)
            {
                DependencyTreeNode p = dtn;

                //pName ��ͨ���ʵ��׼����ν��
                ItemAndScore pItem = pm.getRelatedRelation(p.word.name);
                String pName = pItem.item.name;

                if(predicatesM2I.contains(pName))
                {
                    //ͬʱ������ʵ���ν�ʡ�ҩƷ-������ֱ����Ϊ����ƥ�䡣
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
                    //�ҳ� ν�� ����֮�ڵ��ھӣ����й�ϵ�ľ���������
                    //����֮�ڵ����ܹ�ϵ�����ˣ�ֱ�����������ֽṹ����ָͬ��
                    //��ֱ����Ϊ���нڵ㶼��neighbor

                    //neiborsList = findNeibors(p,2,true);
                    neiborsList = ds.nodesList;

                    ArrayList<DependencyTreeNode> attributeList = new ArrayList<DependencyTreeNode>();
                    for(DependencyTreeNode tmp: neiborsList)
                    {
                        System.out.println(pName+" neighbors: "+ tmp.word.name);
                        //�����ν�ʲ�����M2P������˳��Ϊ��<?x><predicate>"neighbors"
                        //���������obj��entity��ֻ��"<?x> <����> <����>"������������Ԫ����������õ����ʺ���
                        //��ͬ�ڵ�Ӱ��<����>��Ӧ�ںܶ�entity������� <ְҵ>��Ӧ��Ϊliterary��
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

        //����ʡ��ν�ʵ����ԣ���ԭ��������
        if(hasCondition)
        {
            for(DependencyTreeNode dtn: conditionList)
            {
                ArrayList<String> tmp = pm.getRelatedCondition(dtn.word.name);
                //pName �Ǳ�׼����ν�ʣ�oName �Ǳ�׼���ı���
                String pName = tmp.get(0) , oName = tmp.get(1);
                DependencyTreeNode tmpPreNode = new DependencyTreeNode(new Item(pName, Item.ItemType.relation),"predicate");
                //������ʱ��Ϊ��������literary��
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
             * �ҳ����ʹ�
             * �����ʹʷ���typeList
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
             * �ҳ���ʽν�ʣ������ý���ʴʵ�
             * ��ν�ʷ���predicateList
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
             * �ҳ���ʵ���ҩƷʵ��,����ֱ����PosTag�жϣ����ܳ��� ҩ�� ���������ݼ���Ҳ������ ҩƷʵ�� �������
             * ��ʵ��ֱ�Ӽ���entityMedicineList��entityIllnessList
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
             * �ҳ�������Ϊ����������Ĵ�
             * �������ʼ���conditionList
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
                //���ǲ��й�ϵ�����б߾���Ϊ0
                if(considerCoordinative && h.dep_father2child.equals("����"))
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
                    if(considerCoordinative && chd.dep_father2child.equals("����"))
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
                pre = new Item("����", Item.ItemType.relation);
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
                    if(predicatesM2I.contains(pName)||pName.equals("����"))
                        objType = Item.ItemType.entity;

                    for(DependencyTreeNode tmp: attributeList)
                    {
                        sub = new Item("?x", Item.ItemType.variable);
                        pre = new Item(pName, Item.ItemType.relation);
                        obj = new Item(tmp.word.name,objType);

                        if(pName.equals("����"))
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
                //pre = new Item("���",ItemType.relation);
                //obj = new Item("?x",ItemType.variable);

                sub = new Item("?x", Item.ItemType.variable);
                pre = new Item("����", Item.ItemType.relation);
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
