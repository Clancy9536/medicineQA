package structs;

/**
 * Author: wangqing9536@gmail.com
 * Date: 8/26/2019
 * Time: 4:52 PM
 * Description:
 * ReturnValue:
 **/

public class Argument {
    public String name;
    public int distence;

    public Argument(String name, int distence) {
        this.distence = distence;
        this.name = name;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
