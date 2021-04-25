#### 项目启动

配置mysql：修改属性文件的连接配置，并创建对应的数据库和表

配置mq：修改mqConfig的参数

配置redis：修改属性文件的连接配置



### 文章-设计点赞的评价系统

有人说，初级程序员只会CURD（Create、Update、Read、Delete），没什么水准，只做这些没什么上升空间。有人说，学习了算法和设计模式，但是在实际项目中却根本用不上，面试造火箭，工作拧螺丝。有人说，掌握了分布式、高并发的框架，才算是优秀的程序员，才能摆脱CURD。

但我认为，CURD是程序员的基本功，写好它也不是那么容易的事情。程序员的进阶，从来不是一次思维的转变或者技术的选择就能实现的，而是一点一滴积累经验，日复一日自我提升的量变的过程。

本文以设计一个简单的评价系统为例，讨论要实现一个较为完善的CURD功能所需要掌握的细节与方法。评价系统很常见，比如美团订单评价、豆瓣评分、stream玩家评价、kindle书评等等，也很复杂，涉及订单、用户、推荐、客服等多个系统和流水线。我们这次仅设计其中核心的一小部分，虚构出一个简单的评价系统需求场景，然后实现它。这不代表真实的实现方式就是这样，更不代表本文的实现就是最佳实践，我只是根据自己的经验，力所能及地设计一个看得过去的评价系统。

也许一个好的技术说明文应该尽量不包含作者的个人观点，但是软件设计不一样，同一个需求可行的设计方法有很多，每个人的选择都有可能不同，一些设计决策和代码构思不可能不包含个人观点。虽然如此，在实现和解释某一项技术时，我会力求其真实性，在不可断定时，也会显示标注出来。



#### 场景分析

![image-20210330130827349](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210330130827349.png)



这是参考美团订单评价而虚构出的需求模型，省略了大量额外需求，只保留了核心的增删改查。从需求中可以看到明显的一个特点，就是一条订单对应一个评价，这条规则大大限制了用户请求影响范围，降低了恶意请求、垃圾请求的发生频率，以至于并发的压力并不大，总体上没有大的技术瓶颈，还好还好。

需求中涉及与订单服务、用户服务的交互，这些并不是我们要设计的，在项目，我们以桩对象的形式模仿它的行为。对待其他的外部依赖也是如此，这样能让我们更集中于评价系统本身的设计。



#### 技术选型

语言框架的话，毫无疑问是Java+Spring Boot，并不是说其他的不行，只是因为我只会这个。

架构模式的话，开发阶段是使用单点架构，只在云服务器上部署一个实例，客户端直接根据IP和端口访问服务。而在实际应用中，无论是作为nginx的upstream服务还是作为一个微服务，都可以很好的适配，至少要有一个网关。

数据库的话，使用mysql。一般来说，评价系统所可能产生的数据量是万亿级别，需要涉及到大型的数据库集群，使用mysql可能不合适，但是我没有条件搭建像OceanBase这样的企业级分布式关系型数据库，只能以mysql作为样例。mysql比mongodb等nosql具有更好的通用性，有更完善的事务机制，可以很容易地与其他系统连接。



#### 设计表结构

```sql
#create database remark;

#评价表
drop table remark;
CREATE TABLE IF NOT EXISTS remark(
	id INT primary key auto_increment COMMENT 'ID',
   	gmt_create datetime not null default CURRENT_TIMESTAMP COMMENT '创建时间',
  	gmt_modified datetime not NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  	consumer_id INT not null COMMENT '顾客ID|评价者ID',
  	item_id varchar(50) not null COMMENT '项目ID|商品ID',
  	order_id varchar(50) not null COMMENT '订单ID',
  	score TINYINT not null COMMENT '评分（0-100）',
  	header VARCHAR(20) not null COMMENT '评价标语|标题',
  	content VARCHAR(200) not null COMMENT '评价内容',
  	images VARCHAR(1000) COMMENT '附属图片，空格分隔',
  	votes INT not null default 0  COMMENT '投票|点赞数',
  	user_name VARCHAR(20) COMMENT '用户名',
    user_face VARCHAR(100) COMMENT '用户头像',
  	status TINYINT not null default 1 COMMENT '评价状态（0：无效 1:有效）',
  	INDEX(item_id),
  	UNIQUE(order_id)
);
#测试数据
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(1,1,'o1',100,'深入浅出，推荐','强烈推荐，把复杂的算法讲得很清楚，学习一个新算法，很受用。学完了再看其他数据再一步深入。');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(2,1,'o2',80,'很适合我这种算法小白','对于算法已经有一定理解的人可能会觉得这书太肤浅，但对于我这种小白来说太有用了，比起晦涩的语言描述，这种图示加上说明的讲解方式最起码让我一下子就看明白了。如果你想从无到有的了解算法，这书很适合。');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(3,1,'o3',20,'太无趣了，不推荐','总之就是十分不推荐');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(4,2,'o4',80,'避免代码陷阱','介绍了大量不大注意的“坑”，通过大量的容易出错的知识点，讲解如何避免这些定时炸弹在程序中出现。');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(5,2,'o5',60,'还不错','比起effective java ，这个也是值得一看的');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(6,2,'o6',100,'必须是好书','比effective java还要实用。。。');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(1,3,'o7',77,'测试','<b>我这是评论</b>');
insert into remark(consumer_id,item_id,order_id,score,header,content) 
values(7,3,'o8',101,'测试2','这个用户未购买，但是发表了评价');
insert into remark(consumer_id,item_id,order_id,score,header,content,images) 
values(2,3,'o9',20,'测试3','带图评价','https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20201224011110127.png https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20201224011336403.png');

```



#### 仓库层

实体类：

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remark {
    private long id;

    private long consumerId;

    private String itemId;

    private String orderId;

    private short score;

    private String header;

    private String content;

    private String images;
    
    private String createTime;

    private String username;

    private String userface;
}
```



仓库类：

```java
@Repository
public class RemarkRepository {
    @Autowired
    private JdbcTemplate db;

    /**
     * 添加评价
     * @param remark 评价对象
     * @return 如果插入成功，返回true，否则返回false
     */
    public boolean addRemark(/*valid*/ Remark remark) {
        String sql = "insert into remark(consumer_id,item_id,order_id,score,header,content,images,user_name,user_face) values(?,?,?,?,?,?,?,?,?)";
        return db.update(sql, remark.getConsumerId(), remark.getItemId(), remark.getOrderId(), remark.getScore(), remark.getHeader(),
                remark.getContent(), remark.getImages(), remark.getUsername(), remark.getUserface()) > 0;
    }

}
```

没错，我手动操作了sql，并没有使用ORM。我没有从ORM中看到任何好处，从领域模型到表结构，这个映射过程完全可以在仓库层（Repository）完成。仓库对象会更轻量级，因为它只执行必要的sql，不会一次性增删改查全部具备；仓库对象可读性更好，不用忍受框架生成的WherexxxAndxxx方法，更不用强迫实体类定义Integer、Boolean等恶心的类型，它会直接提供特定于领域模型的简明易懂的方法，为上层使用者服务而不是向底层实现者妥协；仓库对象更灵活，它可以增加除了sql操作以外的其他运算，聚合、过滤等，也可以根据参数动态生成sql，这样让上层服务更轻松，更能专注于自身的逻辑实现，而不需要服务层再为如何使用表结构而费心；最后，仓库层所抽象的数据源不止是数据库，它也可以作为redis、es、memory等数据源的抽象层。

仓库层，是虚拟出来的概念。不同仓库之间不耦合，一般来说，一个仓库类负责一个表或某类数据的权威访问，其他任何需要这项数据的上层代码，都需要依赖这个仓库，这有助于消除重复代码，易于优化。比仓库层更底层的是异构数据的访问层，比如访问文件系统需要用到fileUtil，访问redis需要用到lettuce，访问db需要用到jdbc，所以在某种程度上，我理想中的仓库层与orm并不矛盾，只是单方面觉得orm有点累赘。

仓库层在整个项目抽象层次结构中的位置：

![image-20210420213127145](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210420213127145.png)

后文会多次提到仓库层，这是整个系统中非常重要的抽象。



#### 关于异常捕获

关于异常捕获，本项目遵循以下几个约定：

**积极地抛出异常**。异常包含很多有价值的信息，有助于调用者更好地处理问题，不要吞掉异常。

异常不会产生性能问题，比如下面的测试代码：

```java
public class TestCatch {
    private final static Exception EXCEPTION = new Exception();
    public static void main(String[] args) {
        //分别代表不使用try-catch，使用但不抛出异常，使用但小概率抛出异常，使用但大概率抛出异常，一定抛出异常
        double[] rateList = new double[]{-1, 0, 0.00001, 0.1, 1};

        //预热
        doSomething(0.001);

        for (int i = 0; i < rateList.length; i++) {
            StopWatch watch = new StopWatch();
            watch.start();
            doSomething(rateList[i]);
            watch.stop();
            System.out.println(watch.getTotalTimeMillis());
        }
    }

