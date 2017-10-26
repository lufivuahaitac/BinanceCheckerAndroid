/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.netbit.utils;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author truongnq
 */
public class Utils {


    public static <K, V> Map<K, V> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<K, V>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }
}
