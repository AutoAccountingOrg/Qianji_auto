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

package cn.dreamn.qianji_auto.data.database.Table;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Regular {
    //自动分类规则
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String regular;//翻译后的规则
    public String name;//名称
    public String remark;//说明
    public String identify;//category/sms/app/notice/helper
    public String app;//
    public String data;//规则在编辑页面中的数据
    public String dataId;//云端ID
    public String version;//版本号

    public int use = 1;//是否启用该规则
    public int sort = 0;//排序

}
