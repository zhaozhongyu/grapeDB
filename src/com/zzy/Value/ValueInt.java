package com.zzy.Value;

public class ValueInt extends Value
{
    private static final int STATIC_SIZE = 1000;
    private static final ValueInt[] STATIC_CACHE = new ValueInt[STATIC_SIZE];//创建一个缓存池
    public final int value;

    static {
        for (int i = 0; i < STATIC_SIZE; i++) {
            STATIC_CACHE[i] = new ValueInt(i);
        }
    }

    public ValueInt(int value) {
        this.value = value;
    }

    public static ValueInt get(int i) {
        if (i >= 0 && i < STATIC_SIZE) {
            return STATIC_CACHE[i];
        }
        return new ValueInt(i);
    }

    @Override public int getType()
    {
        return this.INT;
    }

    public Integer getValue(){
        return this.value;
    }

    @Override public int compareTo(Object o)
    {
        return this.value - (Integer)o;
    }

    public boolean equals(Object vs){
        return this.getValue().equals(((ValueInt)vs).getValue());
    }
}
