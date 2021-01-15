package com.cowbi.spqrrow.java.whatsNew;


public class InterfaceNewImpl implements InterfaceNew , InterfaceNew1{

    public static void main(String[] args) {
        InterfaceNewImpl interfaceNew = new InterfaceNewImpl();
        interfaceNew.def();
    }
    @Override
    public void def() {
        InterfaceNew1.super.def();
    }

    @Override
    public void f() {
    }
}
