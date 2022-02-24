
## 时序数据
### 可观察性

在现实世界, 时序数据基本为多种来源数据的随时间产生的可观察数据集. 通常情况下, 时序数据表示为相对较大且按时间顺序排列的数据集。由于时间序列的特性，使用分组、连接等数据执行复杂查询或习惯性操作可能非常困难且成本高昂。这就是为什么在大多数案例中，时序数据是按时间顺序在数据流中收集和使用的，并且不会进行复杂的转换。时间序列数据的这种性质使得传统数据库不太适合存储和处理此类数据。

Alfred 是一个功能强大的实时计算引擎和消息中间件的结合体，最初设计用于聚合和传输大量超高频、低延迟时间序列数据的场景(金融股市交易, 电商支付, IoT, IOV)。

### 消息传输

Alfred 使用消息存储时序事件数据, 同时 并且每种类型的事件都有一个在 Alfred 中分配给它的个体消息类。消息类具有一组字段（属性），用于表征、描述、标识每种特定类型的事件。类也有两个从消息父类继承的属性。这些字段是时间戳（事件的时间和日期）和符号（时间序列源的个体标识符）。所有这些使消息类似于高级编程语言中的类(Java类)。

在下图中，金融市场数据以交易和最佳买入/卖出 (BBO) 消息的形式表示。每个消息类都有一组单独的字段和从父类继承的两个属性：符号（交易工具标识符）和特定的时间戳。

[Message Inhibition](https://github.com/datasphere-oss/Alfred/blob/main/picture/Msg-Inhibition.png)

### 数据流处理

消息按每个符号的时间戳顺序存储在数据流中。在下面的流图中，我们看到了按 Symbol(AAPL、AMZN 等)聚合的不同类别的消息(Best Bid/Offer (BBO) 和 Trade）。所有消息都根据它们的时间戳(T1、T2 等)按时间顺序排列在数据流中。 Alfred 提供纳秒颗粒度的时间戳。

[Stream Timeline](https://github.com/datasphere-oss/Alfred/blob/main/picture/Stream-Timeline.png)

Alfred 数据流可能包含多个类的消息。在这种情况下，特定的流模式决定了可以将哪些消息类记录到每个流中。

### 摄取数据

Publisher (loader) 写入数据到数据流中. Loader 按时间顺序将消息添加到数据流中. 一些 loader 写入相同的数据流; 而每个Loader仅写入一个数据流中.

[Loader](https://github.com/datasphere-oss/Alfred/blob/main/picture/Loader.png)

### 消费数据

Subscriber(Cursor)从数据流中读取数据. 单个数据流可能有多个从中读取数据的Cursor，每个Cursor可以从一个或多个数据流中读取数据。Cursor使用与Loader写入的时间顺序完全相同的消息。

[Cursor](https://github.com/datasphere-oss/Alfred/blob/main/picture/Cursor.png)

Timestamp, symbol 和 class 能够通过 "keys"来感知, 服务于消息检索. 游标使用键来订阅特定的数据子集。Cursor消耗的数据是原始流的消息时间线的转换，其中消息使用任何或所有“Keys”进行过滤，并且始终按时间顺序排列（T2>T1.. etc.）

### 订阅数据

Cursor（Subscriber）可以消费整个数据流。在这种情况下，Cursor会按照Loader（Publisher）写入的时间顺序接收相关消息。所有Symbol的消息都根据它们的时间戳以一个时间序列聚合。这张图片展示了一个流时间线，其中所有消息都按时间顺序排列。

[Timeline1](https://github.com/datasphere-oss/Alfred/blob/main/picture/Timeline1.png)

### 订阅过滤

Cursor（Subscriber）可以根据订阅消息的时间戳过滤订阅消息。原始的时间顺序被保留。图片说明了从时间戳 T6 开始订阅所有消息流。

[Timeline2](https://github.com/datasphere-oss/Alfred/blob/main/picture/Timeline2.png)

Cursor（Subscriber）可以根据它们的时间戳和类过滤订阅消息。原始的时间顺序被保留。图片说明了从时间戳 T6 开始订阅 Trade 类的所有消息流。




