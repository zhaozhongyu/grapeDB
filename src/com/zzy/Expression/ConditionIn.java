package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.SQLConnection;


import java.util.List;

/**
 * 处理表达式In
 * select xxx from table where xxx in (aaa, bbb, ccc);
 */
public class ConditionIn extends Condition
{
    private List<Expression> values; //in 的取值列表
    private Expression column;
    private boolean Not;

    public ConditionIn(Expression column, List<Expression> values, boolean Not){
        this.column = column;
        this.values = values;
        this.Not = Not;
    }

    public String getColumnName() {
        return this.column.getColumnName();
    }

    public List<Expression> getValues() {
        return values;
    }

    public boolean isNot() {
        return Not;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public Value getValue(SQLConnection connection) {
        Value columnValue = column.getValue(connection);
        for(Expression value : values){
            if(columnValue.equals(value)){
                return ValueBoolean.get(true);
            }
        }
        return ValueBoolean.get(false);
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.column.mapColumns(resolver, level);
    }
}
