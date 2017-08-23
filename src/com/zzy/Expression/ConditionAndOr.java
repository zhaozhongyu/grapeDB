package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.SQLConnection;

/**
 * 处理多个条件式, 所有条件式通过ConditionAndOr组合在一起, Condition
 * 在无括号的情况下, 最终会分解成一连串的ConditionAnd 表达式, 然后求并集
 */
public class ConditionAndOr extends Condition
{
    public static String AND = "AND", OR = "OR";
    private String andOrType ; //记录是And还是Or

    private Expression left, right;


    public ConditionAndOr(String andOrType){ this.andOrType = andOrType; };

    public ConditionAndOr(String andOrType, Expression left, Expression right) {
        this.andOrType = andOrType;
        this.left = left;
        this.right = right;
    }

    public void setLeft(Condition left)
    {
        this.left = left;
    }

    public void setRight(Condition right)
    {
        this.right = right;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public Value getValue(SQLConnection connection) {
        if(this.andOrType.equals(AND)) {
            if(left.getValue(connection).equals(ValueBoolean.get(true)) && right.getValue(connection).equals(ValueBoolean.get(true))){
                return ValueBoolean.get(true);
            }
            return ValueBoolean.get(false);
        } else {
            if(left.getValue(connection).equals(ValueBoolean.get(true)) || right.getValue(connection).equals(ValueBoolean.get(true))){
                return ValueBoolean.get(true);
            }
            return ValueBoolean.get(false);
        }
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.left.mapColumns(resolver, level);
        this.right.mapColumns(resolver, level);
    }

    //如果left 或right其中一个为null 则不合法
    public boolean checkLegal(){
        return this.left == null || this.right == null;
    }

    public String getAndOrType(){
        return this.andOrType;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }
}
