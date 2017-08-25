package com.zzy.parser.dml;

import com.zzy.Expression.*;
import com.zzy.Table.Column;
import com.zzy.Table.Row;
import com.zzy.Table.Table;
import com.zzy.Table.TableFilter;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.SQLConnection;
import com.zzy.engine.SQLResultSet;
import com.zzy.engine.SQLStatement;
import com.zzy.parser.Prepared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *UPDATE 表名称 SET 列名称 = 新值 WHERE 列名称 = 某值
 */
public class Update extends Prepared
{
    private TableFilter from;
    private HashMap<Column, Value> set; //
    private boolean Unique = false;
    private Expression where;
    private ArrayList<Expression> andConditions;

    public Update(SQLConnection connection){
        super(connection);
    }

    @Override
    public SQLResultSet execute(SQLStatement statement) {
        ArrayList<Row> rows = new ArrayList<>(); //用于存放所有满足where子句条件的row
        if(where != null) { where.mapColumns(from, 0); }
        for(Column column : set.keySet()){
            if( column.isUnique() ){
                Unique = true; //如果待set的字段里有Unique的column, 则此时校验查询到的result行数, 超过一行时报错
            }
        }
        //先取出所有符合条件的row
        if (andConditions != null && andConditions.size() > 0) {

            for (Expression expression : andConditions) { //每个and条件式单独执行一次查询
                from.init();//每次进入and表达式时, 都清空一次之前的and表达式记录
                TableFilter tableFilter = from;
                while (expression instanceof ConditionAndOr) { //根据and条件式左右两边, 判断是否有index条件, 如果有的话, 则指定到cursor上
                    Expression right = ((ConditionAndOr) expression).getRight();
                    expression = ((ConditionAndOr) expression).getLeft();
                    if (tableFilter.getTable().getColumn(right.getColumnName()).isPrimaryKey() && !(expression instanceof ConditionLike)) {
                        //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                        tableFilter.addFilterCondition(right, false);
                        break;
                    }
                }
                if (tableFilter.getTable().getColumn(expression.getColumnName()).isPrimaryKey() && !(expression instanceof ConditionLike)) {
                    //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                    tableFilter.addFilterCondition(expression, false);
                }

                while (tableFilter.next()) { //next方法移动游标并返回是否还有值
                    if (where.getValue(connection).equals(ValueBoolean.get(true))) { //如果当前的where 表达式为true
                        rows.add(tableFilter.getCurrentRow());
                    }
                }
            }
        } else { //倘若没有where子句, 直接遍历
            TableFilter tableFilter = from;
            while(tableFilter.next()){
                rows.add(tableFilter.getCurrentRow());
            }
        }
        if(Unique && rows.size() > 1){ //如果存在unique列并且符合条件的row数量大于1
            throw new RuntimeException("Found multi rows to set and some column is unique.");
        }
        Table table = from.getTable();
        for(Row row : rows){
            Value[] values = new Value[row.length()];
            for(int i = 0; i < row.length(); i++){
                values[i] = row.getValue(i);
            }
            for(Map.Entry<Column, Value> entry : set.entrySet()){
                values[entry.getKey().getColumnid()] = entry.getValue(); //将要set的value都替换掉
            }
            Row nrow = new Row(values);
            table.update(row, nrow);
        }
        return null;
    }



    public void setFrom(TableFilter from) {
        this.from = from;
    }

    public void setSet(HashMap set) {
        this.set = set;
    }

    public void setWhere(Expression where) {
        this.where = where;
        Expression temp = where;
        andConditions = new ArrayList<>();
        while(temp instanceof ConditionAndOr && ((ConditionAndOr)temp).getAndOrType().equals(ConditionAndOr.OR)){ //如果当前的值是一个OR条件式
            andConditions.add(((ConditionAndOr) temp).getRight());
            temp = ((ConditionAndOr) temp).getLeft();
        }
        andConditions.add(temp);
    }
}