    public static void doSomething(double exceptionRate) {
        if (exceptionRate < 0) {
            for (int i = 0; i < 100000000; i++) {
                Math.cos(ThreadLocalRandom.current().nextDouble());
            }
        } else {
            for (int i = 0; i < 100000000; i++) {
                try {
                    double v = ThreadLocalRandom.current().nextDouble();
                    Math.cos(v);
                    if (v < exceptionRate) throw EXCEPTION;
                } catch (Exception e) {
                }
            }
        }
    }
}
```

结果输出：

```java
2258
2264
2244
2253
2271
```

太令人惊讶了，运行时间几乎没有差别，编译器和解释器已经做了极大的优化。（如果改为每次抛出异常是new一个异常对象的话，那么运行时间将大大延长，可见异常对象的创建开销是可观的，这点值得注意）

异常捕获也不一定降低可读性。实际上，我们常说的异常捕获引起的可读性的降低，大多是由于语言本身的不足导致的，因为它占用了额外的3行代码，没有IF那样简洁，这也是很多人不喜欢SWITCH的原因。



**不封装返回值**

有些程序员喜欢采用诸如ResultMsg的形式封装方法的返回值，将方法调用结果以及异常信息都包含进来。比如：

```java
public ResultMsg insert(String content){
    if (StringUtils.isEmpty(content) || content.length() > 150)
        return ResultMsg.PARAM_ERROR.setMsg("内容不能为空或超过150字");

	// do insert
    return ResultMsg.INSERT_SUCCESS;
}
```

这虽然能为调用者提供丰富的结果信息，避免try-catch捕获异常，但是却丧失了可读性。这是致命的，我们不能从方法签名中直观地看出这个方法返回了什么数据类型，而且调用者很容易忽略本应该需要特别处理的异常。



**仓库层不捕获异常**

前面建议积极捕获异常，为什么仓库层却不捕获呢？因为捕获不了。捕获的本质是为了处理，仓库层只负责对数据源的存储与读取，数据源导致的异常它自己无法解决。吞掉吗？绝不可以，调用者有权知道具体出问题的原因，有必要知道查询结果究竟是真的为空还是出故障了，仓库层不应该替使用者做这种决定。捕获并重新抛出更有意义的异常吗？这就有点过于谨小慎微了，这让设计过于臃肿，对于调用者的帮助很小，没这个必要。那要在方法签名中声明throws，强制让调用者处理吗？也不用，调用者如果真的想捕获，想做一个管理者的角色的话，它自然会去捕获，如果没有显示去捕获，可能它就是不想，扔给全局异常处理会更简单。



总是低层掌握的异常信息比高层掌握的更具体，高层是对低层执行结果的抽象概括。

![image-20210331195659496](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210331195659496.png)



#### Create：发布评价

RemarkService代码：

```java
/**
 * 评价服务
 */
@Service
@Slf4j
public class RemarkService {
    private final RemarkRepository remarkRepository;
    private final OrderService orderService;

    public RemarkService(RemarkRepository remarkRepository, OrderService orderService) {
        this.remarkRepository = remarkRepository;
        this.orderService = orderService;
    }

    /**
     * [CREATE] 发布评价
     *
     * @param remark 评价对象
     * @return 如果新增成功，返回true，否则，返回false或抛出异常。
     * @throws ExceedAuthorizedAccessException 越权访问异常
     */
    public boolean createRemark(/*valid*/ Remark remark) throws ExceedAuthorizedAccessException {
        //用户订单校验
        String itemId = orderService.checkAndGetItemId(remark.getConsumerId(), remark.getOrderId());
        if (itemId == null) {
            throw new ExceedAuthorizedAccessException("用户不存在有效订单");
        }

        remark.setItemId(itemId);
        try {
            //直接插入，利用数据库的唯一约束做幂等性校验。
            remarkRepository.addRemark(remark);
        } catch (Exception e) {
            log.warn("function RemarkService.createRemark make exception:{} by:{}", e.getMessage(), remark);
            return false;
        }

        return true;
    }
}
```



OrderService代码：

```java
/**
 * 订单服务
 */
public class OrderService {
    /**
     * 检查是否已经产生订单关系，并返回订单关联的商品id
     * @param consumerId 用户id
     * @param orderId 订单id
     * @return 如果订单有效，返回商品id，否则返回null
     */
    public String checkAndGetItemId(long consumerId, String orderId){
        //TODO 这里需要调用远程服务，但是我们尚未对接远程服务，因此设定一个假想值。
        RemoteServerUtil.mockRemoteServerRequest();
        return "item-1";
    }
}

//other file
public class RemoteServerUtil {
    public static void mockRemoteServerRequest(){
        //模拟远程请求延迟
        try {
            Thread.sleep(10 + ThreadLocalRandom.current().nextInt(10));
        }catch (InterruptedException e){
        }
    }
}
```

RemarkService直接使用OrderService而不是OrderRepository，是因为高层服务总是比底层服务更优质。一般来说，仓库层是服务层或其他管理层具体实现的一部分，服务层之间的通信，应该依赖抽象而不是具体。

评价服务利用数据库唯一约束取巧实现了幂等性，我认为这也是幂等性最合理的实现方式（利用底层服务的互斥性）。



#### 单元测试

我们使用H2内存数据库隔离远程数据库的依赖，需要在test/resources下建立相关的数据源配置文件。

测试RemarkRepository：

```java
@SpringBootTest
@RunWith(SpringRunner.class)
class RemarkRepositoryTest {
    @Autowired
    private RemarkRepository remarkRepository;

    @Autowired
    private JdbcTemplate db;

    @Test
    void addRemark() {
        Remark remark = new Remark();
        remark.setConsumerId(1000);
        remark.setItemId("1234");
        remark.setContent("真心不错，下次还要买!!!");
        remark.setOrderId("orderId-1");
        remark.setHeader("标题");
        remark.setScore(80);
        remark.setImages("https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20201224011110127.png");

        remarkRepository.addRemark(remark);
        String sql = "select consumer_id,item_id,order_id,score,header,content,images from remark where order_id = ?";
        Remark target = db.queryForObject(sql, (resultSet, i) -> {
                    Remark tempRemark = new Remark();
                    tempRemark.setConsumerId(resultSet.getLong(1));
                    tempRemark.setItemId(resultSet.getString(2));
                    tempRemark.setOrderId(resultSet.getString(3));
                    tempRemark.setScore(resultSet.getShort(4));
                    tempRemark.setHeader(resultSet.getString(5));
                    tempRemark.setContent(resultSet.getString(6));
                    tempRemark.setImages(resultSet.getString(7));
                    return tempRemark;
                }, remark.getOrderId()
        );

        Assert.assertEquals(remark, target);

        //测试唯一约束冲突
        sql = "select count(*) from remark";
        Integer count = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));
        try {
            remarkRepository.addRemark(remark);
        } catch (Exception e) {
        }

        Integer count2 = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));

        Assert.assertEquals(count, count2);
    }
}
```



测试RemarkService:

```java
@SpringBootTest
@RunWith(SpringRunner.class)
class RemarkServiceTest {
    @Autowired()
    private RemarkService remarkService;

