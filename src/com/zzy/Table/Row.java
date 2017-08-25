package com.zzy.Table;

import com.zzy.Value.Value;

import java.util.Arrays;

public class Row
{
    private Value[] data;      //标识实际的一组数据, 以数组方式存放

    public Row(Value[] data) {
        this.data = data;
    }

    public Value getValue(int i) {
        return data[i];
    }


    public void setValue(int i, Value v) {
        data[i] = v;
    } //update的时候使用

    public int length(){ return data.length; }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for(int i = 0; i < data.length; i++){
            sb.append(data[i]+" ");
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public boolean equals(Object o){
        Row row = (Row)o;
        if(row.length() != length()){
            return false;
        }
        for(int i = 0; i < length(); i ++){
            if(! getValue(i).equals(row.getValue(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode(){
        int code = 0;
        for(int i = 0; i < length(); i++){
            code += getValue(i).hashCode() * (i+1);
        }
        return code;
    }
}
