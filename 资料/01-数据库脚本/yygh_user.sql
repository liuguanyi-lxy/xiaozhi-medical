/*
SQLyog Ultimate - MySQL GUI v8.2 
MySQL - 8.0.30 : Database - yygh_user
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`yygh_user` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `yygh_user`;

/*Table structure for table `patient` */

DROP TABLE IF EXISTS `patient`;

CREATE TABLE `patient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `name` varchar(20) DEFAULT NULL COMMENT '姓名',
  `certificates_type` varchar(3) DEFAULT NULL COMMENT '证件类型',
  `certificates_no` varchar(30) DEFAULT NULL COMMENT '证件编号',
  `sex` tinyint DEFAULT NULL COMMENT '性别',
  `birthdate` date DEFAULT NULL COMMENT '出生年月',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机',
  `is_marry` tinyint DEFAULT NULL COMMENT '是否结婚',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省code',
  `city_code` varchar(20) DEFAULT NULL COMMENT '市code',
  `district_code` varchar(20) DEFAULT NULL COMMENT '区code',
  `address` varchar(100) DEFAULT NULL COMMENT '详情地址',
  `contacts_name` varchar(20) DEFAULT NULL COMMENT '联系人姓名',
  `contacts_certificates_type` varchar(3) DEFAULT NULL COMMENT '联系人证件类型',
  `contacts_certificates_no` varchar(30) DEFAULT NULL COMMENT '联系人证件号',
  `contacts_phone` varchar(11) DEFAULT NULL COMMENT '联系人手机',
  `card_no` varchar(50) DEFAULT NULL COMMENT '就诊卡号',
  `is_insure` tinyint DEFAULT '0' COMMENT '是否有医保',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0：默认 1：已认证）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3 COMMENT='就诊人表';

/*Data for the table `patient` */

insert  into `patient`(`id`,`user_id`,`name`,`certificates_type`,`certificates_no`,`sex`,`birthdate`,`phone`,`is_marry`,`province_code`,`city_code`,`district_code`,`address`,`contacts_name`,`contacts_certificates_type`,`contacts_certificates_no`,`contacts_phone`,`card_no`,`is_insure`,`status`,`create_time`,`update_time`,`is_deleted`) values (4,26,'乔峰','10','130111199909092345',1,'2021-07-31','1',0,'110000','110100','110101','1','','','','',NULL,0,0,'2021-08-01 20:13:11','2025-09-03 14:36:23',0),(5,26,'虚竹','10','360380200201019087',1,'2021-07-31','2',0,'140000','140100','140101','2','','','','',NULL,0,0,'2021-08-01 20:13:25','2025-09-03 14:36:26',0),(6,26,'段誉','10','321234200405215637',1,'2021-07-31','3',0,'110000','110100','110101','3','','','','',NULL,0,0,'2021-08-01 20:13:39','2025-09-03 14:36:30',0),(7,26,'张翠山1','10','123456789099',1,'2021-07-31','15611248741',0,'110000','110100','110101','北京市昌平区','测试1','10','222','111',NULL,0,0,'2021-08-04 10:23:22','2021-08-04 20:20:54',0);

/*Table structure for table `user_info` */

DROP TABLE IF EXISTS `user_info`;

CREATE TABLE `user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `nick_name` varchar(20) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(11) NOT NULL DEFAULT '' COMMENT '手机号',
  `name` varchar(20) DEFAULT NULL COMMENT '用户姓名',
  `certificates_type` varchar(3) DEFAULT NULL COMMENT '证件类型',
  `certificates_no` varchar(30) DEFAULT NULL COMMENT '证件编号',
  `certificates_url` varchar(200) DEFAULT NULL COMMENT '证件路径',
  `auth_status` tinyint NOT NULL DEFAULT '0' COMMENT '认证状态（0：未认证 1：认证中 2：认证成功 -1：认证失败）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0：锁定 1：正常）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  KEY `uk_mobile` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb3 COMMENT='用户表';

/*Data for the table `user_info` */

insert  into `user_info`(`id`,`openid`,`nick_name`,`phone`,`name`,`certificates_type`,`certificates_no`,`certificates_url`,`auth_status`,`status`,`create_time`,`update_time`,`is_deleted`) values (1,'a9_AA80U3ER567UOP00QQoKKGY86','测试','1560000000','test','身份证','360380199909092888','http',0,0,'2021-08-04 11:19:06','2025-09-03 14:38:08',0),(26,'o3_SC50j3CKHoADxRmZ5oQT8MpB0','尚硅谷','1350000000','张三丰','身份证','360380199909092341','https://test-0317.oss-cn-beijing.aliyuncs.com/2021/08/01/f322dabdd949430eb1ac5b030b48608b001.png',2,1,'2021-08-01 17:49:10','2025-09-03 14:38:00',0);

/*Table structure for table `user_login_record` */

DROP TABLE IF EXISTS `user_login_record`;

CREATE TABLE `user_login_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `ip` varchar(32) DEFAULT NULL COMMENT 'ip',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除(1:已删除，0:未删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户登录记录表';

/*Data for the table `user_login_record` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
