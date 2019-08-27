package model;

import structs.Item;
import structs.ItemAndScore;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 10:07 AM
 * Description:
 * ReturnValue:
 **/

public class ParaphraseModel {
    public ArrayList<String> typeMedicineList = new ArrayList<String>();
    public HashMap<String, ArrayList<ItemAndScore> > relationParaphraseDict = new HashMap<String, ArrayList<ItemAndScore> >();
    public HashMap<String, ArrayList<ItemAndScore> > typeParaphraseDict = new HashMap<String, ArrayList<ItemAndScore> >();
    public HashMap<String, ArrayList<String>> conditionParaphraseDict = new HashMap<String, ArrayList<String>>();

    public ParaphraseModel()
    {
        loadRelationParaphraseDict();
        loadTypeParaphraseDict();
        loadConditionParaphraseDict();

    }

    public void loadRelationParaphraseDict()
    {
        System.out.println("loadRelationParaphraseDict() ...");
        File inputFile = new File("./data/paraphrase/relation_paraphrase_dictionary.txt");//put file path here...
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#")) continue;

                String[] contents = line.split("\t");
                if(contents.length!=3)	//多于两个TAB或少于两个TAB的，直接抛弃。
                    continue;

                String relation = contents[0], paraphrase = contents[1];
                Double score = Double.parseDouble(contents[2]);
                ItemAndScore ias = new ItemAndScore(new Item(relation, Item.ItemType.relation), score);

                if (!relationParaphraseDict.containsKey(paraphrase)) // paraphrase of relation, each paraphrase in relationParaphraseDict has a ArrayList
                    relationParaphraseDict.put(paraphrase, new ArrayList<ItemAndScore>());
                relationParaphraseDict.get(paraphrase).add(ias);

            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // for each ArrayList sort the content in it
        for (String key : relationParaphraseDict.keySet())
        {
            ArrayList<ItemAndScore> al = relationParaphraseDict.get(key);
            Collections.sort(al);
        }
        System.out.println("loadRelationParaphraseDict() done.");
    }

    public void loadTypeParaphraseDict()
    {
        System.out.println("loadTypeParaphraseDict() ...");
        File inputFile = new File("./data/paraphrase/type_paraphrase_dictionary.txt");//put file path here...
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#")) continue;

                String[] contents = line.split("\t");
                String type = contents[0], paraphrase = contents[1];
                Double score = Double.parseDouble(contents[2]);
                ItemAndScore ias = new ItemAndScore(new Item(type, Item.ItemType.entity), score);

                if (!typeParaphraseDict.containsKey(paraphrase))
                    typeParaphraseDict.put(paraphrase, new ArrayList<ItemAndScore>());
                typeParaphraseDict.get(paraphrase).add(ias);
                //System.out.println(type+" "+paraphrase+" "+score);

            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        for (String key : typeParaphraseDict.keySet())
        {
            ArrayList<ItemAndScore> al = typeParaphraseDict.get(key);
            Collections.sort(al);
        }

        System.out.println("loadTypeParaphraseDict() done.");
    }

    public void loadConditionParaphraseDict() // implicit relation
    {
        System.out.println("loadConditionParaphraseDict() ...");
        File inputFile = new File("./data/paraphrase/condition_paraphrase_dictionary.txt");//put file path here...
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "utf-8"));
            String line;

            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith("#")) continue;

                String[] contents = line.split("\t");
                if(contents.length!=3)	//多于两个TAB或少于两个TAB的，直接抛弃。
                    continue;

                String paraphrase = contents[0], relation = contents[1], condition = contents[2];
                ArrayList<String> tmp = new ArrayList<String>();
                tmp.add(relation);
                tmp.add(condition);

                if (!conditionParaphraseDict.containsKey(paraphrase))
                    conditionParaphraseDict.put(paraphrase, tmp);

            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("loadConditionParaphraseDict() done.");
    }

    public ArrayList<String> getRelatedCondition(String paraphrase)
    {
        if(paraphrase!=null && conditionParaphraseDict.containsKey(paraphrase))
            return conditionParaphraseDict.get(paraphrase);
        else
            return null;
    }

    public ItemAndScore getRelatedType(String paraphrase)
    {
        if (paraphrase!=null &&
                typeParaphraseDict.containsKey(paraphrase))
            return typeParaphraseDict.get(paraphrase).get(0);
        else
            return null;
    }

    public ItemAndScore getRelatedRelation(String paraphrase)
    {
        if (paraphrase!=null &&
                relationParaphraseDict.containsKey(paraphrase))
            return relationParaphraseDict.get(paraphrase).get(0);
        else
            return null;
    }
}
