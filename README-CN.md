# Alfred

## 特点
Alfred 为云原生实时规则计算引擎，Alfred以事件驱动和高性能著称。提供大规模分布式、流批混合处理、智能数据模型来满足严苛的金融业务目标。

## 架构

Alfred Server 为 Alfred实时和规则计算的核心部分. 架构图展示了Alfred其主要组件部分:

[components](https://github.com/datasphere-oss/Alfred/blob/main/picture/Pub-Sub.png)

Message Bus 提供了客户端-服务端的网络通信, 可使用协议为 TCP, IPC, UDP, 或 HTTP 网路协议. 客户端API可使用多种语言进行集成对接.

Alfred Instance 感知到持续地数据流入. Publisher (Loader) 将数据写入到数据流. 每个 Loader 仅将数据写入到一个特定的数据流中. 多个 Loader 可同时将数据写入到一个数据流中. Subscriber (Cursor) 能够从一个或多个数据流中读取数据, 可根据需求临时添加实时过滤条件.






### 应用场景
主要用于金融行业股市及衍生品交易市场、时序数据与实时规则计算、实时风控预警等应用场景，以及新能源汽车驾驶路况实时监测预警等应用场景。

### 应用挑战
为了能够即时捕捉处理来自金融市场的数据, Alfred以高吞吐的事件处理计算技术改变游戏规则，可支持现代高频交易的实时事件数据处理，

### 金融可信

Alfred经过金融市场复杂极端环境磨炼，为金融级可信产品，

