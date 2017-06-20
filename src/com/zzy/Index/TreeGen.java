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

        ArrayList arrList = gen.getAllKeyList();

        Iterator iterator = arrList.iterator();



        while(iterator.hasNext()) {

            Comparable key = (Comparable)iterator.next();

            _gen1.insert(key);

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

        int indexLOC;

        if(this._rootNode.childNodes.size() <= 0) {

            if(!this._rootNode.isLeaf) {

                theLeaf = new TreeLeaf();

                theLeaf.isLeaf = true;

                Iterator iterator = this._rootNode.keys.iterator();



                while(iterator.hasNext()) {

                    Comparable var8 = (Comparable)iterator.next();

                    theLeaf.keys.add(var8);

                }



                this._rootNode = theLeaf;

            }



            int var7 = -1;

            indexLOC = 0;

            Iterator iterator = this._rootNode.keys.iterator();



            while(true) {

                if(iterator.hasNext()) {

                    Comparable var10 = (Comparable)iterator.next();

                    if(var10.equals(indexNO) ){

                        return false;

                    }



                    if(indexNO.compareTo(var10) > 0) {

                        var7 = indexLOC;

                    }



                    if(indexNO.compareTo(var10) >= 0) {

                        ++indexLOC;

                        continue;

                    }

                }



                this._rootNode.keys.add(var7 + 1, indexNO);

                this.recurse_division_after_insert(this._rootNode);

                return true;

            }

        } else {

            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO);

            if(theLeaf == null) {

                return false;

            } else {

                indexLOC = -1;

                int cindex = 0;

                Iterator iterator = theLeaf.keys.iterator();



                while(true) {

                    if(iterator.hasNext()) {

                        Comparable f1 = (Comparable)iterator.next();

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



                    this.insertIndexNO(theLeaf, indexNO);

                    if(indexLOC == -1) {

                        this.recursion_changeMinimun(theLeaf, indexNO);

                    }



                    this.recurse_division_after_insert(theLeaf);

                    return true;

                }

            }

        }

    }



    public boolean insert(Comparable indexNO, Object value) {

        TreeLeaf theLeaf;

        int indexLOC;

        if(this._rootNode.childNodes.size() <= 0) {

            if(!this._rootNode.isLeaf) {

                theLeaf = new TreeLeaf();

                theLeaf.isLeaf = true;

                Iterator iterator = this._rootNode.keys.iterator();



                while(iterator.hasNext()) {

                    Comparable var8 = (Comparable)iterator.next();

                    theLeaf.keys.add(var8);

                }



                this._rootNode = theLeaf;

            }



            int var7 = -1;

            indexLOC = 0;

            Iterator iterator = this._rootNode.keys.iterator();



            while(true) {

                if(iterator.hasNext()) {

                    Comparable var10 = (Comparable)iterator.next();

                    if(var10.equals(indexNO) ){

                        return false;

                    }



                    if(indexNO.compareTo(var10) > 0) {

                        var7 = indexLOC;

                    }



                    if(indexNO.compareTo(var10) >= 0) {

                        ++indexLOC;

                        continue;

                    }

                }



                this._rootNode.keys.add(var7 + 1, indexNO);

                ((TreeLeaf)this._rootNode).values.add(var7 + 1, value);

                this.recurse_division_after_insert(this._rootNode);

                return true;

            }

        } else {

            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO);

            if(theLeaf == null) {

                return false;

            } else {

                indexLOC = -1;

                int cindex = 0;

                Iterator iterator = theLeaf.keys.iterator();



                while(true) {

                    if(iterator.hasNext()) {

                        Comparable f1 = (Comparable)iterator.next();

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



                    this.insertIndexNO(theLeaf, indexNO, value);

                    if(indexLOC == -1) {

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



    private void recursion_to_serach(INode currentNode, Comparable indexNO) {

        if(currentNode != null) {

            int cindex;

            Comparable key;

            Iterator iterator;

            if(!currentNode.isLeaf && currentNode.childNodes.size() > 0) {

                int var7 = -1;

                cindex = 0;



                for(iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {

                    key = (Comparable) iterator.next();

                    if(indexNO.compareTo(key) < 0) {

                        break;

                    }



                    if(indexNO.compareTo(key ) >= 0) {

                        var7 = cindex;

                    }

                }



                if(var7 != -1) {

                    this.recursion_to_serach((INode)currentNode.childNodes.get(var7), indexNO);

                }

            } else {

                boolean indexLoc = true;

                cindex = 0;



                for(iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {

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

    private boolean insertIndexNO(INode currentNode, Comparable indexNO, Object value) {

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

                ((TreeLeaf)currentNode).values.add(indexLOC+1, value);

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

            int originTotal;

            int cLindex;

            TreeNode var12;

            if(currentNode.isLeaf) {

                currentLeaf = (TreeLeaf)currentNode;

                TreeLeaf var9 = new TreeLeaf();

                var9.parent = currentLeaf.parent;

                var9.isLeaf = true;

                var9.rightBrother = currentLeaf.rightBrother;

                currentLeaf.rightBrother = var9;

                int var10 = 0;



                for(Iterator iterator = currentLeaf.keys.iterator(); iterator.hasNext(); ++var10) {

                    Comparable var11 = (Comparable) iterator.next();

                    if(var10 >= this._min) {

                        var9.keys.add(var11);

                        if(currentLeaf.values.size() > var10) {

                            var9.values.add(currentLeaf.values.get(var10));

                        }

                    }

                }



                originTotal = currentLeaf.keys.size();



                for(cLindex = originTotal - 1; cLindex >= this._min; --cLindex) {

                    currentLeaf.keys.remove(cLindex);

                    if(currentLeaf.values.size() > cLindex) {

                        currentLeaf.values.remove(cLindex);

                    }

                }



                if(currentLeaf.parent == null) {

                    var12 = new TreeNode();

                    var12.keys.add(currentLeaf.keys.get(0));

                    var12.keys.add(var9.keys.get(0));

                    var12.childNodes.add(currentLeaf);

                    var12.childNodes.add(var9);

                    currentLeaf.parent = var12;

                    var9.parent = var12;

                    this._rootNode = var12;

                } else {

                    cLindex = parentNode.childNodes.indexOf(currentNode);

                    parentNode.keys.add(cLindex + 1, var9.keys.get(0));

                    parentNode.childNodes.add(cLindex + 1, var9);

                    this.recurse_division_after_insert(parentNode);

                }

            } else {

                currentNode2 = (TreeNode)currentNode;

                TreeNode normalNode = currentNode2;

                TreeNode rightBrother = new TreeNode();

                rightBrother.parent = parentNode;

                originTotal = currentNode2.keys.size();



                for(cLindex = this._min; cLindex <= originTotal - 1; ++cLindex) {

                    rightBrother.keys.add(normalNode.keys.get(cLindex));

                    ((INode)normalNode.childNodes.get(cLindex)).parent = rightBrother;

                    rightBrother.childNodes.add((INode)normalNode.childNodes.get(cLindex));

                }



                for(cLindex = originTotal - 1; cLindex >= this._min; --cLindex) {

                    normalNode.childNodes.remove(cLindex);

                    normalNode.keys.remove(cLindex);

                }



                if(parentNode == null) {

                    var12 = new TreeNode();

                    var12.keys.add(normalNode.keys.get(0));

                    var12.keys.add(rightBrother.keys.get(0));

                    var12.childNodes.add(normalNode);

                    var12.childNodes.add(rightBrother);

                    normalNode.parent = var12;

                    rightBrother.parent = var12;

                    this._rootNode = var12;

                } else {

                    cLindex = parentNode.childNodes.indexOf(normalNode);

                    parentNode.keys.add(cLindex + 1, rightBrother.keys.get(0));

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

        } else {

            int parentNode1;

            if(currentNode.parent == null && currentNode.childNodes.size() <= 0) {

                parentNode1 = this.getIndexLocation(currentNode, indexNO);

                if(parentNode1 == -1) {

                    return false;

                } else {

                    currentNode.keys.remove(parentNode1);

                    return true;

                }

            } else if(currentNode.parent == null && currentNode.childNodes.size() > 0) {

                return false;

            } else if(currentNode.parent != null && currentNode.childNodes.size() > 0) {

                return false;

            } else if(currentNode.childNodes.size() > 0) {

                return false;

            } else if(currentNode.keys.size() > this._min) {

                parentNode1 = this.getIndexLocation(currentNode, indexNO);

                if(parentNode1 == 0) {

                    currentNode.keys.remove(parentNode1);

                    this.recursion_handler_firstOneDelete((INode)null, currentNode, 0.0F);

                    return true;

                } else {

                    currentNode.keys.remove(parentNode1);

                    return true;

                }

            } else {

                INode parentNode = currentNode.parent;

                int indexLoc = this.getIndexLocation(currentNode, indexNO);

                int cNodePindex = parentNode.childNodes.indexOf(currentNode);

                if(cNodePindex == -1) {

                    return false;

                } else {

                    INode leftBrother = null;

                    TreeLeaf rightBrother = ((TreeLeaf)currentNode).rightBrother;

                    if(cNodePindex > 0) {

                        leftBrother = (INode)parentNode.childNodes.get(cNodePindex - 1);

                    }



                    if(leftBrother != null && leftBrother.keys.size() > this._min) {

                        currentNode.keys.remove(indexLoc);

                        currentNode.keys.add(0, leftBrother.keys.get(leftBrother.keys.size() - 1));

                        leftBrother.keys.remove(leftBrother.keys.size() - 1);

                        this.recursion_handler_firstOneDelete((INode)null, currentNode, 0.0F);

                        return true;

                    } else if(rightBrother != null && rightBrother.keys.size() > this._min) {

                        currentNode.keys.remove(indexLoc);

                        currentNode.keys.add(rightBrother.keys.get(0));

                        rightBrother.keys.remove(0);

                        this.recursion_handler_firstOneDelete((INode)null, rightBrother, 0.0F);

                        if(indexLoc == 0) {

                            this.recursion_handler_firstOneDelete((INode)null, currentNode, 0.0F);

                        }



                        return true;

                    } else {

                        Comparable f1;

                        Iterator iterator;

                        if(leftBrother != null) {

                            currentNode.keys.remove(indexLoc);

                            if(indexLoc == 0) {

                                this.recursion_handler_firstOneDelete((INode)null, currentNode, 0.0F);

                            }



                            iterator = currentNode.keys.iterator();



                            while(iterator.hasNext()) {

                                f1 = (Comparable) iterator.next();

                                leftBrother.keys.add(f1);

                            }



                            ((TreeLeaf)leftBrother).rightBrother = ((TreeLeaf)currentNode).rightBrother;

                            parentNode.keys.remove(cNodePindex);

                            parentNode.childNodes.remove(cNodePindex);

                            this.recursion_combination(parentNode);

                            return true;

                        } else if(rightBrother == null) {

                            return false;

                        } else {

                            currentNode.keys.remove(indexLoc);

                            if(indexLoc == 0) {

                                this.recursion_handler_firstOneDelete((INode)null, currentNode, 0.0F);

                            }



                            iterator = rightBrother.keys.iterator();



                            while(iterator.hasNext()) {

                                f1 = (Comparable) iterator.next();

                                currentNode.keys.add(f1);

                            }



                            ((TreeLeaf)currentNode).rightBrother = ((TreeLeaf)rightBrother).rightBrother;

                            parentNode.keys.remove(cNodePindex + 1);

                            parentNode.childNodes.remove(cNodePindex + 1);

                            this.recursion_combination(parentNode);

                            return true;

                        }

                    }

                }

            }

        }

    }



    private void recursion_handler_after_deletion(INode curretNode) {

    }



    private void recursion_handler_firstOneDelete(INode childNode, INode currentNode, Comparable firstIndexNO) {

        if(currentNode != null) {

            INode parentNode = currentNode.parent;

            if(currentNode.isLeaf) {

                if(parentNode != null) {

                    Comparable childIndexLoc1 = currentNode.keys.get(0);

                    parentNode.childNodes.indexOf(currentNode);

                    this.recursion_handler_firstOneDelete(currentNode, parentNode, childIndexLoc1);

                }



            } else {

                int childIndexLoc = currentNode.childNodes.indexOf(childNode);

                if(childIndexLoc != -1) {

                    if(!currentNode.keys.get(childIndexLoc).equals(firstIndexNO)) {

                        Comparable cIndexNO;

                        if(childIndexLoc > 0) {

                            currentNode.keys.remove(childIndexLoc);

                            currentNode.keys.add(childIndexLoc, firstIndexNO);

                            if(parentNode != null) {

                                cIndexNO = currentNode.keys.get(0);

                                this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);

                            }



                        } else if(childIndexLoc == 0) {

                            currentNode.keys.remove(0);

                            currentNode.keys.add(0, firstIndexNO);

                            cIndexNO = currentNode.keys.get(0);

                            this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);

                        }

                    }

                }

            }

        }

    }



    private int getIndexLocation(INode currentNode, Comparable indexNO) {

        byte indexLoc = -1;

        if(currentNode == null) {

            return indexLoc;

        } else {

            int cindex = 0;



            for(Iterator var6 = currentNode.keys.iterator(); var6.hasNext(); ++cindex) {

                Comparable f1 = (Comparable) var6.next();

                if(f1 == indexNO) {

                    break;

                }

            }



            return cindex;

        }

    }



    private void recursion_combination(INode currentNode) {

        if(currentNode != null) {

            INode parentNode = currentNode.parent;

            if(currentNode.keys.size() < this._min) {

                if(currentNode.keys.size() == 1 && parentNode == null) {

                    this._rootNode = (INode)currentNode.childNodes.get(0);

                    this._rootNode.parent = null;

                } else if(parentNode != null || currentNode.keys.size() < 2) {

                    INode leftBrother = null;

                    INode rightBrother = null;

                    int theCPindex = parentNode.childNodes.indexOf(currentNode);

                    if(theCPindex != -1) {

                        if(theCPindex == 0) {

                            rightBrother = (INode)parentNode.childNodes.get(1);

                        } else if(theCPindex == parentNode.childNodes.size() - 1) {

                            leftBrother = (INode)parentNode.childNodes.get(theCPindex - 1);

                        } else {

                            leftBrother = (INode)parentNode.childNodes.get(theCPindex - 1);

                            rightBrother = (INode)parentNode.childNodes.get(theCPindex + 1);

                        }



                        if(leftBrother != null && leftBrother.keys.size() > this._min) {

                            currentNode.keys.add(0, leftBrother.keys.get(leftBrother.keys.size() - 1));

                            currentNode.childNodes.add(0, (INode)leftBrother.childNodes.get(leftBrother.childNodes.size() - 1));

                            ((INode)currentNode.childNodes.get(0)).parent = currentNode;

                            leftBrother.keys.remove(leftBrother.keys.size() - 1);

                            leftBrother.childNodes.remove(leftBrother.childNodes.size() - 1);

                            parentNode.keys.remove(theCPindex);

                            parentNode.keys.add(theCPindex, currentNode.keys.get(0));

                        } else if(rightBrother != null && rightBrother.keys.size() > this._min) {

                            currentNode.keys.add(rightBrother.keys.get(0));

                            currentNode.childNodes.add((INode)rightBrother.childNodes.get(0));

                            ((INode)currentNode.childNodes.get(currentNode.childNodes.size() - 1)).parent = currentNode;

                            rightBrother.keys.remove(0);

                            rightBrother.childNodes.remove(0);

                            parentNode.keys.remove(theCPindex + 1);

                            parentNode.keys.add(theCPindex + 1, rightBrother.keys.get(0));

                        } else {

                            Comparable tmpNode;

                            Iterator iterator;

                            INode tmpNode1;

                            if(leftBrother != null) {

                                iterator = currentNode.keys.iterator();



                                while(iterator.hasNext()) {

                                    tmpNode = (Comparable)iterator.next();

                                    leftBrother.keys.add(tmpNode);

                                }



                                iterator = currentNode.childNodes.iterator();



                                while(iterator.hasNext()) {

                                    tmpNode1 = (INode)iterator.next();

                                    tmpNode1.parent = leftBrother;

                                    leftBrother.childNodes.add(tmpNode1);

                                }



                                parentNode.keys.remove(theCPindex);

                                parentNode.childNodes.remove(theCPindex);

                                this.recursion_combination(parentNode);

                            } else if(rightBrother != null) {

                                iterator = rightBrother.keys.iterator();



                                while(iterator.hasNext()) {

                                    tmpNode = (Comparable) iterator.next();

                                    currentNode.keys.add(tmpNode);

                                }



                                iterator = rightBrother.childNodes.iterator();



                                while(iterator.hasNext()) {

                                    tmpNode1 = (INode)iterator.next();

                                    tmpNode1.parent = currentNode;

                                    currentNode.childNodes.add(tmpNode1);

                                }



                                parentNode.keys.remove(theCPindex + 1);

                                parentNode.childNodes.remove(theCPindex + 1);

                                this.recursion_combination(parentNode);

                            }

                        }

                    }

                }

            }

        }

    }



    private void recursion_search_first_leaf(INode currNode) {

        if(currNode != null) {

            if(currNode.isLeaf) {

                this._leaf_tmp = (TreeLeaf)currNode;

            } else if(currNode.childNodes.size() > 0) {

                this.recursion_search_first_leaf((INode)currNode.childNodes.get(0));

            }

        }

    }



    public TreeLeaf getFirstLeaf() {

        this._leaf_tmp = null;

        this.recursion_search_first_leaf(this._rootNode);

        return this._leaf_tmp;

    }



    public ArrayList<Comparable> getAllKeyList() {

        ArrayList flist = new ArrayList();



        for(TreeLeaf fLeaf = this.getFirstLeaf(); fLeaf != null; fLeaf = fLeaf.rightBrother) {

            Iterator iterator = fLeaf.keys.iterator();



            while(iterator.hasNext()) {

                Comparable f2 = (Comparable)iterator.next();

                flist.add(f2);

            }

        }



        return flist;

    }

}
