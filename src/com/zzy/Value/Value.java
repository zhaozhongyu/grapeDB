package com.zzy.Value;

public abstract class Value implements Comparable
{
    public static final int UNKNOWN = -1;
    public static final int NULL = 0;
    public static final int BOOLEAN = 1;
    public static final int BYTE = 2;
    public static final int INT = 3;
    public static final int FLOAT = 4;
    public static final int BYTES = 5;
    public static final int STRING = 6;
    public static final int BLOB = 7;
    public static final int ARRAY = 8;
    public static final int TIME = 9;
    public static final int DATE = 10;


    public abstract int getType();

    /**
     * 用于作为b+树排序依据
     * @param v
     * @return
     */
   /* public final int compareTo(Value v) {
        if (this == v) {
            return 0;
        }
        if (this.getType() != v.getType()) {
            // 对int 和 float的处理
            if (this.getType() == Value.INT && v.getType() == Value.FLOAT) {
                if(((ValueFloat)v).getValue() - ((ValueInt)this).getValue() == 0){
                    return 0;
                }
                return ((ValueFloat)v).getValue() - ((ValueInt)this).getValue() > 0? -1:1;
            }
            if (this.getType() == Value.FLOAT && v.getType() == Value.INT) {
                if(((ValueFloat)this).getValue() - ((ValueInt)v).getValue() == 0){
                    return 0;
                }
                return ((ValueFloat)this).getValue() - ((ValueInt)v).getValue() > 0?1:-1;
            }
            throw new RuntimeException("Can't compare two different type!");
        }
        return this.getValue().compareTo(v.getValue());
    }*/



    public Comparable getValue() { //不作为抽象方法是由于像Null这种是没办法返回一个Comparable值的
        return null;
    }


}
