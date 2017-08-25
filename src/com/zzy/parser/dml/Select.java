package com.zzy.parser.dml;

import com.zzy.Expression.*;
import com.zzy.Index.Index;
import com.zzy.Index.IndexCursor;
import com.zzy.Table.Column;
import com.zzy.Table.Row;
import com.zzy.Table.Table;
import com.zzy.Table.TableFilter;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.*;
import com.zzy.result.LocalResult;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/**
 * select实现类
 */
public class Select extends Query
{

    private List<Expression> select; //select a,b,c from中的a,b,c
    private List<TableFilter> from;
    private Expression where;
    private List<Expression> andConditions; //把where拆解成多个不带or的子句, 然后依次根据每个子句来获取值
    private Expression[] groupby;
    private Expression having; //having子句



    public Select(SQLConnection connection) {
        super(connection);
        from = new ArrayList<>();
        andConditions = new ArrayList<>();
    }

    @Override
    public void init() {
        List<Expression> list = new ArrayList<>();
        for(int i = 0; i < select.size(); i ++) {
            Expression expression = select.get(i);
            if(expression.isWildcard()){ //如果是通配符*, 则取出来然后把当前table的所有column放进去
                String tableAlias = expression.getTableAlias();
                if(tableAlias == null){ //如果没有tablealias , 则将from中的所有表内的column加入进去
                    for(int k = 0; k < from.size(); k++){
                        Table table = from.get(k).getTable();
                        tableAlias = table.getName();
                        for(Column column : table.getColumns()){
                            list.add(new ExpressionColumn(tableAlias, column.getName()));
                        }
                    }
                }
                //如果有tablealias, 则寻找from内的表的alias, 如果找不到则抛出错误
                else {
                    for(int k = 0; k < from.size(); k++){
                        Table table = from.get(k).getTable();
                        //String tableAliasfrom = table.getName();
                        String tableAliasfrom = from.get(k).getTableAlias();
                        if(tableAlias.equals(tableAliasfrom)) {
                            for(Column column : table.getColumns()){
                                list.add(new ExpressionColumn(tableAlias, column.getName()));
                            }
                            break;
                        } else if (k == from.size() - 1){
                            throw  new RuntimeException("Synxax error");
                        }
                    }
                }
            }
            //如果不是通配符, 则直接放进去
            else {
                list.add(expression);
            }
        }
        this.select = list;

        for (TableFilter f : from) { //将tableFilter分发到每个ExpressionColumn上
            for (Expression expr : select ) {
                expr.mapColumns(f, 0);
            }
            if(where != null) {
                where.mapColumns(f, 0);
            }
        }
    }

