package com.zzy.parser.dml;

import com.zzy.Expression.Expression;
import com.zzy.Index.Index;
import com.zzy.Table.Column;
import com.zzy.Table.Table;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;
import com.zzy.engine.SQLResultSet;
import com.zzy.engine.SQLStatement;

import java.util.ArrayList;

/**
 * INSERT INTO 表名称 VALUES (值1, 值2,....)
 * INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
 */
public class Insert extends Query
{
    private Column[] columns;
    private Value[] values;
    boolean noColumns; //全量插入
    private Table table;
    private SQLConnection connection;


    public Insert(SQLConnection connection){
        super(connection);
        this.connection = connection;
    }

    @Override
    public void init() {

    }

    @Override
    protected SQLResultSet executeImpl(SQLStatement statement) {
        if(noColumns){
            table.insert(values);
        }
        else  {
            table.insert(values, columns);
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public ArrayList<Expression> getExpressions() {
        return null;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setNoColumns(boolean noColumns) {
        this.noColumns = noColumns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
    }

    public void setValues(Value[] values) {
        this.values = values;
    }
}
