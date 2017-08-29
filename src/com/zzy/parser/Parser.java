package com.zzy.parser;

import com.zzy.Expression.*;
import com.zzy.Schema.Schema;
import com.zzy.Table.Column;
import com.zzy.Table.ColumnType;
import com.zzy.Table.Table;
import com.zzy.Table.TableFilter;
import com.zzy.Value.Value;
import com.zzy.Value.ValueInt;
import com.zzy.Value.ValueString;
import com.zzy.engine.SQLConnection;
import com.zzy.parser.ddl.CreateTable;
import com.zzy.parser.dml.*;

import java.sql.SQLException;
import java.util.*;

public class Parser
{
    public String currentToken = "";
    public LinkedList<String> tokens;
    public Schema schema;

    private int[] characterTypes;
    private String originalSQL;
    private String sqlCommand;
    private char[] sqlCommandChars;

    private int parseIndex;
    private int lastParseIndex;

    Select currentSelect = null;
    Prepared currentPrepared = null;

    private String schemaName;

    private final boolean identifiersToUpper = true;

    private SQLConnection connection;

    public Parser(SQLConnection connection, Schema schema) {
        this.connection = connection;
        this.schema = schema;
    }

    /**
     *
     *
     */
    public Prepared parse(String sql){
        tokens = Tokenizer.Tokenizer(sql);
        next();
        if(checkEqual("SELECT")){
            return parseSelect();
        }
        else if(checkEqual("CREATE")){
            return parseCreate();
        }
        else if(checkEqual("INSERT")){
            return parseInsert();
        }
        else if(checkEqual("DELETE")){
            return parseDelete();
        }
        else if(checkEqual("UPDATE")){
            return parseUpdate();
        } else {
            throw new RuntimeException("Syntax error.");
        }
    }

    /**
     * select * from table where name = 'peter';
     */
    public Prepared parseSelect(){
        currentSelect = new Select(connection);
        ArrayList<Expression> list = new ArrayList<>();
        while (! checkNext("FROM")){ //把from之前的所有string解析成ExpressionColumn加入list中
            list.add(new ExpressionColumn(currentToken));
        }
        currentSelect.setSelect(list);
        while(!checkEqual("WHERE")){
            String tableName = next();
            Table table = schema.getTableOrView(tableName);
            String alias = tableName;
            if (checkNext("AS")){
                alias = next();
                TableFilter tableFilter = new TableFilter(connection, table, alias, currentSelect);
                currentSelect.addTableFilter(tableFilter);
                next(); //将current走到逗号处或者走到where处 todo 此处未检查合法性
            } else if (checkEqual(",")){ //没有alias
                TableFilter tableFilter = new TableFilter(connection, table, alias, currentSelect);
                currentSelect.addTableFilter(tableFilter);
            } else { //tablename后面要么是as, 要么是逗号, 两个都不是的话说明已结束tablename
                TableFilter tableFilter = new TableFilter(connection, table, alias, currentSelect);
                currentSelect.addTableFilter(tableFilter);
                break;
            }

        }

        if(checkEqual("WHERE")){
            currentSelect.setWhere(parseWhere());
        } else {

        }
        return currentSelect;
    }

    /**
     * 解析where子句, 暂时不实现join多表查询, 还有select子查询
     * 每一个条件式, = 表达式由3个字符串组成, LIKE表达式由3或4个字符串组成, IS表达式由3到4个字符串组成, Between表达式由5个字符串组成, In表达式由不限量个字符串组成
     * 进入此方法时, currentToken是where的下一个
     */
    public Expression parseWhere(){
        Expression expression = parseAnd();
        while (checkEqual("OR")){
            expression = new ConditionAndOr(ConditionAndOr.OR, expression, parseAnd());
        }
        return expression;
    }

    // 循环解析And表达式
    private Expression parseAnd(){
        Expression expression = parseCondition();
        while(checkNext("AND")){
            expression = new ConditionAndOr(ConditionAndOr.AND, expression, parseCondition());
        }
        return expression;
    }

