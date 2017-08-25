package com.zzy.Index;

import java.util.ArrayList;
import java.util.Iterator;

public class TreeGen {
    private int _m = 4;
    private int _min = 2;
    private INode _rootNode = new TreeLeaf();
    private INode _research_result = null;
    private TreeLeaf _leaf_tmp = null;

    public int getM() {
        return this._m;
    }

    public int getMin() {
        return this._min;
    }

    public TreeGen(int m) {
        this._m = m;
        this._min = (int)Math.ceil((double)this._m / 2.0D);
    }

    public static TreeGen getCopyGen(TreeGen gen) {
        TreeGen _gen1 = new TreeGen(gen.getM());
        ArrayList<Comparable> arrList = gen.getAllKeyList();
        Iterator var4 = arrList.iterator();

        while(var4.hasNext()) {
            Comparable f1 = (Comparable)var4.next();
            _gen1.insert(f1);
        }

        return _gen1;
    }

    public void setGen(int m, int min, INode inode) {
        this._m = m;
        this._min = min;
        this._rootNode = inode;
    }

    public INode getRootNode() {
        return this._rootNode;
    }

    public boolean insert(Comparable indexNO) {
        TreeLeaf theLeaf;
        int cindex;
        if(this._rootNode.childNodes.size() <= 0) {
            if(!this._rootNode.isLeaf) {
                theLeaf = new TreeLeaf();
                theLeaf.isLeaf = true;
                Iterator var9 = this._rootNode.keys.iterator();

                while(var9.hasNext()) {
                    Comparable keyNO = (Comparable)var9.next();
                    theLeaf.keys.add(keyNO);
                }

                this._rootNode = theLeaf;
            }

            int indexLOC = -1;
            cindex = 0;
            Iterator var11 = this._rootNode.keys.iterator();

            while(true) {
                if(var11.hasNext()) {
                    Comparable f1 = (Comparable)var11.next();
                    if(f1.equals(indexNO)) {
                        return false;
                    }

                    if(indexNO.compareTo(f1) > 0) {
                        indexLOC = cindex;
                    }

                    if(indexNO.compareTo(f1) >= 0) {
                        ++cindex;
                        continue;
                    }
                }

                this._rootNode.keys.add(indexLOC + 1, indexNO);
                this.recurse_division_after_insert(this._rootNode);
                return true;
            }
        } else {
            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO);
            if(theLeaf == null) {
                return false;
            } else {
                cindex = -1;
                cindex = 0;
                Iterator var6 = theLeaf.keys.iterator();

                while(true) {
                    if(var6.hasNext()) {
                        Comparable f1 = (Comparable)var6.next();
                        if(f1.equals(indexNO)) {
                            return false;
                        }

                        if(indexNO.compareTo(f1) > 0) {
                            cindex = cindex;
                        }

                        if(indexNO.compareTo(f1) >= 0) {
                            ++cindex;
                            continue;
                        }
                    }

                    this.insertIndexNO(theLeaf, indexNO);
                    if(cindex == -1) {
                        this.recursion_changeMinimun(theLeaf, indexNO);
                    }

                    this.recurse_division_after_insert(theLeaf);
                    return true;
                }
            }
        }
    }


    public INode search(Comparable indexNO) {
        this._research_result = null;
        this.recursion_to_serach(this._rootNode, indexNO);
        return this._research_result;
    }

    //递归查找, 最终会查找到leaf节点
    private void recursion_to_serach(INode currentNode, Comparable indexNO) {
        if(currentNode != null) {
            int cindex;
            Comparable key;
            Iterator var6;
            if(!currentNode.isLeaf && currentNode.childNodes.size() > 0) {
                int indexLoc = -1;
                cindex = 0;

                for(var6 = currentNode.keys.iterator(); var6.hasNext(); ++cindex) {
                    key = (Comparable)var6.next();
                    if(indexNO.compareTo(key) < 0) {
                        break;
                    }

                    if(indexNO.compareTo(key) >= 0) {
                        indexLoc = cindex;
                    }
                }

                if(indexLoc != -1) {
                    this.recursion_to_serach((INode)currentNode.childNodes.get(indexLoc), indexNO);
                }
            } else {
                boolean indexLoc = true;
                cindex = 0;

                for(Iterator iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {
                    key = (Comparable) iterator.next();
                    if(indexNO.equals(key)) {
                        this._research_result = currentNode;
                        break;
                    }
                }

            }
        }
    }

    private void recursion_changeMinimun(INode currentNode, Comparable indexNO) {
        if(currentNode != null) {
            if(! currentNode.keys.get(0).equals(indexNO)) {
                currentNode.keys.remove(0);
                currentNode.keys.add(0, indexNO);
            }

            this.recursion_changeMinimun(currentNode.parent, indexNO);
        }
    }

    private boolean insertIndexNO(INode currentNode, Comparable indexNO) {
        if(currentNode == null) {
            return false;
        } else {
            int indexLOC = -1;
            int cindex = 0;
            Iterator iterator = currentNode.keys.iterator();

            while(true) {
                if(iterator.hasNext()) {
                    Comparable f1 = (Comparable) iterator.next();
                    if(f1.equals(indexNO)) {
                        return false;
                    }

                    if(indexNO.compareTo(f1) > 0) {
                        indexLOC = cindex;
                    }

                    if(indexNO.compareTo(f1) >= 0) {
                        ++cindex;
                        continue;
                    }
                }

                currentNode.keys.add(indexLOC + 1, indexNO);
                return true;
            }
        }
    }



    private TreeLeaf recursion_search_suitable_leaf(INode currentNode, Comparable indexNO) {
        if(currentNode == null) {
            return null;
        } else if(!currentNode.isLeaf && currentNode.childNodes.size() > 0) {
            int indexLoc = -1;
            int cindex = 0;

            for(Iterator iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {
                Comparable iNO = (Comparable) iterator.next();
                if(indexNO.compareTo(iNO) < 0) {
                    break;
                }

                if(indexNO.compareTo(iNO) > 0) {
                    indexLoc = cindex;
                }

                if(indexNO.equals(iNO)) {
                    return null;
                }
            }

            return indexLoc == -1?this.recursion_getLeftLeaf(currentNode):this.recursion_search_suitable_leaf((INode)currentNode.childNodes.get(indexLoc), indexNO);
        } else {
            return (TreeLeaf)currentNode;
        }
    }

    private TreeLeaf recursion_getLeftLeaf(INode currentNode) {
        return currentNode == null?null:(currentNode.isLeaf?(TreeLeaf)currentNode:(currentNode.childNodes.size() <= 0?null:this.recursion_getLeftLeaf((INode)currentNode.childNodes.get(0))));
    }

    private void recurse_division_after_insert(INode currentNode) {
        if(currentNode.keys.size() > this._m) {
            TreeLeaf currentLeaf = null;
            TreeNode currentNode2 = null;
            INode parentNode = currentNode.parent;
            int theOriginTotal;
            int cLindex;
            TreeNode theRoot;
            if(currentNode.isLeaf) {
                currentLeaf = (TreeLeaf)currentNode;
                TreeLeaf rightLeaf = new TreeLeaf();
                rightLeaf.parent = currentLeaf.parent;
                rightLeaf.isLeaf = true;
                rightLeaf.rightBrother = currentLeaf.rightBrother;
                currentLeaf.rightBrother = rightLeaf;
                int cindex = 0;

                for(Iterator var13 = currentLeaf.keys.iterator(); var13.hasNext(); ++cindex) {
                    Comparable f1 = (Comparable)var13.next();
                    if(cindex >= this._min) {
                        rightLeaf.keys.add(f1);
                        if(currentLeaf.values.size() > cindex) {
                            rightLeaf.values.add(currentLeaf.values.get(cindex));
                        }
                    }
                }

                theOriginTotal = currentLeaf.keys.size();

                for(cLindex = theOriginTotal - 1; cLindex >= this._min; --cLindex) {
                    currentLeaf.keys.remove(cLindex);
                    if(currentLeaf.values.size() > cLindex) {
                        currentLeaf.values.remove(cLindex);
                    }
                }

                if(currentLeaf.parent == null) {
                    theRoot = new TreeNode();
                    theRoot.keys.add((Comparable)currentLeaf.keys.get(0));
                    theRoot.keys.add((Comparable)rightLeaf.keys.get(0));
                    theRoot.childNodes.add(currentLeaf);
                    theRoot.childNodes.add(rightLeaf);
                    currentLeaf.parent = theRoot;
                    rightLeaf.parent = theRoot;
                    this._rootNode = theRoot;
                } else {
                    cLindex = parentNode.childNodes.indexOf(currentNode);
                    parentNode.keys.add(cLindex + 1, (Comparable)rightLeaf.keys.get(0));
                    parentNode.childNodes.add(cLindex + 1, rightLeaf);
                    this.recurse_division_after_insert(parentNode);
                }
            } else {
                currentNode2 = (TreeNode)currentNode;
                TreeNode normalNode = currentNode2;
                TreeNode rightBrother = new TreeNode();
                rightBrother.parent = parentNode;
                theOriginTotal = currentNode2.keys.size();

                for(cLindex = this._min; cLindex <= theOriginTotal - 1; ++cLindex) {
                    rightBrother.keys.add((Comparable)normalNode.keys.get(cLindex));
                    ((INode)normalNode.childNodes.get(cLindex)).parent = rightBrother;
                    rightBrother.childNodes.add((INode)normalNode.childNodes.get(cLindex));
                }

                for(cLindex = theOriginTotal - 1; cLindex >= this._min; --cLindex) {
                    normalNode.childNodes.remove(cLindex);
                    normalNode.keys.remove(cLindex);
                }

                if(parentNode == null) {
                    theRoot = new TreeNode();
                    theRoot.keys.add((Comparable)normalNode.keys.get(0));
                    theRoot.keys.add((Comparable)rightBrother.keys.get(0));
                    theRoot.childNodes.add(normalNode);
                    theRoot.childNodes.add(rightBrother);
                    normalNode.parent = theRoot;
                    rightBrother.parent = theRoot;
                    this._rootNode = theRoot;
                } else {
                    cLindex = parentNode.childNodes.indexOf(normalNode);
                    parentNode.keys.add(cLindex + 1, (Comparable)rightBrother.keys.get(0));
                    parentNode.childNodes.add(cLindex + 1, rightBrother);
                    this.recurse_division_after_insert(parentNode);
                }
            }
        }
    }

    public boolean delete(Comparable indexNO) {
        INode currentNode = this.search(indexNO);
        if(currentNode == null) {
            return false;
        }
        int indexLoc;
        if (currentNode.parent == null && currentNode.childNodes.size() <= 0) {
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            if (indexLoc == -1) {
                return false;
            } else { //走到这里当前的INode为TreeLeaf
                currentNode.keys.remove(indexLoc);
                ((TreeLeaf)currentNode).values.remove(indexLoc);
                return true;
            }
        } else if (currentNode.parent == null && currentNode.childNodes.size() > 0) {
            return false;
        } else if (currentNode.parent != null && currentNode.childNodes.size() > 0) {
            return false;
        } else if (currentNode.childNodes.size() > 0) {
            return false;
        } else if (currentNode.keys.size() > this._min) {
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            if (indexLoc == 0) {
                currentNode.keys.remove(indexLoc);
                if(currentNode instanceof TreeLeaf){
                    ((TreeLeaf)currentNode).values.remove(indexLoc);
                }
                this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                return true;
            } else {
                currentNode.keys.remove(indexLoc);
                if(currentNode instanceof TreeLeaf){
                    ((TreeLeaf)currentNode).values.remove(indexLoc);
                }
                return true;
            }
        } else {
            INode parentNode = currentNode.parent;
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            int cNodePindex = parentNode.childNodes.indexOf(currentNode);
            if (cNodePindex == -1) {
                return false;
            } else {
                INode leftBrother = null;
                INode rightBrother = ((TreeLeaf) currentNode).rightBrother;
                if (cNodePindex > 0) {
                    leftBrother = (INode) parentNode.childNodes.get(cNodePindex - 1);
                }

                if (leftBrother != null && leftBrother.keys.size() > this._min) {
                    currentNode.keys.remove(indexLoc);
                    currentNode.keys.add(0, (Comparable) leftBrother.keys.get(leftBrother.keys.size() - 1));
                    leftBrother.keys.remove(leftBrother.keys.size() - 1);
                    if(currentNode instanceof TreeLeaf){
                        ((TreeLeaf)currentNode).values.remove(indexLoc);
                        ((TreeLeaf)currentNode).values.add(0, ((TreeLeaf)leftBrother).values.get(leftBrother.keys.size() - 1));
                        ((TreeLeaf)leftBrother).values.remove(leftBrother.keys.size() - 1);
                    }
                    this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                    return true;
                } else if (rightBrother != null && rightBrother.keys.size() > this._min) {
                    currentNode.keys.remove(indexLoc);
                    currentNode.keys.add((Comparable) rightBrother.keys.get(0));
                    rightBrother.keys.remove(0);
                    if(currentNode instanceof TreeLeaf){
                        ((TreeLeaf)currentNode).values.remove(indexLoc);
                        ((TreeLeaf)currentNode).values.add((Comparable) rightBrother.keys.get(0));
                        ((TreeLeaf)rightBrother).values.remove(0);
                    }
                    this.recursion_handler_firstOneDelete((INode) null, rightBrother, 0.0F);
                    if (indexLoc == 0) {
                        this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                    }
                    return true;
                } else {
                    Comparable f1;
                    Iterator var9;
                    if (leftBrother != null) {
                        currentNode.keys.remove(indexLoc);
                        if(currentNode instanceof TreeLeaf){
                            ((TreeLeaf)currentNode).values.remove(indexLoc);
                        }
                        if (indexLoc == 0) {
                            this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                        }

                        var9 = currentNode.keys.iterator();

                        while (var9.hasNext()) {
                            f1 = (Comparable) var9.next();
                            leftBrother.keys.add(f1);
                        }

                        ((TreeLeaf) leftBrother).rightBrother = ((TreeLeaf) currentNode).rightBrother;
                        parentNode.keys.remove(cNodePindex);
                        parentNode.childNodes.remove(cNodePindex);
                        this.recursion_combination(parentNode);
                        return true;
                    } else if (rightBrother == null) {
                        return false;
                    } else {
                        currentNode.keys.remove(indexLoc);
                        if(currentNode instanceof TreeLeaf){
                            ((TreeLeaf)currentNode).values.remove(indexLoc);
                        }
                        if (indexLoc == 0) {
                            this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                        }

                        var9 = rightBrother.keys.iterator();

                        while (var9.hasNext()) {
                            f1 = (Comparable) var9.next();
                            currentNode.keys.add(f1);
                        }

                        ((TreeLeaf) currentNode).rightBrother = ((TreeLeaf) rightBrother).rightBrother;
                        parentNode.keys.remove(cNodePindex + 1);
                        parentNode.childNodes.remove(cNodePindex + 1);
                        this.recursion_combination(parentNode);
                        return true;
                    }
                }
            }
        }

    }
