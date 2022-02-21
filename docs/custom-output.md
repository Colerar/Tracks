# 自定义文件名输出

可以通过 `tracks config name-xxx` 来设置输出文件名.

1. 使用 `tracks config list` 查看所有配置项, 若是 `macOS` `linux` 可配合 `grep` 筛选:

```shell
tracks config list | grep name-
```

输出示例:

```shell
name-cover           - 封面名称样式
name-video           - 视频名称样式
name-audio           - 音频名称样式
name-subtitle        - 字幕名称样式
name-final           - 混流后的名称样式
```

2. 使用 `tracks config name-xxx` 查询具体项目的值, 例如:

```shell
➜  ~ tracks config name-cover,name-video,name-audio,name-subtitle,name-final
当前封面名称样式：%date%-%video:title%.png
当前视频名称样式：%date%-%video:title%-%part:num%.m4v
当前音频名称样式：%date%-%video:title%-%part:num%.m4a
当前字幕名称样式：%date%-%video:title%-%part:num%.%subtitle:lang%.srt
当前混流后的名称样式：%date%-%video:title%-%part:num%.mp4
```

3. 使用 `tracks config name-xxx=yyy` 设置项目, 例如

```shell
➜  ~ tracks config "name-cover=%date%-%video:title%.png"
封面名称样式设置为：%date%-%video:title%.png
```

其中形如 `%xxx%` 即为 Tracks 提供的变量占位符.

## 可用变量

### 通用

|   占位符    |  名称   | 简介                                |
|:--------:|:-----:|:----------------------------------|
|   year   |   年   | 公元年, 如 `2021`                     |
|  month   |   月   | 一年的第几月, 填充至两位, 如 `01` `08` `12`   |
|   day    |   日   | 一月的第几日, 填充至两位, 如 `08` `15` `29`   |
|   hour   |   时   | 二十四小时制的时, 填充至两位, 如 `01` `15` `23` |
|  minute  |   分   | 一小时的第几分, 填充至两位, 如 `05` `40` `59`  |
|  second  |   秒   | 一分钟的第几秒, 填充至两位, 如 `05` `20` `40`  |
|   date   |  日期   | 如 `2022-01-14`, 前几项的组合            |
| timedate | 时间+日期 | 如 `2022-01-14 05:14:19`, 前几项的组合   |

> 以上全为下载时的时间

### 分 P 及剧集

|        占位符        |      名称       | 简介                                                                 |
|:-----------------:|:-------------:|--------------------------------------------------------------------|
|     part:num      | 分 P 号码 / 剧集序号 | 分 P 号码从 `1` 开始, 剧集可能包含小数, 如 `11.5`                                 |
|    part:title     |    分 P 标题     |                                                                    |
|   part:duration   |    分 P 时长     | 时:分:秒, 分和秒会填充 2 位 `0`, 若不足一小时不会显示小时, 如 `00:03`, `12:02`, `1:00:00` |
| part:sec-duration |    分 P 时长     | 以秒表示的时长, 如 `114514`                                                |
|     part:cid      |    分 P cid    | 分 P cid 如 `1919810`                                                |

### 视频信息

|      占位符       |  名称   | 简介                                                              |
|:--------------:|:-----:|-----------------------------------------------------------------|
|    video:id    | 视频 id | 普通视频: 形如 `BVrac123AS` 的 BV 号; PGC内容(番剧等): 形如 `ss123123` 的 ss 号码 |
|   video:type   |  类型   | 普通视频: 视频分区; PGC: PGC类型, 如`番剧`, `综艺`, `纪录片`等                     |
| video:duration | 视频时长  | 和 `part:duration` 类似, 但是, 是所有分 P/剧集的总和                          |
|  video:title   |  标题   | 视频标题 / 季度标题                                                     |
|  video:author  |  作者   | 视频作者                                                            |
|   video:date   | 发布日期  |                                                                 |

### 视频 / 音频轨

|       占位符       |  名称   | 简介                                                               |
|:---------------:|:-----:|------------------------------------------------------------------|
|   track:codec   |  编码   | 对于视频可能为 `[HEVC, AVC, AV1]`, 对于音频只有 `[AUDIO]`                     |
|  track:quality  |  质量   | 对于视频: 分辨率, 如 `V1080P`, `V8K`等; 对于音频 `[LOW, MEDIUM, HIGH, DOLBY]` |
| track:framerate |  帧速率  | 对应的视频帧速率, 如 `24` `30` `60` 等                                     |

### 字幕轨

| 占位符               | 名称   | 简介                    |
|-------------------|------|-----------------------|
| subtitle:lang     | 语言代码 | 如 `zh-Hans`, `en-US`  |
| subtitle:langName | 语言名称 | 以中文表示的语言名称, 如`中文(简体)` |
