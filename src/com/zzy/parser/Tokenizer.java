package com.zzy.parser;

import java.util.LinkedList;

public class Tokenizer {
    /**
     *CREATE TABLE Persons (Id_P int not null primary key, LastName varchar(255) NOT NULL, FirstName varchar(255), Address varchar(255), City varchar(255) );
     * @param sql
     */
    public static LinkedList<String> Tokenizer(String sql ){
        LinkedList<String> list = new LinkedList<>();
        int current = 0;
        char c;
        StringBuilder sb;
        while(current < sql.length()){
            c = sql.charAt(current); //先取值
            //检查是否字符
            if(Character.isLetter(c)){
                sb = new StringBuilder();
                while (Character.isLetterOrDigit(c) ){ //只要以字母开头, 则获取到后面的所有字母或数字, 或者是点号, 如city.Name, 或者是.*, 如city.*
                    sb.append(c);
                    c = sql.charAt(++ current);
                }
                if(c == '.'){ //如果后面有.*  或者.后接columnname
                    sb.append(c);
                    c = sql.charAt(++ current);
                    if(c == '*'){
                        sb.append(c);
                        c = sql.charAt(++ current);
                    } else {
                        while (Character.isLetterOrDigit(c) ){
                            sb.append(c);
                            c = sql.charAt(++ current);
                        }
                    }
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
            if(c == ';'){ //遇到;则表示这段sql已结束
                break;
            }
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
                case '\'': //如果遇到' 说明是insert或update或select中的表示varchar的值, 此时继续往下读, 一直读取到下一个'出现, 对于where子句中的正则表达式, 必须要用''包起来
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
                case '*':
                    list.add(String.valueOf(c));
                    current++;
                    break;
                default:
                    System.out.println(c);
                    throw new RuntimeException("Unrecognized characters.");
            }



        }
        return list;
    }
}
