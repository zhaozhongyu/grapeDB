package com.zzy.Storage;

import com.zzy.Index.*;
import com.zzy.Schema.Schema;
import com.zzy.Table.Column;
import com.zzy.Table.Row;
import com.zzy.Table.Table;
import com.zzy.Value.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import static com.zzy.constant.SysProperties.*;

/**
 *
 */
public class storeImpl implements Store {
    public Schema schema;
    public storeImpl(){
        schema = new Schema(true, "data");
        init();
    }

    @Override
    public void init() {
        File file = new File(dirname);
        if(!file.exists()){ //
            file.mkdirs();
            return;
        }
        file = new File(dirname+ "/"+filename);
        if(!file.exists()){
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            byte[] bytes = new byte[20480];
            randomAccessFile.readUTF(); //第一个string记录名字, 不重要
            if (randomAccessFile.readDouble() > version){
                System.out.println("version is bigger than current version");
            }
            randomAccessFile.readLong(); //这个数字记录的是上一次写入记录的时间
            //往后记录的是table的信息
            int length = randomAccessFile.readInt(); //第一个长度记录table的数量
            while(length > 0){ //根据table记录依次创建table实例
                String tablename = randomAccessFile.readUTF();
                int num = randomAccessFile.readInt();
                int mode, size, columnType;String columnName ;
                Column[] columns = new Column[num];
                Column primarykey = null;
                Table table;
                for(int i = 0; i < num; i++){
                    columnName = randomAccessFile.readUTF();
                    mode = randomAccessFile.readShort(); //使用十进制来确定column的类型, 其中primary key, unique, nullable分别在321位上置为1
                    columnType = randomAccessFile.readInt();
                    size = randomAccessFile.readInt(); //比如varchar(255)
                    columns[i] = new Column(columnName,columnType, size, (mode / 100 == 1),(mode % 100 / 10 == 1), (mode % 10 == 1));
                    if(columns[i].isPrimaryKey()){
                        primarykey = columns[i];
                    }
                }
                if(primarykey != null){
                    table = new Table(tablename,schema,columns, primarykey);
                } else {
                    table = new Table(tablename,schema,columns);
                }
                length --;
            }
            //依次读取index
            while (randomAccessFile.getFilePointer() != randomAccessFile.length()) {
                // tablename | columnname | root节点 ...
                String tablename = randomAccessFile.readUTF();
                Table table = schema.getTableOrView(tablename);
                String columnname = randomAccessFile.readUTF();
                Column column = table.getColumn(columnname);
                int m = randomAccessFile.readInt(); //标示是几阶B+树
                boolean isDefault = randomAccessFile.readBoolean(); //标示是否default index
                TreeGen treeGen = new TreeGen(m);
                Index index = new Index(column);
                index.setIndexTree(treeGen);
                if (isDefault) {
                    table.setDefaultIndex(index);
                } else {
                    table.addIndex(index);
                }

                //从这里开始是节点信息, 组成为bool是否叶子节点 | key的数量 | key ..| child(value), 根据广度优先组合, key 是int | bytes
                Stack<INode> stack = new Stack<>();
                boolean isleaf;
                int num;
                int type;
                //读取根节点
                type = randomAccessFile.readInt(); //这个值记录当前的tree的value类型
                isleaf = randomAccessFile.readBoolean();
                num = randomAccessFile.readInt();
                INode node;
                if (isleaf) {
                    node = new TreeLeaf();
                } else {
                    node = new TreeNode();
                }
                for (int i = 0; i < num; i++) {
                    length = randomAccessFile.readInt();
                    randomAccessFile.read(bytes, 0, length);
                    Value value = Bytes2Value(bytes, type);
                    node.keys.add(value);
                }
                INode root = node;
                stack.push(node);
                while (!stack.isEmpty()) {
                    INode current = stack.pop();
                    if (!current.isLeaf) { //如果不是叶子节点, 则下面要读取与current的key数量对应的Inode设为子节点
                        for (int n = 0; n < current.keys.size(); n++) {
                            isleaf = randomAccessFile.readBoolean();
                            num = randomAccessFile.readInt();
                            if (isleaf) {
                                node = new TreeLeaf();
                            } else {
                                node = new TreeNode();
                            }
                            for (int i = 0; i < num; i++) {
                                length = randomAccessFile.readInt();
                                randomAccessFile.read(bytes, 0, length);
                                Value value = Bytes2Value(bytes, type);
                                node.keys.add(value);
                            }
                            current.childNodes.add(node);
                            stack.push(node);
                        }
                    } else { //如果是叶子节点, 则下面要读取与current的key数量对应的row
                        for (int n = 0; n < current.keys.size(); n++) {
                            length = randomAccessFile.readInt(); //row的长度
                            randomAccessFile.read(bytes, 0, length);
                            ((TreeLeaf) current).values.add(Bytes2Row(Arrays.copyOfRange(bytes, 0, length)));
                        }
                    }

                }
                treeGen.set_rootNode(root);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    //正常关闭时把index文件重写一遍
    public void close() {
        File file = new File(dirname + "\\"+filename);
        file.delete();
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            byte[] bytes = new byte[2048];
            randomAccessFile.writeUTF(filename);
            randomAccessFile.writeDouble(version);
            randomAccessFile.writeLong(System.currentTimeMillis());
            //写table
            randomAccessFile.writeInt(schema.getTables().size()); //table的数量
            for(Table table : schema.getTables()){
                randomAccessFile.writeUTF(table.getName());
                randomAccessFile.writeInt(table.getColumns().length);
                for(Column column : table.getColumns()){
                    randomAccessFile.writeUTF(column.getName());
                    int sum = 0;
                    if(column.isPrimaryKey()){
                        sum += 100;
                    }
                    if(column.isUnique()){
                        sum += 10;
                    }
                    if(column.isNullable()){
                        sum += 1;
                    }
                    randomAccessFile.writeShort(sum); //使用十进制来确定column的类型, 其中primary key, unique, nullable分别在321位上置为1
                    randomAccessFile.writeInt(column.getType().ordinal());
                    randomAccessFile.writeInt(column.getLength());
                }
            }
            for(Table table : schema.getTables()){

                for(Index index : table.getIndexs()){
                    randomAccessFile.writeUTF(table.getName());
                    randomAccessFile.writeUTF(index.getColumnName());
                    randomAccessFile.writeInt(index.getIndexTree().getM());
                    randomAccessFile.writeBoolean(index.getDefault());
                    Stack<INode> stack = new Stack<>();
                    Stack<TreeLeaf> leaves = new Stack<>();
                    stack.push(index.getIndexTree().getRootNode());
                    randomAccessFile.writeInt(index.getType()); //这里记录当前tree的value类型
                    while(!stack.isEmpty()){
                        INode node = stack.pop();
                        randomAccessFile.writeBoolean(node.isLeaf);
                        randomAccessFile.writeInt(node.keys.size());
                        for(Comparable value : node.keys){
                            randomAccessFile.writeInt(getValueSize(value));
                            randomAccessFile.write(Value2Bytes(value));
                        }
                        if(!node.isLeaf){
                            for(INode inode : node.childNodes){
                                stack.push(inode);
                            }
                        } else { //如果是leaf则将所有节点加入到leaves中以备写出
                            leaves.push((TreeLeaf)node);
                        }
                    }
                    while(!leaves.isEmpty()){ //写出所有leaf中的row
                        TreeLeaf leaf = leaves.pop();
                        for(Object o : leaf.values){
                            byte[] bytes1 = Row2Bytes((Row)o);
                            randomAccessFile.writeInt(bytes1.length);
                            randomAccessFile.write(bytes1);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getValueSize(Comparable value){
        if(value instanceof ValueString){
            return ((ValueString)value).getValue().getBytes().length;
        }
        if(value instanceof ValueInt){
            return 4;
        }
        if(value instanceof ValueBoolean){
            return 1;
        }
        if(value instanceof ValueNull){
            return 1;
        }
        if(value instanceof ValueDouble){
            return 8;
        }
        return 0;
    };

    public byte[] Value2Bytes(Comparable value){
        if(value instanceof ValueString){
            return ((ValueString) value).getValue().getBytes();
        }
        if(value instanceof ValueInt){
            int num = ((ValueInt)value).getValue();
            byte[] src = new byte[4];
            src[3] =  (byte) ((num>>24) & 0xFF);
            src[2] =  (byte) ((num>>16) & 0xFF);
            src[1] =  (byte) ((num>>8) & 0xFF);
            src[0] =  (byte) (num & 0xFF);
            return src;
        }
        if(value instanceof ValueBoolean){
            if(value.equals(ValueBoolean.get(true))){
                return new byte[]{0x00};
            }
            return new byte[]{0x01};
        }
        if(value instanceof ValueNull){
            return new byte[]{0x0f};
        }
        if(value instanceof ValueDouble){
            double d = ((ValueDouble)value).getValue();
            long l = Double.doubleToLongBits(d);
            byte[] b = new byte[8];
            b[0] = (byte) (l >>> 56);
            b[1] = (byte) (l >>> 48);
            b[2] = (byte) (l >>> 40);
            b[3] = (byte) (l >>> 32);
            b[4] = (byte) (l >>> 24);
            b[5] = (byte) (l >>> 16);
            b[6] = (byte) (l >>> 8);
            b[7] = (byte) (l);
            return b;
        }

        return new byte[0];
    }

    public Value Bytes2Value(byte[] bytes, int type){
        switch (type){
            case Value.NULL:
                return ValueNull.INSTANCE();
            case Value.BOOLEAN:
                if(bytes[0] == 0x00){
                    return ValueBoolean.get(true);
                } else {
                    return ValueBoolean.get(false);
                }
            case Value.INT:
                int value;
                value = (int) ((bytes[0] & 0xFF)
                        | ((bytes[1] & 0xFF)<<8)
                        | ((bytes[2] & 0xFF)<<16)
                        | ((bytes[3] & 0xFF)<<24));
                return ValueInt.get(value);
            case Value.Double:
                long l = ((long) bytes[0] << 56) & 0xFF00000000000000L;
                // 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
                l |= ((long) bytes[1] << 48) & 0xFF000000000000L;
                l |= ((long) bytes[2] << 40) & 0xFF0000000000L;
                l |= ((long) bytes[3] << 32) & 0xFF00000000L;
                l |= ((long) bytes[4] << 24) & 0xFF000000L;
                l |= ((long) bytes[5] << 16) & 0xFF0000L;
                l |= ((long) bytes[6] << 8) & 0xFF00L;
                l |= (long) bytes[7] & 0xFFL;
                return new ValueDouble(Double.longBitsToDouble(l));
            case Value.BYTES:
                return ValueNull.INSTANCE();
            case Value.STRING:
                return new ValueString(new String( bytes));
            case Value.BLOB:
                return ValueNull.INSTANCE();
            default:
                return ValueNull.INSTANCE();
        }
    }

    public byte[] Row2Bytes(Object o){
        Row row = (Row)o;
        Value value;
        byte[] bytes, bytes1, bytes2;
        byte[] result = new byte[2048];
        int len = 0, last = 0; //last记录当前的result 写到哪里了
        for(int i = 0; i < row.length(); i++){
            value = row.getValue(i);
            bytes = Value2Bytes(value);
            len += bytes.length + 8; //4 表示的是2个int的大小
            if(len > result.length){ //如果初始值2048不够用, 则扩展一下
                byte[] nresult = new byte[2048 + len];
                System.arraycopy(result,0, nresult, 0, result.length);
                result = nresult;
            }
            bytes1 = int2Bytes(value.getType());//类型
            bytes2 = int2Bytes(bytes.length);
            System.arraycopy(bytes1, 0, result, last, 4); last += 4;
            System.arraycopy(bytes2, 0, result, last, 4); last += 4;
            System.arraycopy(bytes, 0, result, last, bytes.length);last += bytes.length;
        }
        return Arrays.copyOf(result, len);
    }

    public byte[] int2Bytes(int num ){
        byte[] src = new byte[4];
        src[3] =  (byte) ((num>>24) & 0xFF);
        src[2] =  (byte) ((num>>16) & 0xFF);
        src[1] =  (byte) ((num>>8) & 0xFF);
        src[0] =  (byte) (num & 0xFF);
        return src;
    }

    public int bytes2int(byte[] bytes){
        int value;
        value = (int) ((bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF)<<8)
                | ((bytes[2] & 0xFF)<<16)
                | ((bytes[3] & 0xFF)<<24));
        return value;
    }

    public Row Bytes2Row(byte[] bytes){
        ArrayList<Value> values = new ArrayList<>();
        int i = 0;
        while(i < bytes.length){
            int type = bytes2int(Arrays.copyOfRange(bytes, i, i+4)); i+= 4;
            int len = bytes2int(Arrays.copyOfRange(bytes, i, i+4)); i+= 4;
            Value value = Bytes2Value(Arrays.copyOfRange(bytes, i, i+len), type); i += len;
            values.add(value);
        }
        return new Row((Value[])values.toArray(new Value[values.size()]));
    }
}
