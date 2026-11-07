/*
SQLyog Ultimate - MySQL GUI v8.2 
MySQL - 8.0.30 : Database - yygh_order
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`yygh_order` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `yygh_order`;

/*Table structure for table `order_info` */

DROP TABLE IF EXISTS `order_info`;

CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint DEFAULT NULL,
  `out_trade_no` varchar(300) DEFAULT NULL COMMENT '订单交易号',
  `hoscode` varchar(30) DEFAULT NULL COMMENT '医院编号',
  `hosname` varchar(100) DEFAULT NULL COMMENT '医院名称',
  `depcode` varchar(30) DEFAULT NULL COMMENT '科室编号',
  `depname` varchar(20) DEFAULT NULL COMMENT '科室名称',
  `title` varchar(20) DEFAULT NULL COMMENT '医生职称',
  `hos_schedule_id` varchar(50) DEFAULT NULL COMMENT '排班编号（医院自己的排班主键）',
  `reserve_date` date DEFAULT NULL COMMENT '安排日期',
  `reserve_time` tinyint DEFAULT '0' COMMENT '安排时间（0：上午 1：下午）',
  `patient_id` bigint DEFAULT NULL COMMENT '就诊人id',
  `patient_name` varchar(20) DEFAULT NULL COMMENT '就诊人名称',
  `patient_phone` varchar(11) DEFAULT NULL COMMENT '就诊人手机',
  `hos_record_id` varchar(30) DEFAULT NULL COMMENT '预约记录唯一标识（医院预约记录主键）',
  `number` int DEFAULT NULL COMMENT '预约号序',
  `fetch_time` varchar(50) DEFAULT NULL COMMENT '建议取号时间',
  `fetch_address` varchar(255) DEFAULT NULL COMMENT '取号地点',
  `amount` decimal(10,0) DEFAULT NULL COMMENT '医事服务费',
  `quit_time` datetime DEFAULT NULL COMMENT '退号时间',
  `order_status` tinyint DEFAULT NULL COMMENT '订单状态',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_hoscode` (`hoscode`),
  KEY `idx_hos_schedule_id` (`hos_schedule_id`),
  KEY `idx_hos_record_id` (`hos_record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb3 COMMENT='订单表';

/*Data for the table `order_info` */

