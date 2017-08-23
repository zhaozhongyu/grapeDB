package com.zzy.Expression;


/**
 <Condition> := <Condition> And <Condition> //顶级元素
 <Condition> := <Condition> Or <Condition> //顶级元素
 <Condition> := <Attribute> in <Query> //二级元素
 <Condition> := <Attribute> Between <Attribute> and <Attribute> //二级元素
 <Condition> := <Attribute> {=|<=|<|>=|>|!=} <Attribute>  //最小元素
 <Condition> := <Attribute> like <Pattern> //最小元素
 <Condition> := <Attribute> IS [NOT] Null; //最小元素
 * */
public abstract class Condition extends Expression implements Comparable
{
    public static int Arithmetic = 0,Between = 1, Like = 2, Is = 4, In = 5;

    public abstract int getType() ;

    @Override
    public int compareTo(Object o) {
        return this.getType() - ((Condition)o).getType();
    }
}
