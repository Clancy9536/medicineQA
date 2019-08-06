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
        //sentence = "��ð��������ʲô����
        //s[0] = "��ð��" "����" "��" "ʲô" "��" "?"
        //s[1] = "ר����" "����" "���" "�޶���" "����" "���"
        String[][] s = posTagger.tag2Array(sentence);
        DependencyTree tree = null;
        try
        {
            if(s!=null && s[0].length>0)
            {
                //tree:
                //0 ��ð�� ר���� 1 ����
                //1 ���� ���� -1 ���Ĵ�
                //2 �� ��� 1 ����
                //3 ʲô �޶��� 4 ����
                //4 �� ���� 2 ���
                //5 �� ��� 1 ���
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
