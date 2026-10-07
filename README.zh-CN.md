# Oneirgeo · 梦域

[English](README.md) | **简体中文**

*时钟停在三点十七分。灯还亮着。*

Oneirgeo 是一个将 Minecraft 改造成巨大超现实梦境的 Fabric 模组。它重做主世界、下界与末地，并新增镜面之海、无尽泳池和后室。在六个维度之间，熟悉的地方变得陌生，空间折回自身，散落的记忆逐渐拼成一个故事。

模组以梦核、怪核、阈限空间和心理恐怖为基调。探索这些世界，寻找穿行与醒来的路，想起它们对你意味着什么。

当前预发布版本为 **1.0.0**，可在 [GitHub Releases](https://github.com/eltavine/Oneirgeo/releases) 下载。

## 六个世界

梦域的每个维度都有 **4064 格的垂直空间**，范围为 Y = -2032 至 Y = 2031。

| 世界 | 场景 |
| --- | --- |
| 主世界 | 悬浮在雾中的巨大神经网络、组织般的结构、突触，以及家的痕迹。 |
| 下界 | 锅炉房走廊、灰烬平原、熔岩之海、巨大炉膛与倒悬之城。 |
| 末地 | 永夜之海、星空墓园、不可能的几何结构与天文台。 |
| 镜面之海 | 静止的镜面、竖立的镜子，以及海面之下的另一侧。 |
| 无尽泳池 | 无限延伸的瓷砖泳池、安静的水面、封闭房间与通往别处的门。 |
| 后室 | 泛黄墙纸、潮湿地毯、荧光灯，以及仿佛没有尽头的办公房间。 |

## 主要内容

- **折叠空间与异常重力**：空间接缝、重力倒置区域和封闭陷阱，让辨认方向成为谜题的一部分。
- **入梦与醒来**：在主世界睡觉会进入无尽泳池。醒来之门可以带你返回；在泳池中，清醒度过低也会结束梦境。
- **隐藏的清醒度**：黑暗、梦境维度和空间异常会消磨清醒度；光照、食物、睡眠与清醒茶可以恢复它。低清醒度会影响氛围和遭遇。
- **通过探索还原的故事**：收集记忆碎片，阅读梦境日记，在六个章节中找回 VHS 录像带。
- **诡异的相遇**：无脸者、追踪者和模仿者游荡其中，也有救生员与夜班护士。
- **穿行梦境的方式**：镜子传送门、隐藏门、电梯和上升气流，连接不同地点与高度。
- **生存补给**：系统根据所在维度定期提供物资，间隔可配置；被困房间会阻断自动补给。
- **不会遗落的东西**：新世界默认死亡不掉落；梦境日记、录像带等剧情物品无法丢弃。
- **梦境视听效果**：自定义环境音、混响、远景剪影、异常天空、后期处理与手持摄像机风格的画面，支持客户端调整。

游戏内文本支持 **英语和简体中文**。

## 运行要求与安装

当前源码配置使用以下版本：

| 组件 | 版本 |
| --- | --- |
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` 或更新版本 |
| Fabric API | `0.162.0+26.3` |
| Java | `25` |

1. 为目标 Minecraft 版本建立 Fabric 游戏实例。
2. 在 [GitHub Releases](https://github.com/eltavine/Oneirgeo/releases) 下载模组 JAR，或按下方说明从源码构建。
3. 将模组 JAR 和匹配版本的 Fabric API JAR 放入实例的 `mods` 目录。使用模组本体 JAR，勿使用 `-sources.jar`。
4. 启动游戏，使用**默认世界预设创建新世界**，体验梦域替换后的世界生成。

多人游戏需要在服务端和每位玩家的客户端同时安装 Oneirgeo 与 Fabric API。模组包含世界生成、客户端视觉效果和网络通信；新世界能提供预期的完整体验。

## 开始探索

首次进入游戏时，你会获得一本梦境日记。探索世界，使用记忆碎片填充日记；完成一个章节后会获得对应的录像带。

- 在主世界的床上睡觉，进入无尽泳池，寻找醒来之门返回。
- 用**镜框**搭建竖直的矩形框架，保持内部为空，再手持**镜子碎片**点击朝向内部空隙的镜框表面，打开通往镜面之海的传送门。内部宽 2 格、高 3 格的框架可以使用。
- 站在**电梯方块**上，按跳跃前往正上方的下一块电梯，按潜行前往下方的电梯。
- 留意门、电话、电视、时钟，以及那些似乎不该出现在这里的物件。

## 客户端设置

设置保存在 `config/oneirgeo-client.json`。输入 `/oneirgeofx` 查看当前设置，也可以在游戏内修改：

| 命令 | 作用 |
| --- | --- |
| `/oneirgeofx safe_mode true` | 抑制闪烁效果，并放慢其余过渡。 |
| `/oneirgeofx effects false` | 关闭梦境后期处理效果。 |
| `/oneirgeofx intensity 0.5` | 设置整体效果强度，范围为 `0`–`2`。 |
| `/oneirgeofx camcorder false` | 关闭摄像机风格的画面表现。 |
| `/oneirgeofx shake 0` | 设置视角晃动强度，范围为 `0`–`2`。 |
| `/oneirgeofx reverb false` | 关闭音频混响。 |
| `/oneirgeofx reload` | 重新读取配置文件。 |

还可以使用 `screen_text`、`far_silhouettes` 与 `wrong_sky` 开关，参数均为 `true` 或 `false`。

## 构建与开发

安装 JDK 25，并确保 Gradle 使用该版本。仓库已包含 Gradle Wrapper。

```sh
git clone https://github.com/eltavine/Oneirgeo.git
cd Oneirgeo
./gradlew build
```

构建产物位于 `build/libs/`。Windows 下将 `./gradlew` 替换为 `gradlew.bat`。

| 命令 | 用途 |
| --- | --- |
| `./gradlew runClient` | 启动开发客户端。 |
| `./gradlew runServer` | 启动开发专用服务端。 |
| `./gradlew runDatagen` | 重新生成 `src/main/generated/` 中的资源。 |
| `./gradlew runSelftest` | 执行覆盖所有维度的无界面生成冒烟测试。 |
| `./gradlew runClientGameTest` | 执行视觉冒烟测试，截图位于 `run/gametest/screenshots/`。 |

开发客户端默认使用 4 GB 堆内存，无界面自测默认使用 3 GB。需要时可覆盖配置的堆大小：

```sh
./gradlew runClient -Poneirgeo.heap=6G
```

生成的资源与世界数据纳入版本控制；数据生成缓存和本地运行文件已被忽略。

### 管理与调试命令

以下命令需要游戏管理员权限，例如服务器 OP，或在单人游戏中开启作弊。

| 命令 | 用途 |
| --- | --- |
| `/oneirgeo where` | 查看当前层、场景、生物群系与空间状态。 |
| `/oneirgeo dim <dimension>` | 前往指定维度，例如 `oneirgeo:poolrooms`。 |
| `/oneirgeo layer <name>` | 前往当前维度的指定层，可用 Tab 补全层名。 |
| `/oneirgeo stats` | 查看区块生成耗时。 |
| `/oneirgeo seams` | 检查附近的空间接缝。 |
| `/oneirgeo lucidity [value]` | 查看清醒度，或设置为 `0` 至 `1` 之间的数值。 |
| `/oneirgeo supply` | 立即发放物资。 |
| `/oneirgeo story` | 查看剧情进度。 |

游戏规则：`oneirgeo:lucidity` 控制清醒度是否变化；`oneirgeo:supply_interval` 设置自动补给间隔，单位为游戏刻，默认 `6000`（每秒 20 刻时为五分钟），设为 `0` 可关闭补给。梦域还会默认开启原版规则 `keep_inventory`，新世界中死亡不会掉落物品；可用 `/gamerule keep_inventory false` 关闭。已有世界保持原有设置。

## 许可

作者：**eltavine**。采用 **Apache-2.0** 许可，详见 [LICENSE.txt](LICENSE.txt)。
