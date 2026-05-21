-- 在 travel.sql 建库建表之后执行本文件，可插入演示数据（按需修改或删除）
SET NAMES utf8mb4;

INSERT INTO `user` (`userID`, `userName`, `userPassword`, `userType`) VALUES
('U_DEMO1', 'demo', '123456', '2')
ON DUPLICATE KEY UPDATE `userName` = VALUES(`userName`);

INSERT INTO `ordinaryuser` (`userID`, `PhoneNumber`, `RealName`, `Gender`, `RegisterTime`) VALUES
('U_DEMO1', '13800138000', '演示用户', '男', NOW())
ON DUPLICATE KEY UPDATE `PhoneNumber` = VALUES(`PhoneNumber`);

INSERT INTO `landscape` (`LandscapeID`, `userID`, `Title`, `Content`, `Address`, `LandscapeTel`, `OpeningTime`, `Level`, `AuditState`, `PublishTime`) VALUES
('LS_DEMO_001', 'U_DEMO1', '南京夫子庙', '秦淮风光带核心景区，夜景与小吃闻名。', '南京市秦淮区贡院街', '025-52209788', '08:30-22:00', 'AAAAA', '审核通过', NOW()),
('LS_DEMO_002', 'U_DEMO1', '苏州拙政园', '中国四大名园之一，以水景与假山著称。', '苏州市姑苏区东北街', '0512-67510286', '07:30-17:30', 'AAAAA', '审核通过', NOW()),
('LS_DEMO_003', 'U_DEMO1', '杭州西湖', '世界文化遗产，环湖免费游览。', '杭州市西湖区龙井路', '0571-87179617', '全天开放', 'AAAAA', '审核通过', NOW())
ON DUPLICATE KEY UPDATE `Title` = VALUES(`Title`);

INSERT INTO `recommendationpost` (`RecomID`, `userID`, `Tag`, `Content`, `PublishTime`, `AuditState`) VALUES
('RE_DEMO_001', 'U_DEMO1', '南京三日游攻略', 'Day1 夫子庙与秦淮河；Day2 中山陵与明孝陵；Day3 西湖断桥。', NOW(), '审核通过')
ON DUPLICATE KEY UPDATE `Tag` = VALUES(`Tag`);

INSERT INTO `landcomment` (`CommentID`, `LandscapeID`, `userID`, `Content`, `PublishTime`) VALUES
('LCM_DEMO_001', 'LS_DEMO_001', 'U_DEMO1', '夜景很棒，建议错峰出行。', NOW())
ON DUPLICATE KEY UPDATE `Content` = VALUES(`Content`);

INSERT INTO `landlike` (`LikeID`, `LandscapeID`, `LinkUrl`, `userID`, `LikeTime`) VALUES
('LL_DEMO_001', 'LS_DEMO_001', '/landscapes/LS_DEMO_001', 'U_DEMO1', NOW())
ON DUPLICATE KEY UPDATE `LikeTime` = VALUES(`LikeTime`);

INSERT INTO `landcollect` (`CollectID`, `LandscapeID`, `LinkUrl`, `userID`, `CollectTime`) VALUES
('LC_DEMO_001', 'LS_DEMO_002', '/landscapes/LS_DEMO_002', 'U_DEMO1', NOW())
ON DUPLICATE KEY UPDATE `CollectTime` = VALUES(`CollectTime`);
