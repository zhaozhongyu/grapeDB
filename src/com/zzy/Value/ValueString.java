package com.zzy.Value;

public class ValueString extends Value
{
    private String value;

    public ValueString(String str){
        this.value = str;
    }
    @Override
    public int getType()
    {
        return this.STRING;
    }

    @Override
    public String getValue(){
        return this.value;
    }

    @Override public int compareTo(Object o)
    {
        return this.getValue().compareTo(((ValueString)o).getValue());
    }

    public String toString(){
        return this.getValue();
    }

    @Override
    public boolean equals(Object vs){
        return this.getValue().equals(((ValueString)vs).getValue());
    }

    @Override public int hashCode() { return this.getValue().hashCode(); }
}
