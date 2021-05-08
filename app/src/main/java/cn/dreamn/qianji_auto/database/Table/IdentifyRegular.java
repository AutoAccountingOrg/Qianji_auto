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

package cn.dreamn.qianji_auto.database.Table;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class IdentifyRegular {
    //其他信息处理规则
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String regular;//翻译后的规则
   // public String num;//remark|account|type|money|num
    public String name;//规则名
    public String text;//测试文本

/*
    public String account1;//账户名称1
    public String account2;//账户名称2
    public String type;//收支类型
    public String silent;//是否为后台交易
    public String money;//金额
    public String fee;//手续费
    public String shopName;//商户名称
    public String shopRemark;//商户备注
    public String source;//账单来源*/
public String tableList;
    public String identify;//规则属于APP、短信、通知
    public String fromApp;//规则匹配具体APP

    public int use = 1;//是否启用该规则

    public int sort = 0;//排序
}