    /**
     * 查询入口
     * */
    @Override
    protected SQLResultSet executeImpl(SQLStatement statement) {
        init();
        SQLResultSet resultSet = new SQLResultSet();
        SQLResultSetMetaData resultSetMetaData = new SQLResultSetMetaData();
        Column[] columns = new Column[select.size()];
        for(int i = 0; i < select.size(); i++){
            columns[i] = ((ExpressionColumn)select.get(i)).getColumn();
        }
        resultSetMetaData.setColumns(columns);
        resultSet.setMetaData(resultSetMetaData);
        List<Row> results = new ArrayList<>();
        HashSet<Row> resultsSet = new HashSet<>(); //此变量用于对结果进行去重

        Value[] values ;
        int size = select.size();
        if (andConditions != null && andConditions.size() > 0){
            for(Expression expression : andConditions){ //每个and条件式单独执行一次查询
                for(TableFilter tableFilter : from){
                    tableFilter.init(); //每次进入and表达式时, 都清空一次之前的and表达式记录
                }
                if(!multiTable()){ //如果是单表查询
                    TableFilter tableFilter = from.get(0);
                    while (expression instanceof ConditionAndOr) { //根据and条件式左右两边, 判断是否有index条件, 如果有的话, 则指定到cursor上
                        Expression right = ((ConditionAndOr) expression).getRight();
                        expression = ((ConditionAndOr) expression).getLeft();
                        if(tableFilter.getTable().getColumn( right.getColumnName() ).isPrimaryKey() &&  !(expression  instanceof ConditionLike)) {
                            //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                            tableFilter.addFilterCondition(right, false);
                            break;
                        }
                    }
                    if(tableFilter.getTable().getColumn( expression.getColumnName() ).isPrimaryKey() &&  !(expression  instanceof ConditionLike)) {
                        //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                        tableFilter.addFilterCondition(expression, false);
                    }

                    while (tableFilter.next()){ //next方法移动游标并返回是否还有值
                        if(where.getValue(connection).equals(ValueBoolean.get(true))){ //如果当前的where 表达式为true
                            //此时, 从select列表中的表达式分别getValue, 并写入到resultSet中
                            values = new Value[size];
                            for(int i = 0; i < size; i++){
                                values[i] = select.get(i).getValue(connection);
                            }
                            Row row = new Row(values);
                            if(!resultsSet.contains(row)){
                                results.add(row);
                                resultsSet.add(row);
                            }
                        }
                    }

                } else {
                    //当前的表到达终点时, 重置当前表, 执行下一个表的next操作, 如果已经没有下一个表, 则退出
                    while (expression instanceof ConditionAndOr) { //根据and条件式左右两边, 判断是否有index条件, 如果有的话, 则指定到cursor上
                        Expression right = ((ConditionAndOr) expression).getRight();
                        expression = ((ConditionAndOr) expression).getLeft();
                        for(TableFilter tableFilter : from) {
                            if(tableFilter.getTableAlias().equals(right.getTableAlias()) //如果alias名一致, 多表查询时必然会有alias名
                                    && tableFilter.getTable().getColumn( right.getColumnName() ).isPrimaryKey()  //判断是否primarykey
                                    &&  !(right  instanceof ConditionLike)
                                    && !(right instanceof ConditionArithmetic && ((ConditionArithmetic) right).getValue() instanceof ExpressionColumn) ) {
                                //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                                tableFilter.addFilterCondition(right, false);
                                break;
                            }
                        }
                    }
                    for(TableFilter tableFilter : from) {
                        if(tableFilter.getTableAlias().equals(expression.getTableAlias()) //如果alias名一致, 多表查询时必然会有alias名
                                && tableFilter.getTable().getColumn( expression.getColumnName() ).isPrimaryKey()  //判断是否primarykey
                                &&  !(expression  instanceof ConditionLike)
                                && !(expression instanceof ConditionArithmetic && ((ConditionArithmetic) expression).getValue() instanceof ExpressionColumn) ) {
                            //如果是当前的条件是primary并且不是like语句则加入到cursor中作为查询条件
                            tableFilter.addFilterCondition(expression, false);
                        }
                    }

                    int k = 0;
                    for(TableFilter tableFilter : from){
                        tableFilter.next(); //将所有tableFilter都执行一遍next(), 以将指针指向有效值
                    }
                    while(k < from.size()) {

                        if(where.getValue(connection).equals(ValueBoolean.get(true))){ //如果当前的where 表达式为true
                            //此时, 从select列表中的表达式分别getValue, 并写入到resultSet中
                            values = new Value[size];
                            for(int i = 0; i < size; i++){
                                values[i] = select.get(i).getValue(connection);
                            }
                            Row row = new Row(values);
                            if(!resultsSet.contains(row)){
                                results.add(row);
                                resultsSet.add(row);
                            }

                        }
                        while (k < from.size() && !from.get(k).next()){ //如果当前的表已经没有next了, 就重置当前表, 并对下一个表进行next(如果有下一个表)
                            from.get(k).reset();
                            ++k;
                            if(k < from.size() && from.get(k).next()){
                                k = 0;
                            }
                        }
                    }
                }
            }
        } else  { // 没有where子句时, 直接遍历所有节点
            //当前的表到达终点时, 重置当前表, 执行下一个表的next操作, 如果已经没有下一个表, 则退出
            int k = 0;
            for(TableFilter tableFilter : from){
                tableFilter.next(); //将所有tableFilter都执行一遍next(), 以将指针指向有效值
            }
            while(k < from.size()) {
                //此时, 从select列表中的表达式分别getValue, 并写入到resultSet中
                values = new Value[size];
                for(int i = 0; i < size; i++){
                    values[i] = select.get(i).getValue(connection);
                }
                Row row = new Row(values);
                if(!resultsSet.contains(row)){
                    results.add(row);
                    resultsSet.add(row);
                }

                while (k < from.size() && !from.get(k).next()){ //如果当前的表已经没有next了, 就重置当前表, 并对下一个表进行next(如果有下一个表)
                    from.get(k).reset();
                    ++k;
                    if(k < from.size() && from.get(k).next()){
                        k = 0;
                    }
                }
            }
        }


        resultSet.setResults(results);
        return resultSet;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public ArrayList<Expression> getExpressions() {
        return null;
    }

    public void addTableFilter(TableFilter filter) {
        from.add(filter); //每个table会生成一个TableFilter, 每个TableFilter持有一个或零个index条件式
    }

    //todo 设置limit条件
    public void setLimit(String alias, Expression condition){

    }

    public void setSelect(List<Expression> select){
        this.select = select;
    }

    //是否存在多个表
    public boolean multiTable(){
        return from.size() > 1 ?true:false;
    }

    //将where子句分解成多个and条件子句, 从每个and条件子句中
    public void setWhere(Expression where) {
        this.where = where;
        Expression temp = where;
        while(temp instanceof ConditionAndOr && ((ConditionAndOr)temp).getAndOrType().equals(ConditionAndOr.OR)){ //如果当前的值是一个OR条件式
            andConditions.add(((ConditionAndOr) temp).getRight());
            temp = ((ConditionAndOr) temp).getLeft();
        }
        andConditions.add(temp);
    }
}
