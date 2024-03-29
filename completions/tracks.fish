# Command completion for tracks
# Generated by Clikt


### Setup for tracks
set -l tracks_subcommands 'dig live login config version'

## Options for tracks
complete -c tracks -n "not __fish_seen_subcommand_from $tracks_subcommands" -s g -o generate-completion -d '为 bash|zsh|fish 生成补全文件'
complete -c tracks -n "not __fish_seen_subcommand_from $tracks_subcommands" -s h -l help -d 'Show this message and exit'


### Setup for dig
complete -c tracks -f -n __fish_use_subcommand -a dig -d '下载命令'

## Options for dig
complete -c tracks -n "__fish_seen_subcommand_from dig" -s v -o video -o vn -o no-video -d '是否下载视频, 默认下载'
complete -c tracks -n "__fish_seen_subcommand_from dig" -s a -o audio -o an -o no-audio -d '是否下载音频, 默认下载'
complete -c tracks -n "__fish_seen_subcommand_from dig" -s s -o subtitle -o sn -o no-subtitle -d '是否下载字幕, 默认下载'
complete -c tracks -n "__fish_seen_subcommand_from dig" -s c -o cover -o cn -o no-cover -d '是否下载封面, 默认下载'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o vo -o ao -o so -o co -o only-video -o only-audio -o only-subtitle -o only-cover -d '仅下载特定类型, 该项优先级最高'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o no-down -o only-info -o nd -o oi -d '仅输出信息, 不下载'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o multipart -o mt -r -d '下载分块数, 默认不分块'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o qe -o qn -o qnd -o quality-exact -o quality-near -o quality-near-down -d '质量匹配模式, 可选精确或临近模式, 默认向上临近'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o qv -o video-quality -r -d '视频质量, 支持 360P 到 8K, 可搭配 -quality-xxx 使用, 可用选项: [240p,360p,480p,720p,720p60f,1080p,1080plus,1080p60f,4k,hdr,dolby,8k]'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o videocodec -o codec -o cv -r -d '视频编码优先级, 默认 [avc, hevc, av1], 可用 [avc,hevc,av1,h264,h265,h.264,h.265]'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o qa -o audio-quality -r -d '音频质量, 支持 64kbps - 320kbps 以及 dolby e-ac-3, 可搭配 -quality-xxx使用, 可用选项: low, medium, high, dolby, flac'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o sub-lang -o sl -r -d '要下载的字幕的语言代码(如 zh-hant, zh-hans), 默认中文, 可指定多个, all 为全部'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o sub-loose -o sub-loose-match -o slm -o sub-strict -o ss -d '是否开启宽松模式, 不仅将匹配语言代码, 同时也匹配语言名称, 并且仅要求匹配项包含, 而非相等, 默认开启'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o sub-weird -o sw -o sub-fallback -o sf -d '字幕贪婪 / 回退匹配模式, 默认回退, 前者将根据指定的顺序选定, 至多选择一个; 后者将下载所有匹配的字幕'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o zhconvert-enable -o ze -o zhconvert-disable -o zd -d '是否使用繁化姬转换字词, 默认关闭'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o zhconvert-to -o zt -r -d '使用繁化姬转换的目标, 默认简体化, 详见: https://zhconvert.org/ , 可用 [Unknown,Simplified,Traditional,China,Hongkong,Taiwan,Pinyin,Bopomofo,Mars,WikiSimplified,WikiTraditional]'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o zhconvert-keep-origin -o zhconvert-keep -o zk -o zhconvert-only-artifact -o zhconvert-clean -o zoa -o zc -d '是否保留转换前文本, 默认开启'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o clean-up -o only-artifact -o oa -o keep-material -o km -d '是否只保留混流后的成品, 默认开启'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o skip-mux -o sm -o mux -s m -d '是否跳过混流'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o pd -o part-detail -o show-all-parts -d '显示所有分P, 默认关闭'
complete -c tracks -n "__fish_seen_subcommand_from dig" -s p -o part -o parts -r -d '视频分 P, 支持范围选择, 形如 \'3-5\', \'0\' 表示全部'
complete -c tracks -n "__fish_seen_subcommand_from dig" -o last-part -o latest-episode -o lp -o not-last-part -o nlp -d '是否选择最新/最后分P'
complete -c tracks -n "__fish_seen_subcommand_from dig" -s h -l help -d 'Show this message and exit'

## Arguments for dig
complete -c tracks -n "__fish_seen_subcommand_from dig" -d 'B 站视频地址或 av BV ss ep md 等号码'


### Setup for live
complete -c tracks -f -n __fish_use_subcommand -a live -d '获取直播流链接'

## Options for live
complete -c tracks -n "__fish_seen_subcommand_from live" -s p -o player -r -d '使用指定播放器打开, 可用 [iina, potplayer, vlc, nplayer, custom]'
complete -c tracks -n "__fish_seen_subcommand_from live" -s q -o quality -r -fa "fast 流畅 std standard 高清 high 超清 bluray blu-ray 蓝光 dolby 杜比 origin 原画 4k" -d '直播画质'
complete -c tracks -n "__fish_seen_subcommand_from live" -o protocol -s P -r -d '协议优先级, 默认 [http_hls, http_stream]'
complete -c tracks -n "__fish_seen_subcommand_from live" -o format -s f -r -d '封装优先级, 默认 [ts, fmp4, flv]'
complete -c tracks -n "__fish_seen_subcommand_from live" -o codec -s c -r -d '编码优先级, 默认 [avc, hevc]'
complete -c tracks -n "__fish_seen_subcommand_from live" -s h -l help -d 'Show this message and exit'

## Arguments for live
complete -c tracks -n "__fish_seen_subcommand_from live" -d '直播房间号'


### Setup for login
complete -c tracks -f -n __fish_use_subcommand -a login -d '扫码登录'

## Options for login
complete -c tracks -n "__fish_seen_subcommand_from login" -o sms -o qr -o cookie -o pwd -d '登录的方式，包括可以使用 [-sms, -qr, -cookie, -pwd]'
complete -c tracks -n "__fish_seen_subcommand_from login" -o no-gui -s G -d '扫码不使用GUI'
complete -c tracks -n "__fish_seen_subcommand_from login" -s h -l help -d 'Show this message and exit'


### Setup for config
complete -c tracks -f -n __fish_use_subcommand -a config -d '配置命令

使用方法:
 
\'tracks config list\' 列举所有可用选项
 
\'tracks config key1=xxx,key2=yyy,\' 分别设置 key1 key2 的值为 xxx yyy

\'tracks config key1,key2\' 查询 key1 和 key2 的值

\'tracks config key1=xxx,key2\' 设置 key1 为 xxx, 查询 key2'

## Options for config
complete -c tracks -n "__fish_seen_subcommand_from config" -s h -l help -d 'Show this message and exit'

## Arguments for config
complete -c tracks -n "__fish_seen_subcommand_from config" -d '表达式'


### Setup for version
complete -c tracks -f -n __fish_use_subcommand -a version -d '显示 Tracks 版本'

## Options for version
complete -c tracks -n "__fish_seen_subcommand_from version" -s l -o long -s s -o short -d '显示长版本号, 默认关闭'
complete -c tracks -n "__fish_seen_subcommand_from version" -s h -l help -d 'Show this message and exit'

