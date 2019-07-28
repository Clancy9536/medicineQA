package structs;

/**
 * Author: wangqing9536@gmail.com
 * Date: 7/28/2019
 * Time: 10:28 AM
 * Description:
 * ReturnValue:
 **/

public class Item {
    public String name;
    public enum ItemType{entity,literal,relation,variable};
    public ItemType type;
    public int position = -1;

    public Item clone()
    {
        Item item = null;
        try
        {
            item = (Item)super.clone();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return item;
    }

    public Item(String _name, ItemType _type)
    {
        name = _name;
        type = _type;
    }

    public String toString()
    {
        if (type == ItemType.entity || type == ItemType.relation)
            return "<"+name+">";
        else if (type == ItemType.literal)
            return "\""+name+"\"";
        else if (type == ItemType.variable)
            return name;
        else
            return name;		//这里应该返回null，为调试方便先改为name
    }

    public boolean templateEquals(Object o)
    {
        if (!(o instanceof Item)) // judge a object whether is a Item
            return false;
        Item item = (Item)o;
        return  this.type==item.type;
    }



}