    @Autowired
    private JdbcTemplate db;

    @Autowired
    private RemarkRepository remarkRepository;

    @Test
    void createRemark() {
        //测试理想成功插入
        Remark remark = new Remark();
        remark.setConsumerId(1000);
        remark.setContent("真心不错，下次还要买!!!");
        remark.setOrderId("orderId-2");
        remark.setHeader("标题");
        remark.setScore((short)80);
        remark.setImages("https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20201224011110127.png");

        try {
            remarkService.createRemark(remark);
        } catch (ExceedAuthorizedAccessException e) {
        }

        String sql = "select consumer_id,item_id,order_id,score,header,content,images from remark where order_id = ?";
        Remark target = db.queryForObject(sql, (resultSet, i) -> {
                    Remark tempRemark = new Remark();
                    tempRemark.setConsumerId(resultSet.getLong(1));
                    tempRemark.setItemId(resultSet.getString(2));
                    tempRemark.setOrderId(resultSet.getString(3));
                    tempRemark.setScore(resultSet.getShort(4));
                    tempRemark.setHeader(resultSet.getString(5));
                    tempRemark.setContent(resultSet.getString(6));
                    tempRemark.setImages(resultSet.getString(7));
                    return tempRemark;
                }, remark.getOrderId()
        );

        Assert.assertEquals(remark, target);

        //测试重复插入
        sql = "select count(*) from remark";
        Integer count = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));
        try {
            boolean flag = remarkService.createRemark(remark);
            Assert.assertFalse(flag);
        } catch (ExceedAuthorizedAccessException e) {
        }

        Integer count2 = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));

        Assert.assertEquals(count, count2);

        //测试权限校验失败
        try {
            new RemarkService(remarkRepository, new MockOrderService()).createRemark(remark);
            Assert.assertTrue(false);
        } catch (ExceedAuthorizedAccessException e) {
        }
    }
}
```

```java
//桩对象，返回失败的校验
public class MockOrderService extends OrderService {
    @Override
    public String checkAndGetItemId(long consumerId, String orderId) {
        return null;
    }
}
```

单元测试，不免有很多重复代码，不过这不重要，对于测试来说，独立性、可重复和自动化是最重要的。



#### Controller

在写controller之前，先决定下通用的返回错误码，我选择参考阿里的错误码标准。虽然如此，精细的错误码适合开发API接口，而对于我们这次设计的评价系统，我会选择返回相对比较笼统的错误码。

```java
/**
 * 参考自阿里巴巴Java编码规范，错误码
 */
public enum RtnCodeEnum {
    SUCCESS("00000", "成功"),
    USER_ERROR_0001("A0001", "用户端错误"),
    
    //......省略一大堆错误码

    private final String code;
    private final String description;

    RtnCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
```

基于此定义通用返回对象。

```java
/**
 * 通用的 rest http请求 API 返回参数格式。如：
 * {
 *     "status": "00000",
 *     "message": "成功",
 *     "data": [
 *         {
 *             "consumerId": 4,
 *             "content": "",
 *             "createTime": "2021-04-08 09:46:36",
 *             "header": "避免代码陷阱",
 *             "id": 4,
 *             "itemId": "2",
 *             "orderId": "o4",
 *             "score": 80
 *         }
 *     ]
 * }
 */
public class APIBody {
    private final String status;

    private final String message;

    /**
     * 只有这个不是final，利用这个特性，使用同线程覆盖来节省创建对象的开销
     */
    private Object data;

    private final static APIBody SUCCESS_BODY = new APIBody(RtnCodeEnum.SUCCESS);

    private final static ThreadLocal<APIBody> SUCCESS_LOCAL_BODY = new ThreadLocal<>();

    private APIBody(RtnCodeEnum rtnCodeEnum, String message) {
        this.status = rtnCodeEnum.getCode();
        this.message = message;
    }

    private APIBody(RtnCodeEnum rtnCodeEnum) {
        this(rtnCodeEnum, rtnCodeEnum.getDescription());
    }

    /**
     * 构建无数据成功返回对象
     */
    public static APIBody buildSuccess(){
        //确保可变属性不受影响
        SUCCESS_BODY.data = null;
        return SUCCESS_BODY;
    }

    /**
     * 以指定返回值构建成功返回对象。
     * @param data 数据对象
     */
    public static APIBody buildSuccess(Object data){
        APIBody body = SUCCESS_LOCAL_BODY.get();
        if (body == null){
            //在并发下可能导致重复创建对象，不过这只发生在最开始，没关系
            body = new APIBody(RtnCodeEnum.SUCCESS);
            SUCCESS_LOCAL_BODY.set(body);
        }

        //每个线程覆盖属于自己的属性,互不冲突
        body.data = data;
        return body;
    }

    public static APIBody buildError(RtnCodeEnum rtnCodeEnum, String message){
        //很少请求真的会执行失败，这个对象创建不必优化
        return new APIBody(rtnCodeEnum, message);
    }

