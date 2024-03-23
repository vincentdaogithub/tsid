package com.vincentdao;

import com.vincentdao.tsid.Tsid;

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args ) {
        System.out.println("Start");
        for (int i = 0; i < 10000; i++) {
            System.out.println(Tsid.Factory.getInstance().generate());
        }
    }
}
