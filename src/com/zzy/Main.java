package com.zzy;

import java.sql.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        Class.forName("com.zzy.engine.SQLDriver").newInstance();
        Connection con = DriverManager.getConnection("jdbc:zzydb", new Properties());
        Statement statement = con.createStatement();
        System.out.println("\tUse the USE command to change the database context.");

        execute(statement, "CREATE TABLE Persons (name varchar(255) not null primary key, address varchar(255) NOT NULL, City varchar(255) );");
        execute(statement, "CREATE TABLE humans (name varchar(255) not null primary key, address varchar(255) NOT NULL, City varchar(255) );");

        execute(statement, "INSERT INTO Persons VALUES ('Gates peter',  'Xuanwumen 10', 'Beijing');");
        execute(statement, "INSERT INTO Persons VALUES ('William',  'Xuanwumen 10', 'Beijing');");
        execute(statement, "INSERT INTO Persons VALUES ('Gates Bill',  'Xuanwumen 10', 'Beijing');");
        execute(statement, "select * from Persons where City = 'Beijing'");
        execute(statement, "select * from Persons where City = 'Beijing' and name = 'William'");
        execute(statement, "select * from Persons where  name = 'Gates Bill'or City = 'Beijing' ;");

        execute(statement, "INSERT INTO humans VALUES ('Gates peter',  'Changanjie', 'Beijing');");
        execute(statement, "INSERT INTO humans VALUES ('William',  'Changanjie', 'Beijing');");
        execute(statement, "INSERT INTO humans VALUES ('Gates Bill',  'Changanjie', 'Beijing');");

        execute(statement, "select p.* from Persons as p where  p.name = 'Gates Bill'or p.City = 'Beijing' ;");
        execute(statement, "select h.* from Persons as p, humans as h where  p.name = 'Gates Bill' and p.name = h.name;");
        execute(statement, "select h.* from Persons as p, humans as h where  p.name = 'Gates Bill' and p.name = h.name or h.name = 'William';");


    }


    private static void execute(Statement statement, String sql) throws SQLException {
        boolean isRS = statement.execute(sql);
        if (isRS) {
            printRS(statement.getResultSet());
        }
    }

    private static void printRS(ResultSet rs) throws SQLException {
        System.out.println("--------------------------------");
        ResultSetMetaData md = rs.getMetaData();
        int count = md.getColumnCount();
        for (int i = 0; i < count; i++) {
            System.out.print(md.getColumnLabel(i));
            System.out.print('\t');
        }
        System.out.println();
        while (rs.next()) {
            for (int i = 0; i < count; i++) {
                System.out.print(rs.getObject(i));
                System.out.print('\t');
            }
            System.out.println();
        }

    }
}
