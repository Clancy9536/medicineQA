package structs;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 10:26 AM
 * Description:
 * ReturnValue:
 **/

public class ItemAndScore implements Comparable<ItemAndScore> {
    public Item item;
    public double score;

    public ItemAndScore(Item i, double s) {
        item = i;
        score = s;
    }
    public int compareTo(ItemAndScore o)
    {
        if (this.score > o.score)
            return -1;
        else if (this.score < o.score)
            return 1;
        return 0;
    }
}
