# FreedomPlus

[![](https://img.shields.io/github/v/release/GangJust/FreedomPlus)](https://github.com/GangJust/FreedomPlus/releases) [![](https://img.shields.io/github/downloads/GangJust/FreedomPlus/total?color=g)]() [![](https://img.shields.io/badge/telegram-freedom%2B-2481CC)](https://t.me/FreedomPlugin)

依赖于抖音运行的开源Xposed模块，Lsposed正常使用，其他框架自测。

需要抖音**文件读写权限**读取模块配置，另外如果频繁崩溃，可尝试更换64位抖音。

已知网络上下载的**抖音历史版本**大多数都是32位。



## 功能介绍

- 视频无水印下载
- 评论区视频/图片保存
- 表情包保存
- 首页控件半透明防烧屏
- 首页清爽模式隐藏大部分控件
- 顶部Tab栏自定义隐藏
- 聊天消息防撤回
- 禁用双击点赞
- 双击打开评论区
- 全屏沉浸式播放
- 移除底部加号按钮
- 视频过滤(直播、广告、长视频、文案关键字等)



## 支持版本

- 理论上支持：28.0.0 ~ 至今 (28.0.0以下可能有兼容问题，请使用1.2.8之前的版本)
- 版本号**参考**列表：[version.json](https://github.com/GangJust/FreedomPlus/blob/master/versions.json)



## 模块下载

前往以下任意地址进行下载

- [FreedomPlus Release](https://github.com/GangJust/FreedomPlus/releases/latest)
- [Xposed Modules Repository Release](https://github.com/Xposed-Modules-Repo/com.freegang.fplus/releases/latest)



## 开源库/Public Library

注：删除线表示依赖被引用，但项目未使用或曾经使用

- [dexkit](https://github.com/LuckyPray/DexKit)
- [mmkv](https://github.com/Tencent/MMKV)
- [sardine-android](https://github.com/thegrizzlylabs/sardine-android)
- ~~[okhttp3](https://github.com/square/okhttp)~~
- ~~[SpannableStringDslExtension](https://github.com/junerver/SpannableStringDslExtension#spannablestringdslextension)~~  



## 重要说明

- 本项目作为一个工具，代码开源，供开发者学习参考使用；
- 开发人员可能会在任何时间**停止更新**或**删除项目**源码，同时也欢迎issues、pull requests；
- 请遵守开源协议对本项目代码的复制修改并开源，若认为代码内容存在不当请邮件联系删除；
- **请、并且：项目代码/功能及其衍生禁止用于非法用途，否则将立即终止本项目的更新。**



## Licenses

[GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html)

```
Copyright (C) 2023  Gang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

