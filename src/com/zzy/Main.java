package com.zzy;

import com.zzy.parser.Parser;

public class Main {

    public static void main(String[] args)
    {
        Parser parser = new Parser();
        parser.Tokenizer("CREATE TABLE Persons (name varchar(255) not null primary key, address varchar(255) NOT NULL, City varchar(255) );");
        parser.parse();
        parser.Tokenizer("INSERT INTO Persons VALUES ('Gates Bill',  'Xuanwumen 10', 'Beijing');");
        parser.parse();
        parser.Tokenizer("select name from persons where name = 'Gates Bill';");
        parser.parse();
        System.out.println();
    }
}
