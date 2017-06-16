package com.zzy.Index;

import java.util.ArrayList;

public class INode {
    public boolean isLeaf = false;
    public INode parent = null;
    public ArrayList<Comparable> keys = new ArrayList();
    public ArrayList<INode> childNodes = new ArrayList();

    public INode() {
    }
}