insert  into `order_info`(`id`,`user_id`,`out_trade_no`,`hoscode`,`hosname`,`depcode`,`depname`,`title`,`hos_schedule_id`,`reserve_date`,`reserve_time`,`patient_id`,`patient_name`,`patient_phone`,`hos_record_id`,`number`,`fetch_time`,`fetch_address`,`amount`,`quit_time`,`order_status`,`create_time`,`update_time`,`is_deleted`) values (14,26,'162825325628088','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','610282d816d1020127ebf811','2021-08-08',0,7,'张翠山1','15611248741','12',25,'2021-08-0709:00前','一层114窗口','100','2021-08-06 15:30:00',1,'2021-08-06 20:34:16','2021-08-07 14:15:28',0),(15,26,'162829645831986','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','610282d816d1020127ebf812','2021-08-07',1,7,'张翠山1','15611248741','13',7,'2021-08-0809:00前','一层114窗口','100','2021-08-07 15:30:00',1,'2021-08-07 08:34:18','2021-08-07 14:15:19',0),(16,26,'162830304778619','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','610282d816d1020127ebf812','2021-08-07',1,7,'张翠山1','15611248741','14',8,'2021-08-0809:00前','一层114窗口','100','2021-08-07 15:30:00',1,'2021-08-07 10:24:07','2021-08-07 14:15:57',0),(17,26,'16298108217849','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','610282d916d1020127ebf81d','2021-08-26',0,7,'张翠山1','15611248741','15',33,'2021-08-2609:00前','一层114窗口','100','2021-08-25 15:30:00',0,'2021-08-24 21:13:41','2021-08-24 21:13:41',0),(18,26,'162985687872461','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','610282d916d1020127ebf81d','2021-08-26',0,7,'张翠山1','15611248741','16',34,'2021-08-2609:00前','一层114窗口','100','2021-08-25 15:30:00',0,'2021-08-25 10:01:18','2021-08-25 10:01:19',0),(19,26,'175094248816888','10000','北京协和医院','200040878','多发性硬化专科门诊','医师','685d41e6820de577c8f54c44','2025-06-27',0,7,'张翠山1','15611248741','17',12,'2025-06-2709:00前','一层114窗口','100','2025-06-26 15:30:00',0,'2025-06-26 20:54:48','2025-06-26 20:54:48',0),(20,26,'17509537164596','10000','北京协和医院','200040878','多发性硬化专科门诊','医师','685d41e6820de577c8f54c44','2025-06-27',0,7,'张翠山1','15611248741','18',13,'2025-06-2709:00前','一层114窗口','100','2025-06-26 15:30:00',0,'2025-06-27 00:01:56','2025-06-27 00:01:56',0),(21,26,'175095407747447','10000','北京协和医院','200040878','多发性硬化专科门诊','医师','685d41e6820de577c8f54c44','2025-06-27',0,7,'张翠山1','15611248741','19',14,'2025-06-2709:00前','一层114窗口','100','2025-06-26 15:30:00',0,'2025-06-27 00:07:57','2025-06-27 00:07:57',0),(22,26,'175095410990711','10000','北京协和医院','200040878','多发性硬化专科门诊','医师','685d41e6820de577c8f54c44','2025-06-27',0,4,'1','1','20',15,'2025-06-2709:00前','一层114窗口','100','2025-06-26 15:30:00',0,'2025-06-27 00:08:29','2025-06-27 00:08:29',0),(23,26,'175682585334454','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','685d41e6820de577c8f54c3d','2025-09-03',1,7,'张翠山1','15611248741','21',18,'2025-09-0309:00前','一层114窗口','100','2025-09-02 15:30:00',-1,'2025-09-02 23:10:53','2025-09-02 23:10:53',0),(24,26,'175682615118927','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','685d41e6820de577c8f54c3d','2025-09-03',1,7,'张翠山1','15611248741','22',19,'2025-09-0309:00前','一层114窗口','100','2025-09-02 15:30:00',-1,'2025-09-02 23:15:51','2025-09-02 23:15:51',0),(25,26,'175682724172436','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','685d41e6820de577c8f54c3c','2025-09-03',0,4,'1','1','23',35,'2025-09-0309:00前','一层114窗口','100','2025-09-02 15:30:00',0,'2025-09-02 23:34:01','2025-09-02 23:34:01',0),(26,26,'175682784148156','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','685d41e6820de577c8f54c3d','2025-09-03',1,4,'1','1','24',20,'2025-09-0309:00前','一层114窗口','100','2025-09-02 15:30:00',-1,'2025-09-02 23:44:01','2025-09-02 23:44:01',0),(27,26,'175682858856593','10000','北京协和医院','200040878','多发性硬化专科门诊','副主任医师','685d41e6820de577c8f54c3d','2025-09-03',1,7,'张翠山1','15611248741','25',21,'2025-09-0309:00前','一层114窗口','100','2025-09-02 15:30:00',-1,'2025-09-02 23:56:28','2025-09-02 23:56:28',0),(28,26,'175682871831697','10000','北京协和医院','200040878','多发性硬化专科门诊','医师','685d41e6820de577c8f54c3e','2025-09-04',0,7,'张翠山1','15611248741','26',12,'2025-09-0409:00前','一层114窗口','100','2025-09-03 15:30:00',-1,'2025-09-02 23:58:38','2025-09-02 23:58:38',0);

/*Table structure for table `payment_info` */

DROP TABLE IF EXISTS `payment_info`;

