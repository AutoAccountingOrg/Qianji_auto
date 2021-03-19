/*
 * Copyright (C) 2021 dreamn(dream@dreamn.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cn.dreamn.qianji_auto.database.Helper;


import cn.dreamn.qianji_auto.database.DbManger;
import cn.dreamn.qianji_auto.database.Table.Cache;
import cn.dreamn.qianji_auto.utils.runUtils.Task;

public class Caches {

    interface getCacheObj{
        void onGet(Cache cache);
    }
    interface getCaches{
        void onGet(Cache[] cache);
    }
    interface getCacheString{
        void onGet(String cache);
    }
    public static void getOne(String name, String type,getCacheObj getCache) {
        Task.onThread(()->{
            DbManger.db.CacheDao().deleteTimeout();
            Cache[] caches =  DbManger.db.CacheDao().getOne(name, type);
            if (caches.length <= 0) {
                getCache.onGet(null);
                return ;
            }
            getCache.onGet(caches[0]);
        });

    }

    public static void getCacheData(String name,String defaultData,getCacheString getCache) {
        getOne(name, "0",cache1 -> {
            if (cache1 == null) {
                getCache.onGet(defaultData);
                return ;
            }
            getCache.onGet(cache1.cacheData);
        });

    }


    public static void getType(String type,getCaches getCache) {
        Task.onThread(()->getCache.onGet( DbManger.db.CacheDao().getType(type)));
    }


    public static void del(String name) {
        Task.onThread(()->DbManger.db.CacheDao().del(name));
    }

    public static void delBody(String body) {
        Task.onThread(()->DbManger.db.CacheDao().delbody(body));
    }

    public static void add(String name, String data, String type) {
        Task.onThread(()->{
            DbManger.db.CacheDao().deleteTimeout();
            DbManger.db.CacheDao().add(name, data, type);
        });
       // return DbManger.db.CacheDao().add(name, data, type);
    }

    public static void update(String name, String data) {
        Task.onThread(()->DbManger.db.CacheDao().update(name, data));
    }

    public static void Clean() {
        Task.onThread(()->DbManger.db.CacheDao().deleteAll());
    }

    public static void AddOrUpdate(String name, String data) {
        getOne(name, "0",cache -> {
            if(cache!=null){
                update(name, data);
            }else{
                add(name, data, "0");
            }
        });
    }

}
