package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.Value.ValueNull;
import com.zzy.engine.SQLConnection;


import java.util.List;

/**
 * xxx is null
 * xxx is not null
 */
public class ConditionIs extends Condition{
    private Expression column;
    private boolean Not; //是否有not关键字

    public ConditionIs(Expression column, boolean Not){
        this.column = column;
        this.Not = Not;
    }

    public String getColumnName() {
        return this.column.getColumnName();
    }

    @Override
    public int getType() {
        return this.Is;
    }

    @Override
    public Value getValue(SQLConnection connection) {
        if(!Not){
            return ValueBoolean.get(column.getValue(connection).equals(ValueNull.INSTANCE()));
        }
        return ValueBoolean.get(! column.getValue(connection).equals(ValueNull.INSTANCE()));

    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.column.mapColumns(resolver, level);
    }


}
