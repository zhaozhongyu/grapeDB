package com.zzy.parser;

import com.zzy.Schema.Schema;
import com.zzy.Table.Column;
import com.zzy.Table.ColumnType;
import com.zzy.Table.Row;
import com.zzy.Table.Table;
import com.zzy.Value.Value;
import com.zzy.Value.ValueInt;
import com.zzy.Value.ValueString;
import com.zzy.parser.ddl.CreateTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class Parser
{
    public String currentToken = "";
    public int currentTokenType;
    public LinkedList<String> tokens;
    public Schema schema = new Schema(false, "data");

    /**
     * 不处理join
     *
     */
    public void parse(){
        next();
        if(checkEqual("SELECT")){
            parseSelect();
        }
        else if(checkEqual("CREATE")){
            parseCreate();
        }
        else if(checkEqual("INSERT")){
            parseInsert();
        }
        else if(checkEqual("DELETE")){
            parseDelete();
        }
        else if(checkEqual("UPDATE")){
            parseUpdate();
        } else {
            throw new RuntimeException("Syntax error.");
        }

    }

    /**
     * select * from table where name = 'peter';
     */
    public void parseSelect(){

        ArrayList<String> list = new ArrayList<>();
        while (! checkNext("FROM")){ //把from之前的所有String加入list中
            list.add(currentToken);
        }
        String tableName = next();
        Table table = schema.getTableOrView(tableName); 
        if(checkNext("WHERE")){ //where子句只处理主键查找
            String columnName = next();
            String symbol = next();
            next();
            Value value = parseValue();
            Row row = table.search(value, columnName);
            System.out.println(row);
        } else {
            
        }
            
    }

    public void parseCreate(){
        next();
        if(checkEqual("TABLE")){
            parseCreateTable();
            return;
        }
        else if(checkEqual("INDEX")){
            parseCreateIndex();
            return;
        }
        throw new RuntimeException("Syntax error,table or index must be after create"); //暂时只支持创建Table, Index
    }

    /**
     * INSERT INTO Persons VALUES (001, 'Gates', 'Bill', 'Xuanwumen 10', 'Beijing')
     * INSERT INTO Persons (ID, LastName, Address) VALUES (001, 'Wilson', 'Champs-Elysees')
     */
    public void parseInsert(){
        if(!checkNext("INTO")){
            throw new RuntimeException("Syntax error, Into must be after insert.");
        }
        String tableName = next();
        Table table = schema.getTableOrView(tableName);
        if (table == null){ //map在获取不到指定key时返回null, 所以要判断table是否为null
            throw new RuntimeException("Not such Table.");
        }
        if(checkNext("VALUES")){  //全量插入
            Value[] values = parseValues();
            table.insert(values);
        } else { //指定插入
            Column[] columns = parserColumns(table);
            if(! checkNext("VALUES")){
                throw new RuntimeException("Syntax error, Values must be after column.");
            }
            Value[] values = parseValues();
            table.insert(values, columns);
        }

    }

    /**
     * delete from persons where name = 'peter';
     * demo 只搞主键的查找
     */
    public void parseDelete(){
        if(! checkNext("FROM")){
            return;
        }
        String tableName = next();
        Table table = schema.getTableOrView(tableName);
        if(! checkNext("WHERE")){
            return;
        }
        String columnName = next();
        String symbol = next();
        next();
        Value value = parseValue();
        table.delete(value, columnName);
    }

    /**
     * Update table set name = 'peter' where id = '1';
     */
    public void parseUpdate(){

    }

    /**
     * 1.不处理if not exist
     *
     */
    public void parseCreateTable(){

        CreateTable createTable = new CreateTable(schema); //通过这个类来创建table
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
        createTable.create();
    }

    /**
     *
     */
    public void parseCreateIndex(){

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
            next(); //将currentToken移动到第一个参数上
            list.add(parseValue());
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
    public Value parseValue(){
        if(currentToken.charAt(0) == '\'' && currentToken.charAt(currentToken.length() - 1) == '\''){
            return new ValueString(currentToken.substring(1, currentToken.length() - 1));
        }
        return new ValueInt(Integer.parseInt(currentToken));
    }

    //判断list中下一个字符串是否与输入的一致
    public boolean checkNext(String str){
        return checkEqual(next());
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
    /**
     *CREATE TABLE Persons (Id_P int not null primary key, LastName varchar(255) NOT NULL, FirstName varchar(255), Address varchar(255), City varchar(255) );
     * @param sql
     * 暂不处理*
     */
    public LinkedList<String> Tokenizer(String sql ){
        LinkedList<String> list = new LinkedList<>();
        int current = 0;
        char c;
        StringBuilder sb;
        while(current < sql.length()){

            c = sql.charAt(current); //先取值
            //检查是否字符
            if(Character.isLetter(c)){
                sb = new StringBuilder();
                while (Character.isLetterOrDigit(c)){ //只要以字母开头, 则获取到后面的所有字母或数字, 暂时不管模糊查询
                    sb.append(c);
                    c = sql.charAt(++ current);
                }
                list.add(sb.toString().toUpperCase()); //全部转为大写字母
                continue;
            }

            //检查是否是数字
            if(Character.isDigit(c)) {
                sb = new StringBuilder();
                while(Character.isDigit(c)){
                    sb.append(c);
                    c = sql.charAt(++ current);
                }
                list.add(sb.toString());
                continue;
            }

            if(Character.isSpaceChar(c) ){ //遇到空格或者,则继续下一步
                current ++;
                continue;
            }

           /* if(c == ')' || c == '(' || c == '[' || c == ']' || c == '-' || c == '!'|| c == ','){  //如果是括号, 则加入token中, []-是正则的作用
                current++;
                list.add(String.valueOf(c));

                continue;
            }*/
            switch (c){
                case '(':
                case '[':
                    list.add(String.valueOf(c));
                    current++;
                    break;
                case ')':
                case ']':
                    list.add(String.valueOf(c));
                    current++;
                    break;
                case '\'': //如果遇到' 说明是insert或update或select中的表示varchar的值, 此时继续往下读, 一直读取到下一个'出现
                    sb = new StringBuilder("'");
                    char lastc = c; //设置last以判断是否是转义的'
                    while(true){
                        c = sql.charAt(++current);
                        if(c == '\'' && lastc != '\\'){
                            sb.append(c);
                            list.add(sb.toString()); //varchar 不转大写
                            break;
                        }
                        sb.append(c);
                        lastc = c;
                    }
                    current++;
                    break;
                case '=':
                    list.add(String.valueOf(c));
                    current++;
                    break;
                case '>':
                    if(sql.charAt(current + 1) == '='){
                        list.add(">=");
                        current += 2;
                    } else {
                        list.add(">");
                        current ++;
                    }
                    break;
                case '<':
                    if(sql.charAt(current + 1) == '='){
                        list.add("<=");
                        current += 2;
                    } else {
                        list.add("<");
                        current ++;
                    }
                    break;
                case '!':
                    if(sql.charAt(current+1) != '='){
                        throw new  RuntimeException("Syntax error, '!' must be before '='.");
                    }
                    list.add("!=");
                    current += 2;
                    break;

                case ',':
                    list.add(String.valueOf(c));
                    current++;
                    break;

            }

            if(c == ';'){ //遇到;则表示这段sql已结束
                break;
            }

        }
        tokens = list;
        return list;
    }
}
