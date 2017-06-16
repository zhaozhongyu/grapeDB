package com.zzy.parser;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by zWX206936 on 2017/6/10.
 */
public class Parser
{

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
            if(Character.isLetterOrDigit(c) || c == '_' || c == '%'){
                sb = new StringBuilder();
                while (Character.isLetterOrDigit(c) || c == '_' || c == '%'){ //只要以字母开头, 则获取到后面的所有字母或数字
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

            if(c == ')' || c == '(' || c == '[' || c == ']' || c == '-' || c == '!'|| c == ','){  //如果是括号, 则加入token中, []-是正则的作用
                current++;
                list.add(String.valueOf(c));
                continue;
            }

            if(c == ';'){ //遇到;则表示这段sql已结束
                break;
            }

        }
        return list;
    }
}
