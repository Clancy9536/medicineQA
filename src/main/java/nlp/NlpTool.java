package nlp;

import org.fnlp.ml.types.Dictionary;
import org.fnlp.nlp.cn.tag.POSTagger;
import org.fnlp.nlp.parser.dep.DependencyTree;
import org.fnlp.nlp.parser.dep.JointParser;
import org.fnlp.util.exception.LoadModelException;

import java.io.IOException;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/27/2019
 * TIME: 9:19 AM
 * Description: use fudan nlp tools to parse a nature language
 * ReturnValue:
 **/

public class NlpTool {
    private POSTagger posTagger;
    private JointParser parser;

    public NlpTool() {
        System.out.println(("NlpTool initialize..."));
        try {
            parser = new JointParser("./models/dep.m");
           posTagger = new POSTagger("./models/seg.m", "models/pos.m", new Dictionary("./models/dictCh.txt")); // to parse a sentence
        } catch (LoadModelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("NlpTool initialize done.");
    }

    public DependencyTree getOrignalDSTree(String sentence)
    {
        //sentence = "感冒灵适用于什么病？
        //s[0] = "感冒灵" "适用" "于" "什么" "病" "?"
        //s[1] = "专有名" "动词" "介词" "限定词" "名词" "标点"
        String[][] s = posTagger.tag2Array(sentence);
        DependencyTree tree = null;
        try
        {
            if(s!=null && s[0].length>0)
            {
                //tree:
                //0 感冒灵 专有名 1 主语
                //1 适用 动词 -1 核心词
                //2 于 介词 1 补语
                //3 什么 限定词 4 定语
                //4 病 名词 2 介宾
                //5 ？ 标点 1 标点
                tree = parser.parse2T(s[0],s[1]);
                System.out.println("DependencyTree:\n" + tree.toString());
            }
            else
            {
                System.out.println("Error: query is empty.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tree;
    }

}
