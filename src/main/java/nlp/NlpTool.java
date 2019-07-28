package nlp;

import org.fnlp.ml.types.Dictionary;
import org.fnlp.nlp.cn.tag.POSTagger;
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
    private JointParser pasrer;

    public NlpTool() {
        System.out.println(("NlpTool initialize..."));
        try {
           pasrer = new JointParser("./models/dep.m");
           posTagger = new POSTagger("./models/seg.m", "models/pos.m", new Dictionary("./models/dictCh.txt")); // to parse a sentence
        } catch (LoadModelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("NlpTool initialize done.");
    }



}