    public static APIBody buildError(RtnCodeEnum rtnCodeEnum){
        return buildError(rtnCodeEnum, rtnCodeEnum.getDescription());
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
```

然后写评价接口：

```java
/**
 * 评价接口
 */
@RestController
public class RemarkController {
    private final RemarkService remarkService;
    private final SecurityService securityService;

    public RemarkController(RemarkService remarkService, SecurityService securityService) {
        this.remarkService = remarkService;
        this.securityService = securityService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello world!";
    }

    /**
     * [CREATE] 发布评价
     */
    @PostMapping("/remark")
    public APIBody createRemark(@RequestBody Remark remark){
        remark.validateForCreate(securityService);

        try {
            if(remarkService.createRemark(remark)){
                return APIBody.buildSuccess();
            }
        } catch (ExceedAuthorizedAccessException e) {
            return APIBody.buildError(RtnCodeEnum.USER_ERROR_A0300);
        }

        return APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
    }
}
```

然后定义全局异常捕获：

```java
/**
 * 捕获全局非受检异常
 */
@RestControllerAdvice(basePackages = "com.sunday.remark.controller")
@Slf4j
public class CommonControllerAdvice {
    //assert断言的非法参数异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIBody> handleAssertExceptions(
            IllegalArgumentException ex) {
        log.info("assert error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.USER_ERROR_A0400, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //sql异常
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<APIBody> handleDatabaseExceptions(
            SQLException ex) {
        log.info("sql error:{} code:{}", ex.getMessage(), ex.getErrorCode());
        APIBody body = APIBody.buildError(RtnCodeEnum.SERVICE_ERROR_C0300);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    //空指针异常
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<APIBody> handleNPEExceptions(
            NullPointerException ex) {
        log.error("NPE error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    //全局异常处理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIBody> handleCommonExceptions(
            Exception ex) {
        log.error("unkown error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```

这样controller就设计完成了。有以下几点值得注意：

1. controller使用构造函数的依赖注入方式，可以摆脱Spring容器创建对象，也有利于测试。
2. 接口日志会记录异常细节，但是接口返回参数只包含笼统的错误消息，不会暴露实现细节。
3. 使用Restful的接口，尤其体现在借助于HTTP METHOD实现对Remark资源的CURD。



#### 关于参数校验

我没有使用Valid或Validate形式的注解校验方式，或者说，我反对这种方式。一开始加上@NotNull、@Min等注解似乎可以舒舒服服地实现参数校验，但是随着规模的增加，你需要增加越来越多的注解，这些校验往往具有复杂的规则，让你不得不自定义校验器，而这些原本可以通过简单的if语句实现。当接口增多时，很多接口要复用同构的请求参数对象，但每个接口需要的校验规则又各不相同，不得不使用复杂的分组校验特性，强迫controlller的接口方法签名写一大堆的注解。对于某些参数很多的接口，经常遇到“当A满足时，B不能为空”这一类的情况，这对于Validate校验方式来说，又要疯狂下定义。无论怎样，随着项目的累积，这种校验方式过于臃肿，甚至导致一个简简单单的实体类被大量注解包围，根本看不懂。

Validate初衷是为了解决在Controller类中大量的参数校验引起的重复代码以及可读性问题，这个方案基于一个荒诞的假设：为什么要在Controller类中写参数校验？就不能换一个地方吗？不，你甚至不必换一个地方，你可以简单地抽取校验方法，放置在Controller类的尾部，如果有人说这样会导致类的代码过多，private和public混合不利于阅读，你应该把C#的部分类的概念抛给他——这不是设计的问题，是语言的问题，语言的不足不应该导致设计上的让步。

我选择了可能更好的做法，校验应该交给最了解待校验参数的对象，也就是实体类，因此在实体类上写一个校验方法，如果要实现分组校验功能，那么写多个方法就行。

我不觉得这不如注解清晰：

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remark {
    private long id;

    private long consumerId;

    private String itemId;

    private String orderId;

    private int score;

    private String header;

    private String content;

    private String images;

    /**
     * 校验：发布评价
     * @param validator 敏感词校验器
     */
    public void validateForCreate(IWordValidator validator) {
        Assert.isTrue(consumerId > 0, "用户id不能为空");
        Assert.isTrue(!StringUtils.isEmpty(orderId), "订单id不可为空");
        Assert.isTrue(score >= 20 && score <= 100 && score % 10 == 0, "评分不合规");
        Assert.isTrue(!StringUtils.isEmpty(header), "标题不能为空");
        Assert.isTrue(header.length() <= 15, "标题不能超过15字");
        Assert.isTrue(!StringUtils.isEmpty(content), "内容不能为空");
        Assert.isTrue(content.length() <= 150, "内容不能超过150字");
        Assert.isTrue(content.length() >= 10, "内容不能少于10字");
        Assert.isTrue(images == null || images.split(" ").length <= 3, "图片不能超过3个");

        Assert.isTrue(!validator.checkHasSensitiveWords(header, content), "您发布的评价内容不符合规范，请修改");
    }
}
```

IWordValidator是一个敏感词校验接口

```java
/**
 * 敏感词校验器
 */
public interface IWordValidator {
    /**
     * 检查是否存在敏感词
     * @param contents 词汇
     * @return 如果存在敏感词，返回true，否则返回false
     */
    boolean checkHasSensitiveWords(String... contents);
}
```

SecurityService实现了这个接口，虽然真正的实现在此省略。

```java
/**
 * 安全服务
 */
@Service
public class SecurityService implements IWordValidator {
    /**
     * 检查是否存在敏感词
     * @param contents 词汇
     * @return 如果存在敏感词，返回true，否则返回false
     */
    @Override
    public boolean checkHasSensitiveWords(String... contents){
        return false;
    }
}
```



#### 关于接口安全性

**可能的威胁**

接口暴露于外网之中，就不免遭受攻击，“攻击”可能来自于用户无意的意外请求，也可能来自于恶意用户的非法请求。如果在分布式系统设计中，应该把“网络是不可靠的”作为前提的话，那么在接口设计中，也应该把“用户是不可信的”作为前提。

一种有代表性的就是利用现有系统的漏洞进行攻击，比如sql注入、脚本攻击等，这个不需要特别的手段就可以实现对系统的破坏，甚至是用户的无意之举都可能影响系统逻辑。我们唯一能做的就是修复这些bug，进行参数校验，不要使用用户的直接输入作为脚本语句的可执行部分，充分测试边界值等等。

还有的攻击主要来自于网络请求传输过程中数据包的捕获与篡改。典型的比如，接口参数以明文传输，那么这部分的数据就会被泄露出去，即便系统本身安全稳定，仍会损害用户的隐私。篡改接口请求参数很容易导致越权访问，因为大多数的数据表单的校验都是由前端完成的，请求篡改却可以绕过客户端验证，虽然服务端也会做数据校验，但百密一疏，防不胜防。另外非法用户还可能以盈利为目的盗用接口，盗取链接，有必要给接口请求设置一个有效期。

还有最为麻烦的，就是请求重放攻击和拒绝服务攻击。通过不断请求接口，可以给服务器造成很大的压力，严重则造成连接池满，服务瘫痪。比如暴露出一个新增接口，如果没有做幂等性设计，则每次请求都会写入数据，造成数据膨胀，就算做了幂等性设计，但为了实现幂等性而去读取redis缓存，这本身也是一个耗时的操作，大量的请求仍会让它瘫痪。

可以通过请求加密协议来实现接口防护，遗憾的是，HTTPS的设计理念是占在用户一边而不是服务器一边的，它保护的是用户的数据传输安全而不是保护数据的合法性。如果用户本身是恶意的（或者被病毒侵入），他仍可以捕获并篡改接口参数，这和协议是HTTP还是HTTPS没有任何关系。我们需要实现自己的接口防护模式。



**signature模式**

signature模式可以概述为：传输明文，以及额外的signature参数，这个参数由明文和密钥拼接后（或者是时间戳与密钥拼接），再用消息摘要算法（MD5）生成。这样恶意用户就算可以看到明文，也改不了数据，防止仿造数据攻击，配合nonce参数还可以防止请求重放攻击。

我一直疑惑，使用对称加密，不以明文传输，不是更能解决上面的问题？思来想去，主要有两个原因：一是可读性强。明文传输利于接口对接和调试，开发和测试同学可以直观地读懂请求参数与响应，虽然可以设计一个解密工具，但是每一次阅读都需要额外的几秒钟时间来解密，在后续维护阶段简直是噩梦。二是事实标准，signature模式虽然不是最优，但是已经成为业界的普遍规范，我工作中对接过的第三方接口，也几乎都采用了这种模式。其他另辟蹊径的做法不是不行，只是路走的人少了，也便成了死路。



**assess_token模式**

这个模式实现得非常漂亮和简洁：用户端先获取assess_token/token，再用assess_token请求其他接口，这样就实现了用户认证、防止盗链、控制请求次数等功能。



**网关**

网关可以实现身份认证（验证token）、签名认证（验证signature）以及必要的黑白名单和流量控制等功能，这些也最好交给网关来做。这不仅是为了避免重复发明轮子，也让服务与服务之间的通信更顺畅。

有了网关这层拦截，我们的业务层接口还需要进行请求数据验证吗？我的观点是看情况，如果这个验证过程实现简单并且能保护关键逻辑，我就会加上，如果实现困难且只起到微弱的作用，我就会放弃。举个例子，比如添加评论和删除评论时，虽然正常的客户端肯定会做校验，不会发出“没购买就评价”的意外请求，但是验证一下也无碍。而对于缓存穿透，其解决方案非常复杂且不彻底，我认为应该从接口设计方面去解决，解决出现问题的本身而不是现象。



#### Delete：删除评价

仓库层：

```java
@Repository
public class RemarkRepository {
    @Autowired
    private JdbcTemplate db;

    /**
     * 删除评价
     * @param remark 评价对象
     * @return 如果插入成功，返回true，否则返回false
     */
    public boolean removeRemark(/*valid*/ Remark remark){
        //联合多个字段查询，确保删除的评价确实是consumerId所发布的，利用数据库本身的特性进行归属逻辑判断，
        //从根本上避免越权删除别人的评价
        String sql = "delete from remark where id = ? and order_id = ? and consumer_id = ? and item_id = ?";
        return db.update(sql, remark.getId(), remark.getOrderId(), remark.getConsumerId(), remark.getItemId()) > 0;
    }
}
```

服务层：

```java
/**
 * [Delete] 删除评价
 *
 * @param remark 评价对象
 * @return 删除成功返回true，否则返回false或抛出异常
 */
public boolean deleteRemark(/*valid*/ Remark remark) {
    try {
        if (!remarkRepository.removeRemark(remark)) {
            //因参数不一致而出错
            return false;
        }
    } catch (Exception e) {
        log.warn("function RemarkService.deleteRemark make exception:{} by:{}", e.getMessage(), remark);
        return false;
    }

    return true;
}
```

控制层：

```java
/**
 * 删除评价
 */
@DeleteMapping("/remark")
public APIBody deleteRemark(@RequestBody Remark remark){
    remark.validateForDelete();

    if (remarkService.deleteRemark(remark)) {
        return APIBody.buildSuccess();
    }

    return APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
}
```



#### 分页查询SQL

一个商品的可能有上万条评价，要设计查询，就要先想好分页算法。mysql的分页sql语句一般是：

```mysql
#跳过1条记录，选择10条记录.因为id是自增的，所以根据id倒序排序就是根据时间倒序排序
select * from remark where item_id = '1' order by id desc limit 1,10
```

但是不论查询的范围是多少，每次都需要扫描item_id的全部内容然后排序，面对万条以上的记录时，查询效率很低。explain分析后显示它会扫描商品所有的评价，哪怕只是查询第一条内容。

很遗憾，没有优化的可能性。但是如果降级需求，则可以产生以下几种解决方案：



**迭代式查询**

客户端请求查询的时候，不是告诉需要查询那一页，而是告诉需要查询那一条记录之后的内容，而这个记录id是有唯一索引的，mysql可以直接利用索引快速跳跃到这之后的内容，减少遍历范围。

```mysql
#跳跃到指定索引，再搜索内容
select * from remark where item_id = '1' and id < 10 order by id desc limit 2
```

这种方式查询越靠后的内容速度越快。客户端可以通过上一次的查询结果的最后一条记录拿到id，这意味着无法进行跳页查询，只能一步步往下刷内容。



**预先分页**

大多数的场景都是读多写少的（也不一定，因为查询都会缓存，大多不经过数据库，而写入必须经过数据库，未必就是查询次数比写入次数多），可以在写入的时候分好页，查询的时候根据页码数查询。

具体实现为：表中建立pageno字段，写入时先查询当前应该写在第几页，然后带上页数插入数据库。这两步操作最好结合在一个sql中保证原子性。分页查询时，直接根据页码索引快速查询，效率极高。

不足之处就是，一页数量是固定的，不能指定每页有多少条记录，这倒没什么，麻烦的是，删除记录时不能及时地重置页码，会导致缺页。需求需要为此妥协，查询记录时，允许查询到空的占位记录，由客户端做遮掩，显示“评价已折叠”，或“该内容已被删除”。不过，如果一页的内容如果恰巧全部被删除，会令用户很费解，明明后面还有几百页，怎么这一页什么都没有，我能想到的解决方法是定期给异常的缺页进行整理，就像是垃圾回收机制一样。



#### 分页查询难题

分页查询问题不等于分页查询sql的问题，每一次sql固然需要执行的快，但是面对上万并发，每一次查询都请求数据库肯定是不行的，因此需要缓存。

典型的使用redis缓存的伪代码如下：

```java
//旁路缓存模式
JSONArray list = redisRepository.readJsonArray(key);
if (list == null){
    List<Remark> remarks = remarkRepository.listRemarks(itemId, pageno);
    list = JSONArray.fromObject(remarks);
    redisRepository.set(key, list, 3600);
}
return list;
```

先不说缓存击穿的问题，上面的代码最大的问题是：每一次新增评价、删除评价都会让一大批废了九牛二虎之力才加载好的缓存失效。想象一下，假如有个10页的内容列表，那么就分别需要10个KEY来缓存这些数据，当有新的内容插入数据库时，那么第一页的第一条数据就变了，顺应的第二页的数据也变了，以此类推，所有KEY的数据就全部与数据库中的数据不一致了，这时应该怎么办？

你可能认为有谁会频繁地在每次有新内容进来后刷新那么多页的内容呢，但是那么多的用户总会有那么几个另外，致使你需要每次更新时全量刷新缓存，频繁的更新在某种程度上会产生缓存击穿，毕竟评价内容表可是大表，一次分页查询可是很耗时的。

还有一种方法就是根据坐标查询范围内容，比如查询id早于100的近10条内容。那么当插入第101条内容后，原本的查询结果并没有错误，仍可以使用。但这又会产生缓存穿透问题，随着评价内容的不断增多，用户可能查询id<101的内容、id<100的内容、id<99的内容，redis产生了太多的KEY，产生了太多的数据冗余，浪费内存，拖累整体性能。

如何做到稳定的写和高效的读，又能做到低频的查库和节约的内存呢？



#### 异构系统的协作

一个激进的解决方案是，使用Redis和Mysql作为双主库。写内容时，既写入数据库，又写入redis，读数据时，只读redis。redis的内容与数据库的内容时刻保持同步，这样就不用查询数据库了，而且占用内存也没有冗余。这是个好的思路，我们从这个方向来设计评价内容的查询。

既然已经决定只从redis读取内容了，那么为什么还要写入数据库呢？可以有以下几个原因：

1. 数据库可以存储全量的内容，而redis一般只保留最近的几百条内容
2. 数据库可以用于离线分析，生成报表，在这方面redis的数据结构和命令接口使它很难做到
3. 数据库可以用于容灾恢复，当redis数据丢失时，可以用数据库中的内容来恢复它

总而言之，数据库最重要的作用是：**连接**，以一个通用的规范，作为一个通用的语言无关的平台，连接外部，是不可替代的。在写内容时，需要确保写入到数据库，收到写入成功的消息后，再写入redis。

基于这个方案，需要解决如下问题：

1. 如果写入redis失败了，那么是需要回滚还是重试？
2. redis使用什么数据结构保持评价内容列表？
3. redis保存全部的评价列表还是部分的评价列表？
4. 如果redis内容丢失了要如何恢复？
5. 如果用户更新了名称、头像，redis中的数据要如何做对应更新？

我们发现，简单地将redis作为权威存储会产生很多不可调和的一致性问题，实际应用中会出现千奇百怪的各种原因使得redis与数据库的内容不一致。所以，“权威存储”不可以有两个，必须一主一副。既然确定了数据库作为权威存储，每次写入都实时同步到数据库，那么redis就作为临时副本，每隔一段时间就使之过期，需要重新同步内容。

所以在之前的方案上，加入定期更新redis这一步，就能大体解决上面的问题：

1. 写入redis失败，既不回滚，也不重试，回滚会导致设计过度复杂（因为回滚会失败），重试会产生重复性问题（因为写入redis成功未必会返回成功，可能网络传输返回时故障），而是依托于定期更新这一机制，使得这种不一致性状态不会一直持续。
2. 使用列表作为数据结构，有利于范围查询，有利于数据添加。
3. redis保存部分的评价列表，需求是允许的，很多评价系统都只给用户显示最近前几百条消息。
4. redis内容丢失，或者是冷启动，缓存键相当于过期，它会自动加载内容到缓存。
5. 用户数据更新只需要把评价表对应的用户信息字段更新，无需主动修改redis，定期更新这一机制会保证redis最终与数据库中的数据保持一致，最终显示正确的信息。



#### 写入流程

添加评价时的逻辑流程图如下：

![image-20210423162444533](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210423162444533.png)

从设计图中可以看出，我在具体实现上一节的方案时，做了很多改进：

1. 检查是否需要更新以及更新的过程是异步的，在单独的队列中执行，不会阻塞当前线程。
2. 引入“代理键”的概念，给代理键设置过期时间而不给评价列表键设置过期时间。当代理键过期时，代表对应的评价列表缓存是过期的，但是不会真的过期，仍然可以用于读取，这有利于防止缓存击穿，并且在更新阶段甚至更新失败后仍可以提供查询。
3. 检查代理键过期与插入评价内容与获取分布式锁都在一个lua脚本中执行，我称之为“评价分页锁”。
4. 删除列表、添加内容、重置代理键、释放锁，也在一个脚本中执行。其实是利用原子特性减少中间状态，让评价写入流程不需要考虑重置代理键完成却未释放锁等等情况，让系统更稳定可控。



这样设计的话，写入操作是不会触发数据库查询的，就算代理键过期，也只有一个线程来负责重新加载这个操作，极大地降低了数据库的压力。



#### 查询流程

查询评价时的逻辑流程图如下：

![image-20210423162648447](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210423162648447.png)

评价缓存键是永远不会过期的，所以不论怎样，查询服务只要查询这个缓存键对应范围的列表即可。唯一需要注意的，就是当代理键过期时，需要执行重新加载流程，这一步和插入评价后所要做的是类似的。

查询触发的定期更新机制是有必要的，因为有很多无人问津的内容没有新增评价，或者删除评价时，旧内容总不能一致保持不变。查询时的锁和写入时的锁是一个锁，这是为了保证查询数据库写入redis这一段时间内，阻塞新内容写入redis，保护一致性。



#### 独特的分布式锁

我将redis的读写命令与分布式的实现命令SETNX融入到一起，设计模块化的脚本命令，充分发挥reids的原子性特性，减少中间状态，减少请求数量，让锁的语义更丰富，更贴合实际。

lua_add_remark.lua：

```lua
--添加评价的预置脚本
--params    1            2             3
--KEYS      代理键名       评价键名       锁键名
--ARGV      评价内容       评价id        锁有效期
--return    0:成功添加评价  1:加锁失败，遗弃   2:加锁失败，需要重试   3:加锁成功

--代理键是否过期
if redis.call('EXISTS', KEYS[1]) == 1 then
    --未过期，添加评价
    redis.call('LPUSH', KEYS[2], ARGV[1])
    redis.call('LTRIM', KEYS[2], 0, 200)
    return 0
else
    local v = redis.call('GET', KEYS[3])
    if v then
        --锁未过期，加锁失败
        if ARGV[2] > v then
            --待添加的评价的id大于当前id，说明是新内容，需要客户端重试
            return 2
        else
            --否则需要遗弃，因为加锁成功的客户端会帮它加载到redis
            return 1
        end
    else
        --加锁成功
        redis.call('SET', KEYS[3], ARGV[2])
        redis.call('EXPIRE',KEYS[3], ARGV[3])
        return 3
    end
end
```



lua_read_remark.lua

```lua
--查询评价的预置脚本
--params    0            1              2
--KEYS      代理键名       评价键名       锁键名
--ARGV      锁过期时间

--代理键是否过期
if redis.call('EXISTS', KEYS[1]) == 1 then
    --未过期
    return -1
else
    --获取头部内容的id
    local content = redis.call('LINDEX', KEYS[2], 0)
    local v = 0
    if content then
        local remarkJson = cjson.decode(content)
        v = cjson.decode(remarkJson)["id"]
    end
    if redis.call('SETNX', KEYS[3], v) == 1 then
        redis.call('EXPIRE', KEYS[3], ARGV[1])
        return v
    else
        --加锁失败，正常返回内容
        return -1
    end
end
```



lua_swap_remark.lua

```lua
--重置评价内容并释放评价分页锁
--params    1            2              3
--KEYS      评价键名       代理键名        锁键名
--ARGV      代理键有效期    锁签名          评价内容数组

--只有当签名一致时（是自己施加的锁），才执行释放锁的操作
if redis.call('GET', KEYS[3]) == ARGV[2]
then
    local remarkJson = cjson.decode(ARGV[3])
    --删除并重建列表（否则只会在原列表上增加，会产生内存泄露）
    redis.call('DEL', KEYS[1])
    for i= 1, #remarkJson do
        redis.call('RPUSH', KEYS[1], cjson.encode(remarkJson[i]) .. "")
    end

    --重置代理键
    redis.call('SET', KEYS[2], 1)
    redis.call('EXPIRE', KEYS[2], ARGV[1])

    --释放分页锁
    redis.call('DEL', KEYS[3])
end
```



防踩坑小记：

1. lua的返回值，不论有多小，都用Lang对象来接
2. KEYS参数用且只能于表示键名，ARGV参数用且只能于表示参数内容，即便都是字符串
3. if中一定要做等值判断，否则大概率是非预期的
4. cjson.decode返回的数据类型是table，不能直接转换为字符串



#### 封装评价的Redis操作

```java
/**
 * 评价redis缓存
 */
@Repository
public class RemarkRedis {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> pushAndLockScript;
    private final DefaultRedisScript<Long> readAndLockScript;
    private final DefaultRedisScript<Object> swapScript;

    public RemarkRedis(RedisTemplate<String, Object> redisTemplate, DefaultRedisScript<Long> pushAndLockScript, DefaultRedisScript<Object> swapScript, DefaultRedisScript<Long> readAndLockScript) {
        this.redisTemplate = redisTemplate;
        this.pushAndLockScript = pushAndLockScript;
        this.swapScript = swapScript;
        this.readAndLockScript = readAndLockScript;
    }

    /**
     * 添加评价，并获取评价分页锁
     *
     * @param remark 评价
     * @return 评价锁获取状态枚举
     */
    public RemarkLockEnum pushAndLock(Remark remark) {
        remark.setCreateTime(Sunday.getTime());
        List<String> list = new ArrayList<>();

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + remark.getItemId());

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + remark.getItemId());

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", remark.getItemId()));

        //评分键
        list.add(RedisKeyConstants.REMARK_SCORE_PREFIX + remark.getItemId());

        Long code = redisTemplate.execute(pushAndLockScript, list, JSONObject.fromObject(remark).toString(), remark.getId(), 2, remark.getScore());
        if (code == null)
            return RemarkLockEnum.ERROR;
        if (code == 0)
            return RemarkLockEnum.SUCCESS;
        if (code == 1)
            return RemarkLockEnum.ABORT;
        if (code == 2)
            return RemarkLockEnum.REDO;

        return RemarkLockEnum.LOCK_OK;
    }

    /**
     * 查询评价，并获取评价分页锁
     */
    public Long readAndLock(String itemId) {
        List<String> list = new ArrayList<>();

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + itemId);

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + itemId);

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", itemId));

        return redisTemplate.execute(readAndLockScript, list, 4);
    }

    /**
     * 更新评价缓存，并释放评价分页锁
     *
     * @param itemId 内容id
     * @param id     锁的签名id
     * @param contents   内容列表
     */
    public void swapAndRelease(String itemId, long id, List<Remark> contents) {
        List<String> list = new ArrayList<>();

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + itemId);

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + itemId);

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", itemId));

        //评价内容数组
        JSONArray array = new JSONArray();
        if (contents != null) {
            for (Object o : contents) {
                array.add(JSONObject.fromObject(o));
            }
        }

        redisTemplate.execute(swapScript, list, 3600 * 6, id, array);
    }

    /**
     * 查询评价列表的某个范围
     *
     * @param itemId 内容id
     * @param start  开始索引
     * @param stop   中止索引
     */
    public JSONArray queryRange(String itemId, int start, int stop) {
        String key = RedisKeyConstants.REMARK_PREFIX + itemId;
        List<Object> list = redisTemplate.opsForList().range(key, start, stop);
        JSONArray array = new JSONArray();
        if (list != null) {
            for (Object o : list) {
                array.add(JSONObject.fromObject(o));
            }
        }

        return array;
    }
}
```



#### 封装异步队列

```java
/**
 * 线程安全的异步执行队列
 */
public final class RemarkQueue {
    //线程数
    private final int nThreads;

    //阻塞队列长度
    private final static int BLOCK_LENGTH = 1000_000;

    /*
     * 线程池组，每个线程池只有一个线程。
     * 不适用线程池自带的负载均衡策略，而是使用自制的根据项目id的哈希负载均衡策略
     * 为了降低锁冲突概率。
     */
    private final ExecutorService[] threads;

    public RemarkQueue(int nThreads) {
        this.nThreads = nThreads;
        this.threads = new ExecutorService[nThreads];

        for (int i = 0; i < nThreads; i++) {
            int finalI = i;
            this.threads[i] = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(BLOCK_LENGTH),
                    r -> new Thread(r, "RemarkQueue:" + finalI),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
    }

    /**
     * 加入异步执行队列
     * @param itemId 商品id
     */
    public void push(String itemId, Runnable runnable){
        if (StringUtils.isEmpty(itemId)){
            return;
        }

        //根据商品id决定使用哪个线程
        int slot = Math.abs(itemId.hashCode() % nThreads);
        threads[slot].execute(runnable);
    }
}
```



#### 修改新增方法

新增评价方法，需要加入写入数据库成功后，异步写入redis的代码：

```java
public boolean createRemark(/*valid*/ Remark remark) throws ExceedAuthorizedAccessException {
	//....

        long remarkId = remarkRepository.addRemark(remark);
        if (remarkId > 0) {
            //如果插入成功，则同步到redis列表中
            remark.setId(remarkId);
            asyncAddRemark(remark);
            return true;
        }
    
    //....
}
```



addRemark修改为在插入成功后获取主键

```java
/**
 * 添加评价
 * @param remark 评价对象
 * @return 如果插入成功，返回插入后的自动主键，否则返回-1
 */
public long addRemark(/*valid*/ Remark remark) {
    final String sql = "insert into remark(consumer_id,item_id,order_id,score,header,content,images,user_name,user_face) values(?,?,?,?,?,?,?,?,?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    db.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, remark.getConsumerId());
        ps.setString(2, remark.getItemId());
        ps.setString(3, remark.getOrderId());
        ps.setShort(4, remark.getScore());
        ps.setString(5, remark.getHeader());
        ps.setString(6, remark.getContent());
        ps.setString(7, remark.getImages());
        ps.setString(8, remark.getUsername());
        ps.setString(9, remark.getUserface());
        return ps;
    }, keyHolder);

    if (keyHolder.getKey() == null){
        return -1;
    }

    return keyHolder.getKey().intValue();
}
```



asyncAddRemark方法：

```java
/**
 * 将用户发布的评价内容异步写入到redis列表中
 */
private void asyncAddRemark(Remark remark) {
    remarkQueue.push(remark.getItemId(), () -> {
        try {
            switch (remarkRedis.pushAndLock(remark)) {
                //加锁成功，需要加载数据库中的评价内容到redis
                case LOCK_OK -> reloadRemarks(remark.getItemId(), remark.getId());

                //加锁失败，等待片刻后重新加入队列
                case REDO -> DelayExecutor.executeLater(1000, () -> asyncAddRemark(remark));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //出现异常，丢弃任务，在一段时间内不一致，但是redis恢复后，并且key过期后，会最终达成一致
            log.warn("asyncRedisList Exception:{}, by:{}", e.getMessage(), remark);
        }
    });
}

//加载评价内容
private void reloadRemarks(String itemId, long code) {
    List<Remark> list = remarkRepository.listRemarks(itemId, code == 0 ? Integer.MAX_VALUE : code, SystemConstants.REMARK_MAX_DISPLAY_LENGTH);
    remarkRedis.swapAndRelease(itemId, code, list);
}
```



从数据库中查询评价内容的方法：

```java
/**
 * 查询商品关联的离curId最近的若干条评价
 * @param itemId 商品id
 * @param curId 查询的主键id不超过maxId
 * @param expectCount 期望数量
 * @return 返回离curId最新的expectCount条评价，如果小于这个数量，返回对应元素数量的列表，如果评价为空，返回长度=0的列表
 */
public List<Remark> listRemarks(/*not null*/ String itemId, long curId, int expectCount){
    if (expectCount <= 0)
        return new ArrayList<>();

    Assert.isTrue(expectCount <= MAX_LIST_SIZE, "不允许一次性查询过多内容");

    String sql = "select id,consumer_id,order_id,score,header,content,images,user_name,user_face,gmt_create from remark where item_id = ? and status = '1' and id <= ? order by id desc limit ?";
    return db.query(sql, (resultSet, i) -> {
        Remark remark = new Remark();
        remark.setId(resultSet.getLong(1));
        remark.setConsumerId(resultSet.getLong(2));
        remark.setOrderId(resultSet.getString(3));
        remark.setItemId(itemId);
        remark.setScore(resultSet.getShort(4));
        remark.setHeader(resultSet.getString(5));
        remark.setContent(resultSet.getString(6));
        remark.setImages(resultSet.getString(7));
        remark.setUsername(resultSet.getString(8));
        remark.setUserface(resultSet.getString(9));
        remark.setCreateTime(resultSet.getString(10));
        return remark;
    }, itemId, curId, expectCount);
}
```



评价删除时删除代理键即可：

```java
remarkRedis.refresh(remark.getItemId());
```



#### Read：查询评价

终于可以开始写查询评价服务了：

```java
/**
 * 查询商品关联的评价,按时间倒叙排序
 *
 * @param itemId 商品id
 * @param start  开始索引
 * @param stop   中止索引
 * @return 返回从curIndex开始，一个小于等于期望值数量的列表。如果无内容，返回长度为0的数组。
 */
@Cacheable(value = "remark")
public JSONArray listRemarks(/*not null*/ String itemId, int start, int stop) {
    try {
        //尝试施加读锁
        Long code = remarkRedis.readAndLock(itemId);
        if (code != null && code >= 0) {
            //加锁成功，需要加载数据库中的评价内容到redis
            remarkQueue.push(itemId, () -> reloadRemarks(itemId, code));
        }

        return remarkRedis.queryRange(itemId, start, stop);
    } catch (Exception e) {
        log.error("function RemarkService.listRemarks make exception:{} by:{},{},{}", e.getMessage(), itemId, start, stop);
        return SystemConstants.EMPTY_ARRAY;
    }
}
```



查询评价接口：

```java
/**
 * 查询商品关联的评价，一次查询固定的条目
 * @param itemId 商品id
 * @param start 当前查询坐标
 */
@GetMapping("/remark")
public APIBody listRemarks(String itemId, int start, Integer consumerId){
    Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不能为空");

    start--;
    JSONArray list = remarkService.listRemarks(itemId, start, start + SystemConstants.REMARK_MAX_LIST_LENGTH - 1);

    //原列表是从redis或db中读取的静态数据，而点赞数据每时每刻都在变化，分开获取这两个部分。
    return APIBody.buildSuccess(remarkService.appendVoteInfo(list, consumerId));
}
```



Postman测试接口：

![image-20210421164015219](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210421164015219.png)



#### 更快的查询

评价内容几乎是静态的，每一次都查询redis服务器着实有点浪费了，完全可以使用内存来缓存10秒内的数据，10秒时间内与世隔绝，任何的不一致都可以承受，这种实现方式将并发的查询请求化线为点，把对分布式服务的依赖降低了一个数量级。

应用程序内存是最可靠最不会出问题的资源，不像磁盘IO需要考虑冲突，网络IO需要考虑阻塞和中断，基于虚拟机的运行环境最不可能出错，换句话说，如果本地运行环境不可靠，那么也没法设计可靠的应用程序了。

内存缓存与分布式缓存最大的区别之一就是内存缓存没有未命中惩罚，内存读写的时间几乎是忽略不计的，引入一个内存缓存机制是零成本的（除了学习成本以外）。可以将缓存过期时间设置很短，毫不夸张的说，即便任何情况下本地缓存未命中，所消耗的时间也与不引入本地缓存机制的时间相差无几。 将热点数据放在本地做一级缓存，将非热点数据放在redis做二级缓存，最终数据存储在数据库系统中。

Caffeine是一个高性能的 Java 缓存库，使用 Window TinyLfu 回收策略，提供了一个近乎最佳的命中率。本着不重复发明轮子的宗旨，我们应该使用像Caffeine这样的开源软件。

使用内存缓存，要意识到分布式情况下的不一致性，不同服务器实例的缓存空间是不同的，相同的数据在不同实例内存下可能并不相同，内存也是有限的，需要考虑到这些局限性，不能滥用内存缓存。

使用Caffeine设计一个10秒的快缓存：

```java
/**
 * 评价缓存容量配置
 */
@Configuration
public class RemarkCacheConfig {
    @Bean("remarkListCache")
    //评价列表缓存
    public Cache<Object, Object> remarkListCache(){
        //一个元素大概5K
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(20000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .softValues()
                .build(key -> null);
    }

    @Bean
    public CacheManager caffeineCacheManager(Cache<Object, Object> remarkListCache) {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        List<CaffeineCache> caffeineCaches = new ArrayList<>();

        caffeineCaches.add(new CaffeineCache("remark", remarkListCache));

        simpleCacheManager.setCaches(caffeineCaches);
        return simpleCacheManager;
    }
}
```

然后，接口层加上@EnableCaching，对应方法加上@Cacheable(value = "remark")即可。



更进一步，静态评价内容可以使用CDN缓存，并设置一个过期更新机制，具体就不详述了。



#### 限流、熔断、降级

当并发越来越高时，为了保护服务器，产生了很多模式，比如断路器（熔断器）、舱壁（隔板、线程隔离）、限流（阻塞、快速失效）。本文不讨论这些模式的实现，而是推荐使用Alibaba Sentinel组件实现限流熔断降级。

我有认真读过Sentinel的源码，它的计算性能好，内存占用低，可以放心的引入。

参考文档：https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel

Sentinel流量塑性：

<img src="C:\Users\lushuntian\Desktop\收藏图片\sentinal 流量塑性.jpg" alt="sentinal 流量塑性" style="zoom: 50%;" />

Sentinel架构：

<img src="C:\Users\lushuntian\Desktop\收藏图片\Sentinel架构.png" alt="Sentinel架构" style="zoom: 67%;" />

Sentinel内部实现：

<img src="C:\Users\lushuntian\Desktop\收藏图片\Sentinel内部实现.png" alt="Sentinel内部实现" style="zoom: 80%;" />



#### 稳定状态

系统在无任何干预的情况下保持长久运行，这种状态称为稳定状态。每次有人操作服务器的时候，不管是有意无意，就都有可能引入不可预测的错误，需要尽可能地让人远离一个上线运行的系统。大多数需要登录系统的原因是要清除积累的日志或者数据。

任何积累资源的机制，都需要另外一种机制来清理它，就像中学计算题中提到的桶一样，桶以一定的速度随着数据的积累而填满，同时它也需要以同样或者更大的速度去消耗。有进有出的状态才是稳定状态，否则日志会溢出，数据库会变慢直至抛出异常。

我看了一下经典评价系统的实现，他们大多不支持跳页查询，而是一步步向下刷，就算是支持跳页查询，也只是支持有限的页数，那些早期的数据应该是被归档被删除了，总之就是不在原表中了。这种定期删除的机制使得表空间不是无限制的增长，而是缓慢的有节制的增加。

对于评价系统，不同商品之间的评价是完全是独立的，用itemId可以进行分隔，因此当数据膨胀时，可以很容易地进行分区分表。



#### 评分统计

在用户评价内容之后，系统会将评分纳入统计，生成摘要显示在页面上。类似下图：

![image-20210422195751193](https://sunday-picture.oss-cn-hangzhou.aliyuncs.com/image-20210422195751193.png)

先从数据结构上分析，虽然上图包含了很多信息，但本质上是一个评分-人数的简单哈希表，总人数、评分比率和均分都可以从哈希表中计算而来，不需要单独存储。

再从存储介质上考虑，所有评价、评分都完整存储在评价表中，比如下面的sql查询了每个分数段的评分：

```sql
select score,count(*) from remark where item_id = 'item-1' group by score order by score
```

在java程序中完全可以根据这个查询结果生成JsonArray对象输出，这样客户端就可以显示了。

如果评价表只包含部分评价内容，而历史悠久的评价内容已被清理删除，但是评分总数不能因删除而减少，这种情况可以创建一个评分摘要表，保存至今的摘要信息，新的评分不必立刻同步过去，而是由sql脚本定期去更新，查询时，将摘要信息表的数据融合评价内容表的最新数据，生成最终要展示给用户的数据。

关于缓存，可以简单地使用二级缓存模式，由内存做一级缓存，redis做二级缓存，内存的过期时间是10秒，redis的过期时间是1分钟，这样很好地利用数据的用户无关性特点。

不过，参考之前的设计方法，既然利用redis做缓存，那就使用双写模式，插入评价时既写入数据库的评分，也写入redis的评分，这样就不需要设置1分钟那么短的过期时间（上百万条商品，每个都1分钟过期，那么对数据库的压力仍然很大），而是6小时的过期时间（或者是永不过期，除非是内存挂掉，但这时又会触发冷启动机制，所以没问题）。

查询方法：

```java
/**
 * 查询商品的评价列表
 * @param itemId 商品id
 * @return 评价列表，如果商品不存在或者查询为空，返回size=0的列表
 */
public List<ScoreInfo> listScores(/*not null*/ String itemId){
    String sql = "select score,count(*) from remark where item_id = ? group by score order by score";
    return db.query(sql, (resultSet, i) -> {
        ScoreInfo scoreInfo = new ScoreInfo();
        scoreInfo.setScore(resultSet.getShort(1));
        scoreInfo.setCount(resultSet.getLong(2));
        return scoreInfo;
    }, itemId);
}
```



redis的操作：

```java
/**
 * 读取评分列表
 */
public Map<Object, Object> readScoreData(String itemId) {
    return redisTemplate.opsForHash().entries(RedisKeyConstants.REMARK_SCORE_PREFIX + itemId);
}

/**
 * 保存评分列表
 */
public void saveScoreData(String itemId, Map<Object, Object> map) {
    redisTemplate.opsForHash().putAll(RedisKeyConstants.REMARK_SCORE_PREFIX + itemId, map);
}
```



在插入评价时修改评分：

```lua
--当且仅当评分键存在时修改评分，避免覆盖冷启动
if redis.call('EXISTS', KEYS[4]) == 1 then
    redis.call('HINCRBY', KEYS[4], ARGV[4], 1)
end
```



查询评分服务代码：

```java
/**
 * 查询评分列表
 *
 * @param itemId 商品id
 */
@Cacheable(value = "score")
public JSONArray listScores(/*not null*/ String itemId) {
    try {
        Map<Object, Object> map = remarkRedis.readScoreData(itemId);

        if (map == null || map.isEmpty()) {
            //冷启动时加载
            if (remarkRedis.lockScoreData(itemId)) {
                List<ScoreInfo> list = remarkRepository.listScores(itemId);
                list.add(new ScoreInfo((short) 0, 0));
                map = new HashMap<>();
                for (ScoreInfo scoreInfo : list) {
                    map.put(String.valueOf(scoreInfo.getScore()), scoreInfo.getCount());
                }

                remarkRedis.saveScoreData(itemId, map);
                return JSONArray.fromObject(list);
            } else {
                //阻塞等待内容加载完成，不适用递归，避免死循环
                int count = 10;
                while (count-- > 0) {
                    Thread.sleep(1000);
                    map = remarkRedis.readScoreData(itemId);
                    if (map != null && !map.isEmpty())
                        break;
                }

                if (map == null || map.isEmpty())
                    return SystemConstants.EMPTY_ARRAY;
            }
        }

        JSONArray array = new JSONArray();
        map.forEach((o, o2) -> {
            JSONObject object = new JSONObject();
            object.put("score", o);
            object.put("count", o2);
            array.add(object);
        });

        return array;
    } catch (Exception e) {
        log.error("function RemarkService.listScores make exception:{} by:{}", e.getMessage(), itemId);
        return SystemConstants.EMPTY_ARRAY;
    }
}
```



查询评分接口：

```java
/**
 * 查询商品评分摘要
 * @param itemId 商品id
 */
@GetMapping("/scores")
public APIBody listScores(String itemId){
    Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不能为空");
    JSONArray list = remarkService.listScores(itemId);
    return APIBody.buildSuccess(list);
}
```



#### 后续

评价系统，怎么也需要可以对评价内容点赞吧，且看下一篇文章。