    // 解析单条条件式, 比如 id = 1, 当进入该方法时, currentToken应该就为id
    public Expression parseCondition() {
        ExpressionColumn left = new ExpressionColumn(next());
        String symbol;
        Expression value;
        boolean Not = false;
        String columnName;
        while (true){
            switch (next()) {
                case "=":
                case ">=":
                case "<=":
                case "<":
                case ">":
                case "!=":
                    symbol = currentToken;
                    if(next().startsWith("'") && currentToken.endsWith("'")){
                        value = new ValueExpression(parseValue(currentToken));
                    } else {
                        value = new ExpressionColumn(currentToken);
                    }

                    ConditionArithmetic arithmetic = new ConditionArithmetic(left, symbol, value);
                    //if(left.isIndex()){
                    //    currentSelect.setLimit(arithmetic);
                    //}
                    return arithmetic;// 这里不可能有not

                case "IN": // id in ( 1, 2, 3, 4, 5) 或者 id in ( select id from persons )
                    if (!checkNext("(")){
                        try{
                            throw new SQLException("Syntax error");
                        }  catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if(checkNext("SELECT")){ // select 子查询
                        throw new RuntimeException("Not Support SubQuery yet.");
                    }
                    List<Expression> list = new ArrayList<>();
                    while(! checkEqual(")")){
                        Expression e = new ValueExpression(parseValue(currentToken));
                        list.add(e);
                        if(checkNext(",")){
                            next();
                        } else if(checkEqual(")")){

                        } else {
                            throw new RuntimeException("Syntax error.");
                        }
                    }
                    ConditionIn conditionIn = new ConditionIn(left, list, Not);
                    if(left.isIndex()){
                        currentSelect.setLimit(conditionIn);
                    }
                    return conditionIn;

                case "BETWEEN": // name between 'aaa' and 'bbb'
                    value = new ValueExpression(parseValue(next()));
                    if(! checkNext("AND")){
                        try{
                            throw new SQLException("Syntax error");
                        }  catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    //                           column, leftValue, rightValue, Not
                    ConditionBetween conditionBetween = new ConditionBetween(left, value, new ValueExpression(parseValue(next())), Not);
                    if(left.isIndex()){
                        currentSelect.setLimit(conditionBetween);
                    }
                    return conditionBetween;

                case "IS":// IS NULL 或者 IS NOT NULL
                    if (checkNext("NULL")){
                        return new ConditionIs(left, false);
                    } else if(checkEqual("NOT") & checkNext("NULL")) {
                        return new ConditionIs(left,true);
                    } else { //is 后面既不是NOT NULL也不是NULL
                        try{
                            throw new SQLException("Syntax error");
                        }  catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "NOT": // NOT IN 或者 NOT LIKE 或 NOT BETWEEN
                    Not = true;
                    continue ;
                case "LIKE": // name like 'peter%'
                    return new ConditionLike(left, new ValueExpression(parseValue(next())), Not);

                default:
                    try {
                        throw new SQLException("Syntax error.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        }
    }



    public Prepared parseCreate(){
        next();
        if(checkEqual("TABLE")){
            return parseCreateTable();
        }
        else if(checkEqual("INDEX")){
            return parseCreateIndex();
        }
        throw new RuntimeException("Syntax error,table or Index must be after create"); //暂时只支持创建Table, Index
    }

    /**
     * INSERT INTO Persons VALUES (001, 'Gates', 'Bill', 'Xuanwumen 10', 'Beijing')
     * INSERT INTO Persons (ID, LastName, Address) VALUES (001, 'Wilson', 'Champs-Elysees')
     */
    public Query parseInsert(){
        Insert insert = new Insert(connection);
        if(!checkNext("INTO")){
            throw new RuntimeException("Syntax error, Into must be after insert.");
        }
        String tableName = next();
        Table table = schema.getTableOrView(tableName);
        if (table == null){ //map在获取不到指定key时返回null, 所以要判断table是否为null
            throw new RuntimeException("Not such Table.");
        }
        insert.setTable(table);
        if(checkNext("VALUES")){  //全量插入
            insert.setNoColumns(true);
            Value[] values = parseValues();
            insert.setValues(values);
        } else { //指定插入
            Column[] columns = parserColumns(table);
            insert.setColumns(columns);
            if(! checkNext("VALUES")){
                throw new RuntimeException("Syntax error, Values must be after column.");
            }
            Value[] values = parseValues();
            insert.setValues(values);
        }
        return insert;
    }

    /**
     * delete from persons where name = 'peter';
     */
    public Prepared parseDelete(){
        if(! checkNext("FROM")){
            try{
                throw new SQLException("Synxax error, 'delete' must follow a 'from'.");
            }catch (SQLException e ){ e.printStackTrace(); }
            return null;
        }
        Delete delete = new Delete(connection);
        String tableName = next();
        Table table = schema.getTableOrView(tableName);
        TableFilter tableFilter = new TableFilter(connection, table, tableName, null);
        delete.setFrom(tableFilter);
        if(! checkNext("WHERE")){
            return delete;
        }

        Expression where = parseWhere();
        delete.setWhere(where);
        return delete;
    }

    /**
     * Update table set name = 'peter' where id = '1';
     */
    public Prepared parseUpdate(){
        Update update = new Update(connection);
        String tableName = next();
        Table table = schema.getTableOrView(tableName);
        TableFilter tableFilter = new TableFilter(connection, table, tableName, null);
        update.setFrom(tableFilter);
        if(! checkNext("SET")){
            try{
                throw new SQLException("Synxax error, 'SET' must exist.");
            }catch (SQLException e ){ e.printStackTrace(); }
            return null;
        }
        HashMap<Column, Value> set = new HashMap<>();
        next();
        while(!checkEqual("WHERE") && !checkEqual(";")){
            Column column = table.getColumn(currentToken);
            if(! checkNext("=")){
                try{
                    throw new SQLException("Synxax error, '=' must exist in SET.");
                }catch (SQLException e ){ e.printStackTrace(); }
            }
            Value value = parseValue(next());
            set.put(column, value);
            next(); //移动到, 或者where上
        }
        update.setSet(set);
        if(checkEqual("WHERE")){
            update.setWhere(parseWhere());
        }
        return update;
    }


    /**
     * 1.不处理if not exist
     *
     */
    public Prepared parseCreateTable(){

        CreateTable createTable = new CreateTable(connection, schema); //通过这个类来创建table
        Stack<String> stack = new Stack<>();
        String tableName = next(); //table 后的第一个字段就是tablename, 此处不做异常处理
        createTable.setTableName(tableName);
        Column column = null ;
        do{
            switch (next()){
                case "(":
                    stack.push("(");
                    break;
                case ")":
                    stack.pop();
                    if(stack.isEmpty()){ //stack为空说明当前table的定义语句完结, 将当前column加到createTable中
                        createTable.addColumn(column);
                    }
                    break;
                case ",": //遇到,说明一个column的定义结束了, 将当前column加到createTable中, 再初始化一次column
                    createTable.addColumn(column);
                    column = null;
                    break;
                case "NOT": //not后面必须跟着null
                    if(!checkNext("NULL")){
                        throw new RuntimeException("Syntax error, Null must be after Not");
                    }
                    column.setNullable(false);
                    break;
                case "PRIMARY": //primary 后面必须跟着key
                    if(!checkNext("KEY")){
                        throw new RuntimeException("Syntax error, Primary must be before key.");
                    }
                    column.setUnique(true);
                    column.setPrimaryKey(true);
                    createTable.setPrimarykey(column);
                    break;
                case "UNIQUE":
                    column.setUnique(true);
                    break;
                case "VARCHAR":
                case "INT":
                case "DATE":
                case "TIME":
                case "BOOLEAN":
                    column.setType(Enum.valueOf(ColumnType.class, currentToken));
                    break;
                default:
                    if(stack.size() == 1){
                        column = new Column(currentToken); // 默认情况下第一个就是column name
                    } else { //适用于varchar(10)这种括号内取值
                        column.setLength(Integer.parseInt(currentToken));
                    }
                    break;
            }
            // next(); //取下一个token
        } while (!stack.isEmpty()); //当遇到(时, 往stack中push, 遇到)时, 从stack中pop
        //结束while说明create table的数据都读取完了
        //createTable.create();
        return createTable;
    }

    /**
     *
     */
    public Prepared parseCreateIndex(){
        return null;
    }

    public Column[] parserColumns(Table table ) {
        if(checkEqual("(")){
            throw new RuntimeException("Syntax error, ( must be after tablename.");
        }
        ArrayList<Column> list = new ArrayList<>();
        while(! ")".equals(currentToken)){ //遇到)终止解析
            next(); //将currentToken移动到第一个参数上
            list.add(table.getColumn(currentToken));
            if(! ",".equals(next()) && ! ")".equals(currentToken)){ //每个value值后只能是, 最后一个value值是), 简单校验语法
                throw new RuntimeException("Syntax error, ',' must be after column.");
            }
        }
        Column[] columns = new Column[list.size()];
        return list.toArray(columns);
    }


    /**
     * 解析values后的括号中的值, 生成一个row, 使用''包起来的, 解析为valuestring类型
     * 不做输入的合法性校验
     * 用于insert等
     */
    public Value[] parseValues(){
        ArrayList<Value> list = new ArrayList<>();
        if(! checkNext("(")){
            throw new RuntimeException("Syntax error, ( must be after values.");
        }

        while(! ")".equals(currentToken)){ //遇到)时终止循环
            //将currentToken移动到第一个参数上
            list.add(parseValue( next()));
            if(! ",".equals(next()) && ! ")".equals(currentToken)){ //每个value值后只能是, 最后一个value值是), 简单校验语法
                throw new RuntimeException("Syntax error, ',' must be after value.");
            }
        }
        Value[] values = new Value[list.size()];

        return list.toArray(values);
    }

    /**
     * 判断当前value的类型, 返回相应的value实例, 后续直接扩充此方法
     * @return
     */
    public Value parseValue(String currentToken){
        if(currentToken.charAt(0) == '\'' && currentToken.charAt(currentToken.length() - 1) == '\''){
            return new ValueString(currentToken.substring(1, currentToken.length() - 1));
        }
        return new ValueInt(Integer.parseInt(currentToken));
    }

    //判断list中下一个字符串是否与输入的一致
    public boolean checkNext(String str){
        next();
        return checkEqual(str);
    }

    public boolean checkEqual(String str) {
        if(currentToken.equals(str)){
            return true;
        }
        return false;
    }

    public String next(){
        if(!tokens.isEmpty()){
            currentToken = tokens.pollFirst();
            return currentToken;
        }
        return null;
    }


}
