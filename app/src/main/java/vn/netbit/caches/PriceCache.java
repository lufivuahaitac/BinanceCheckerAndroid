/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.netbit.caches;

import java.util.Map;

import vn.netbit.beans.Bean;
import vn.netbit.utils.Utils;

/**
 *
 * @author truongnq
 */
public class PriceCache {
    private final Map<Integer, Bean> priceHistory = Utils.createLRUMap(1000000);
    
     private static final class SingletonHolder {

        private static final PriceCache INSTANCE = new PriceCache();
    }

    public static PriceCache getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    
    
    public synchronized void set(int key, Bean value){
        priceHistory.put(key, value);
    }
    
    public synchronized Bean get(int key){
        return priceHistory.get(key);
    }
}
