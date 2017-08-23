package com.zzy.Value;

/**
 */
public class ValueNull extends Value
{
    /**
     * The main NULL instance.
     */
    public static final ValueNull INSTANCE = new ValueNull();

    private ValueNull() {
        // don't allow construction
    }

    public static ValueNull INSTANCE() {return INSTANCE;}

    public int getType() {
        return this.NULL;
    }
    public String toString(){
        return "null";
    }

    @Override public int compareTo(Object o)
    {
        return 0;
    }

    @Override public int hashCode() { return this.getValue().hashCode(); }
}
