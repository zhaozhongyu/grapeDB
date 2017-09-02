package com.zzy.Value;

public class ValueDouble extends Value
{
    public final double value;

    public ValueDouble(double i) {this.value = i;}
    @Override
    public int getType()
    {
        return this.Double;
    }




    @Override
    public Double getValue(){
        return this.value;
    }

    public String toString(){
        return this.getValue().toString();
    }

    @Override public int compareTo(Object o)
    {
        return this.getValue().compareTo(((ValueDouble)o).getValue());
    }

    @Override public int hashCode() { return this.getValue().hashCode(); }
}
