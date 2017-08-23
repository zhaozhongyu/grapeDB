package com.zzy.Expression;

import com.zzy.Table.Column;
import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;


/**
 * 用于封装column
 * 比如: where name = 'peter' 中的 name
 */
public class ExpressionColumn extends Expression{
    private String columnName;
    private String tableAlias;
    private Column column;
    private ColumnResolver columnResolver;
    private int queryLevel;

    public ExpressionColumn(String tableAlias, String columnName) {
        this.tableAlias = tableAlias;
        this.columnName = columnName;

    }

    public ExpressionColumn(String column) {
        if(column.indexOf(".") != -1){
            String[] strings = column.split("\\.");
            this.tableAlias = strings[0]; //获取alias
            this.columnName = strings[1]; //获取到

        } else {
            this.tableAlias = null; //此时说明不是多表查询
            this.columnName = column;
        }
    }

    public ExpressionColumn(Column column) {
        this.column = column;
        this.columnName = column.getName();
    }

    public boolean isIndex(){
        return column.isPrimaryKey();
    }

    @Override
    public int getType() {
        return 0;
    }

    /*
    * ExpressionColumn类持有TableFilter对象, TableFilter对象持有IndexCursor对象, IndexCursor对象根据条件获取到row[]
    * */
    @Override
    public Value getValue(SQLConnection connection) {
        if (columnResolver == null) {
            String message = "can't map " + tableAlias + "." + columnName;
            throw new RuntimeException(message);
        }

        return columnResolver.getValue(column);
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.columnResolver = resolver;
        for(Column column1 : columnResolver.getColumns()){
            if(columnName.equals(column1.getName())) {
                column = column1;
            }
        }
    }

    public String getColumnName() {
        return this.columnName;
    }

    public Column getColumn() {return this.column;}

    @Override
    public boolean isWildcard() {
        return columnName.equals("*");
    }

    @Override public String getTableAlias() {return tableAlias;}
}