CREATE TABLE `payment_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编号',
  `out_trade_no` varchar(30) DEFAULT NULL COMMENT '对外业务编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单id',
  `payment_type` tinyint(1) DEFAULT NULL COMMENT '支付类型（微信 支付宝）',
  `trade_no` varchar(50) DEFAULT NULL COMMENT '交易编号',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额',
  `subject` varchar(200) DEFAULT NULL COMMENT '交易内容',
  `payment_status` tinyint DEFAULT NULL COMMENT '支付状态',
  `callback_time` datetime DEFAULT NULL COMMENT '回调时间',
  `callback_content` varchar(1000) DEFAULT NULL COMMENT '回调信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3 COMMENT='支付信息表';

/*Data for the table `payment_info` */

insert  into `payment_info`(`id`,`out_trade_no`,`order_id`,`payment_type`,`trade_no`,`total_amount`,`subject`,`payment_status`,`callback_time`,`callback_content`,`create_time`,`update_time`,`is_deleted`) values (8,'162825325628088',14,2,'4200001195202108062597287481','100.00','2021-08-07|北京协和医院|多发性硬化专科门诊|副主任医师',2,'2021-08-06 20:34:34','{transaction_id=4200001195202108062597287481, nonce_str=77VOukaGjAUQ6vwg, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuPGPKdUeOuzT0NB3vdfkjMA, sign=FF75F931A9A0A05DB1683051DBCE695B, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=162825325628088, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20210806203445, is_subscribe=N, return_code=SUCCESS}','2021-08-06 20:34:18','2021-08-06 20:34:33',0),(9,'162829645831986',15,2,'4200001176202108075220715918','100.00','2021-08-08|北京协和医院|多发性硬化专科门诊|副主任医师',2,'2021-08-07 08:35:17','{transaction_id=4200001176202108075220715918, nonce_str=m9SHs0Lrvz6e2jZj, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuPGPKdUeOuzT0NB3vdfkjMA, sign=C1156F9B91B99A2B959F670E2E2C68E6, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=162829645831986, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20210807083527, is_subscribe=N, return_code=SUCCESS}','2021-08-07 08:35:04','2021-08-07 08:35:16',0),(10,'162830304778619',16,2,'4200001155202108076308258973','100.00','2021-08-08|北京协和医院|多发性硬化专科门诊|副主任医师',2,'2021-08-07 10:24:32','{transaction_id=4200001155202108076308258973, nonce_str=RrV4VR2nnrVrIfGH, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuPGPKdUeOuzT0NB3vdfkjMA, sign=E3CE4A31F2C7C72F3B767CA2E7A5F58B, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=162830304778619, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20210807102444, is_subscribe=N, return_code=SUCCESS}','2021-08-07 10:24:22','2021-08-07 10:24:32',0),(11,'16298108217849',17,2,NULL,'100.00','2021-08-26|北京协和医院|多发性硬化专科门诊|副主任医师',1,NULL,NULL,'2021-08-24 21:13:46','2021-08-24 21:13:45',0),(12,'162985687872461',18,2,NULL,'100.00','2021-08-26|北京协和医院|多发性硬化专科门诊|副主任医师',1,NULL,NULL,'2021-08-25 10:01:28','2021-08-25 10:01:28',0);

/*Table structure for table `refund_info` */

DROP TABLE IF EXISTS `refund_info`;

CREATE TABLE `refund_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编号',
  `out_trade_no` varchar(50) DEFAULT NULL COMMENT '对外业务编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单编号',
  `payment_type` tinyint DEFAULT NULL COMMENT '支付类型（微信 支付宝）',
  `trade_no` varchar(50) DEFAULT NULL COMMENT '交易编号',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `subject` varchar(200) DEFAULT NULL COMMENT '交易内容',
  `refund_status` tinyint DEFAULT NULL COMMENT '退款状态',
  `callback_content` varchar(1000) DEFAULT NULL COMMENT '回调信息',
  `callback_time` datetime DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COMMENT='退款信息表';

/*Data for the table `refund_info` */

insert  into `refund_info`(`id`,`out_trade_no`,`order_id`,`payment_type`,`trade_no`,`total_amount`,`subject`,`refund_status`,`callback_content`,`callback_time`,`create_time`,`update_time`,`is_deleted`) values (3,'162829645831986',15,2,NULL,'100.00','2021-08-08|北京协和医院|多发性硬化专科门诊|副主任医师',1,NULL,NULL,'2021-08-07 10:20:04','2021-08-07 10:20:04',0),(4,'162830304778619',16,2,'50301609052021080711349778191','100.00','2021-08-08|北京协和医院|多发性硬化专科门诊|副主任医师',2,'{\"transaction_id\":\"4200001155202108076308258973\",\"nonce_str\":\"bmu2Rk0tlrjJirQ2\",\"out_refund_no\":\"tk162830304778619\",\"sign\":\"9BDCCE9B82D4304E5C3A42C658C0DC0D\",\"return_msg\":\"OK\",\"mch_id\":\"1558950191\",\"refund_id\":\"50301609052021080711349778191\",\"cash_fee\":\"1\",\"out_trade_no\":\"162830304778619\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wx74862e0dfcf69954\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}','2021-08-07 10:25:27','2021-08-07 10:25:25','2021-08-07 10:25:27',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
