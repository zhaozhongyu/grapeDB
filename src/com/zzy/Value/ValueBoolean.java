package com.zzy.Value;

/**
 *
 */
public class ValueBoolean extends Value
{
    private static boolean value;
    private static final ValueBoolean valueTrue = new ValueBoolean(true);
    private static final ValueBoolean valueFalse = new ValueBoolean(false);

    private ValueBoolean(boolean value){this.value = value;}

    public static ValueBoolean get(boolean value){return value?valueTrue:valueFalse;};

    @Override public int getType()
    {
        return this.BOOLEAN;
    }

    @Override
    public Boolean getValue(){
        return this.value;
    }

    @Override public int compareTo(Object o)
    {
        return 0;
    }

    public String toString(){
        return this.getValue().toString();
    }

    @Override public int hashCode() { return this.getValue().hashCode(); }
}
