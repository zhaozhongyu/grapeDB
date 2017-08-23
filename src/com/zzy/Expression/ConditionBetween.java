package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.SQLConnection;


import java.util.List;


/**
 * 处理表达式between
 * between
 */
public class ConditionBetween extends Condition
{
    private Expression left;
    private Expression right;
    private Expression column;
    private boolean Not;
    public ConditionBetween(Expression column,  Expression left, Expression right, boolean Not){
        this.column = column;
        this.left = left;
        this.right = right;
        this.Not = Not;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public boolean isNot() {
        return Not;
    }

    public String getColumnName() {
        return this.column.getColumnName();
    }

    @Override
    public int getType() {
        return this.Between;
    }

    @Override
    public Value getValue(SQLConnection connection) {
        Value columnValue = column.getValue(connection);
        if(columnValue.compareTo(left) >= 0 && columnValue.compareTo(right) <= 0) {
            return ValueBoolean.get(true);
        }
        return ValueBoolean.get(false);
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.column.mapColumns(resolver, level);
    }
}
