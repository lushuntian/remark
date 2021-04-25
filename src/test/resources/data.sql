
insert into consumer(mobile,nickname) values('18600000001','小红');
insert into consumer(mobile,nickname) values('18600000002','小黑');
insert into consumer(mobile,nickname) values('18600000003','蓝天白云');
insert into consumer(mobile,nickname) values('18600000004','绿水青山');
insert into consumer(mobile,nickname) values('18600000005','beibei');
insert into consumer(mobile,nickname) values('18600000006','xiaoku');
insert into consumer(mobile,nickname) values('18600000007','天天');




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
