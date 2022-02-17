# 安装教程

**运行要求**

- 任何能运行 JVM 17 的系统 (`Windows` `macOS` `linux` etc.)

## 下载 JRE 17

访问 [Azul](https://www.azul.com/downloads/?package=jre), 下载 Open JRE.

<img width="100%" alt="image" src="https://user-images.githubusercontent.com/62297254/154425265-a8d48bed-39e7-4053-a633-945068460a1c.png">

根据系统选择版本, 此处以 Windows 64 bit 为例, 记得一定要选择 **Java 17 (LTS)**, 下载 `.msi` 文件, 安装即可.

## 下载 Tracks

第二步, 到 [Release 页面](https://github.com/Colerar/Tracks/releases), 下载最新版本的 Tracks.

<img width="60%" alt="image" src="https://user-images.githubusercontent.com/62297254/154423712-f4dfe0d8-64a1-4245-992a-85af13a1000b.png">

> `.zip` `.7z` `.tar` 等后缀名仅仅是压缩格式的区别。

若是 Windows 用户, 可以直接下载 `with-ffmpeg-win-x86_64-gpl` 版本, 跳过下一步.

## 安装 FFmpeg (可选)

可访问 [FFmpeg 官网](https://www.ffmpeg.org/download.html) 根据系统下载即可.

也可通过包管理器, 例如 `apt` `yum` `brew` `choco` 等安装.


<details>
<summary>Windows</summary>

可通过 `choco` 安装 `ffmpeg`.

安装 `choco` (若无):

1. **管理员模式**启动 `Powershell`
   - 可 Win + X, 找到 `Windows PowerShell(管理员)`
   - 或, Win 键菜单下搜索 `powershell`, 右键`以管理员身份运行`.
   
     <img width="40%" alt="image" src="https://user-images.githubusercontent.com/62297254/154429678-d85f1311-4987-4b95-a0cf-d84413ff1922.png">
2. 运行 `Get-ExecutionPolicy`, 若返回 `Restricted` 则运行 `Set-ExecutionPolicy AllSigned` 或 `Set-ExecutionPolicy Bypass -Scope Process`
3. 运行 `Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))`

安装: `choco install ffmpeg`

</details>

<details>
<summary>macOS</summary>

可通过 `brew` 安装 `ffmpeg`.

安装 `brew` (若无):

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
```

若速度不佳, 可参考[中科大镜像文档](https://mirrors.ustc.edu.cn/help/brew.git.html), 切换至国内镜像.

安装: ```brew install ffmpeg```

</details>

<details>
<summary>Linux</summary>

**都用 Linux 了, 自己折腾!!!!! 不写啦!**

</details>

## 安装 Windows Terminal (可选)

> 如果您使用 Windows, 为了更好的使用体验, 强烈建议您, 不要将本程序运行于默认 cmd / powershell,
> 
> 推荐使用 Windows Terminal 等现代终端

通过官方地址在 [Microsoft Store](https://aka.ms/terminal) 下载.

安装后打开即可:

<img width="60%" alt="image" src="https://user-images.githubusercontent.com/62297254/154430989-7d4e7d44-ee11-43c3-84fd-d288a5ea52bb.png">

可自行通过 `Windows Terminal 美化` 等关键词, 自行配置.

## 运行 Tracks

`cd` 到对应目录, 或将安装目录设置为环境变量.

输入 `tracks` 或 `tracks.bat`(Windows) 即可

<img width="50%" alt="image" src="https://user-images.githubusercontent.com/62297254/154432092-41752a3c-223a-4c95-84a3-30a96f0da71c.png">

如图输出即为正常.

若提示未找到 `FFmpeg` 路径, 可通过 `tracks config ffmpeg="路径名"` 配置:

<img width="50%" alt="image" src="https://user-images.githubusercontent.com/62297254/154432414-edc4dd40-df02-4c57-8966-1cfcfc139da8.png">
