# <h1 align="center">Tracks</h1>

<h3 align="center">Kotlin 编写的 B 站视频下载器</h3>

<!--Badges-->

<p align="center">
<a href="https://kotlinlang.org"><img 
src="https://img.shields.io/badge/Kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white" 
alt="Kotlin"/></a><a 
href="https://gradle.org/"><img 
src="https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white" 
alt="Gradle"/></a><a 
href="https://www.jetbrains.com/idea/"><img 
src="https://img.shields.io/badge/IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white" 
alt="IntelliJ IDEA"/></a>
</p>

<p align="center">
<a 
href="https://opensource.org/licenses/MIT"><img 
src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge&logo=opensourceinitiative&logoColor=white" 
alt="MIT License"/></a><a 
href="https://github.com/Colerar/Tracks/releases"><img 
src="https://img.shields.io/github/v/release/Colerar/Tracks?style=for-the-badge" 
alt="Maven Developer"/></a></p>

![video-info](https://user-images.githubusercontent.com/62297254/154002674-83fb713f-0e26-4527-af5d-452e47eb7a5e.jpg)

<details>
<summary>演示视频</summary>

https://user-images.githubusercontent.com/62297254/154002139-1ac70e9f-0d05-4e9e-bd52-41460a676656.mp4

</details>

## 安装

See: [INSTALL.md](docs/_INSTALL.md)

## 功能列表 & TODO

- 封装
  - [X] 使用 FFmpeg cli 转封装
- 下载
  - [X] 普通视频下载
  - [X] PGC 视频下载
  - [ ] 互动视频下载
  - [X] 指定分 P
  - [X] 断点续传 
- 直播流获取
  - [X] 通过房间号
  - [ ] 通过主播 UID
  - [X] 直接唤醒播放器
- Misc
  - [ ] 弹幕 to ASS
  - [X] 字幕 to SRT
  - [X] 繁化姬集成
  - [X] 自定义输出文件名 - [详情](docs/custom-output.md)
