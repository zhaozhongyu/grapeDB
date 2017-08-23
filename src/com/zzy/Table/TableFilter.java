package com.zzy.Table;

import com.zzy.Expression.Condition;
import com.zzy.Expression.ConditionAndOr;
import com.zzy.Expression.Expression;
import com.zzy.Index.Index;
import com.zzy.Index.IndexCursor;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;
import com.zzy.parser.dml.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * 每次查找, 在from阶段生成一个TableFilter对象, TableFilter对象根据index条件查找
 */
public class TableFilter implements ColumnResolver
{
    private static final int BEFORE_FIRST = 0, FOUND = 1, AFTER_LAST = 2, NULL_ROW = 3;

    private Table table ;
    private List<Expression> filterCondition;
    private Expression joinCondition;
    private TableFilter join;
    private Row currentRow;
    private int state;
    private String alias;
    private int scanCount = 0;
    private boolean foundOne;
    private Select select;
    private List<IndexCursor> cursors;
    private IndexCursor currentCursor;
    private SQLConnection connection;


    public TableFilter(SQLConnection connection, Table table, String alias, Select select) {
        this.connection = connection;
        this.table = table;
        this.alias = alias;
        this.select = select;
    }

    public void init(){
        currentCursor = null;
        cursors = null;
        filterCondition = new ArrayList<>();
    }

    /**
     * 将条件式分发给IndexCursor
     * */
    public void addFilterCondition(Expression condition, boolean isJoin) {
        /*if (isJoin) {
            if (joinCondition == null) {
                joinCondition = condition;
            } else {
                joinCondition = new ConditionAndOr(ConditionAndOr.AND, joinCondition, condition);
            }
        } else {
            if (filterCondition == null) {
                filterCondition = condition;
            } else {
                filterCondition = new ConditionAndOr(ConditionAndOr.AND, filterCondition, condition);
            }
        }*/
        filterCondition.add(condition);
        if(cursors == null) {
            cursors = new ArrayList<>();
            for(Index index : table.indexMap.values()){
                IndexCursor cursor = new IndexCursor(connection,this, index);
                cursors.add(cursor);
            }
        }
        for(IndexCursor cursor : cursors){
            if(cursor.addFilterCondition(condition)){
                currentCursor = cursor;
            }
        }

    }

    @Override
    public Column[] getColumns() {
        return table.getColumns();
    }

    /**
     * 根据当前TableFilter的条件返回一个Value
     * */
    @Override
    public Value getValue(Column column) {
        int columnId = column.getColumnid();
        return currentRow.getValue(columnId);
    }

    public boolean next() {
        if(currentCursor == null) {
            this.currentCursor = new IndexCursor(connection,this, table.defaultIndex); //走到这里说明没有index条件, 此时则选择遍历
        }
        boolean result = currentCursor.next();
        currentRow = currentCursor.get();
        return result;
    }

    public void reset() {
        this.currentCursor.reset();
    }

    @Override
    public TableFilter getTableFilter() {
        return this;
    }

    @Override
    public String getTableAlias() {
        return this.alias;
    }


    public Table getTable() {
        return table;
    }
}
