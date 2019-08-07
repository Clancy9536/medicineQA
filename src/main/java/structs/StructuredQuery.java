package structs;

import java.util.ArrayList;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 3:37 PM
 * Description:
 * ReturnValue:
 **/

public class StructuredQuery {
    public ArrayList<Triple> tripleList = new ArrayList<Triple>();
    public int tripleNum = 0;
    public double score = 0.0;

    public void addTriple(Triple triple)
    {
        tripleList.add(triple);
        tripleNum++;
        score+=triple.score;
    }
}
