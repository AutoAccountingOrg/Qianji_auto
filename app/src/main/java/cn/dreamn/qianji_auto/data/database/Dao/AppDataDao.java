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

package cn.dreamn.qianji_auto.data.database.Dao;

import androidx.room.Dao;
import androidx.room.Query;

import cn.dreamn.qianji_auto.data.database.Table.AppData;

@Dao
public interface AppDataDao {
    @Query("SELECT * FROM AppData WHERE identify=:identify  order by id desc limit :from,:to")
    AppData[] loadAll(String identify, int from, int to);

    @Query("INSERT INTO AppData(rawData,identify,fromApp,time) values(:rawData,:identify,:fromApp,strftime('%s','now'))")
    void add(String rawData, String identify, String fromApp);

    @Query("DELETE FROM AppData WHERE id=:pos")
    void del(int pos);

    @Query("DELETE FROM AppData WHERE identify=:identify")
    void delAll(String identify);

    @Query("DELETE FROM AppData WHERE id not in (SELECT id FROM AppData order by id DESC limit :limit)")
    void delTooMore(int limit);
}

