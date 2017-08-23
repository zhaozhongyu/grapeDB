package com.zzy.parser.ddl;

import com.zzy.Schema.Schema;
import com.zzy.Table.Column;
import com.zzy.Table.Table;
import com.zzy.engine.SQLConnection;
import com.zzy.engine.SQLResultSet;
import com.zzy.engine.SQLStatement;
import com.zzy.parser.Prepared;

import java.sql.ResultSet;
import java.util.LinkedList;

/**
 * 通过这个类来创建table
 */
public class CreateTable extends Prepared
{
    private String tableName;
    public LinkedList<Column> columns ;
    public Column primarykey;
    private Schema schema;

    public CreateTable(SQLConnection connection, Schema schema){
        super(connection);
        columns = new LinkedList<>();
        this.schema = schema;
    }

    @Override
    public SQLResultSet execute(SQLStatement statement) {
        create();
        return null;
    }

    public void addColumn(Column column){
        this.columns.add(column);
    }

    public Table create(){
        if(primarykey != null){
            Column[] columnArray = new Column[columns.size()];
            Table table = new Table(tableName, schema, columns.toArray(columnArray),primarykey );
            return table;
        }
        Table table = new Table(tableName, schema, (Column[])columns.toArray() );

        return table;
    }



    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public void setPrimarykey(Column primarykey)
    {
        this.primarykey = primarykey;
    }
}
