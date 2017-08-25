package com.zzy.Expression;

import com.zzy.Table.Column;
import com.zzy.Table.ColumnResolver;
import com.zzy.Table.Row;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.Value.ValueNull;
import com.zzy.engine.SQLConnection;


import java.util.List;

/**
 *  id = 0, id < 0, id > 0  等等
 * */

public class ConditionArithmetic extends Condition
{
    private String compareType;
    private String columnName; // 当前条件式的左边columnName
    private String alias;
    private String Symbol;
    private Expression value;
    private Expression column;

    public ConditionArithmetic(Expression column,  String Symbol, Expression value){
        this.Symbol = Symbol;
        this.column = column;
        this.value = value;
        this.columnName = column.getColumnName();
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getSymbol() {
        return Symbol;
    }

    public Expression getValue() {
        return value;
    }

    public Expression getColumn() {
        return column;
    }


    @Override public String getTableAlias() { return column.getTableAlias(); }

    @Override
    public int getType() {
        return this.Arithmetic;
    }

    @Override
    public Value getValue(SQLConnection connection) {
        switch (Symbol){
            case "=":
                return ValueBoolean.get(column.getValue(connection).equals(value.getValue(connection)));

            case ">":
                return ValueBoolean.get(column.getValue(connection).compareTo(value.getValue(connection)) > 0);

            case ">=":
                return ValueBoolean.get(column.getValue(connection).compareTo(value.getValue(connection)) >= 0);

            case "<":
                return ValueBoolean.get(column.getValue(connection).compareTo(value.getValue(connection)) < 0);

            case "<=":
                return ValueBoolean.get(column.getValue(connection).compareTo(value.getValue(connection)) <= 0);

            case "!=":
                return ValueBoolean.get(column.getValue(connection).compareTo(value.getValue(connection)) != 0);

        }

        return ValueNull.INSTANCE();
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.column.mapColumns(resolver, level);
        this.value.mapColumns(resolver, level);
    }
}
