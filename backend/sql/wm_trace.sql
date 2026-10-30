-- ============================================================
-- 西瓜溯源 (watermelon traceability) schema + menu + role + dict
-- 依赖: 先导入 ry_20260417.sql (以及可选 quartz.sql)
-- 可重复执行: 业务表 drop & create; 菜单/字典/角色按固定 ID 先删后插
-- ============================================================
SET NAMES utf8mb4;

-- ----------------------------
-- 1、地块表
-- ----------------------------
drop table if exists wm_plot;
create table wm_plot (
  plot_id           bigint(20)      not null auto_increment    comment '地块ID',
  plot_name         varchar(64)     not null                   comment '地块名称',
  area_mu           decimal(10,2)   default null               comment '面积(亩)',
  address           varchar(255)    default ''                 comment '地址',
  longitude         decimal(10,6)   default null               comment '经度',
  latitude          decimal(10,6)   default null               comment '纬度',
  soil_type         varchar(64)     default ''                 comment '土壤类型',
  farmer_id         bigint(20)      default null               comment '负责农户用户ID',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (plot_id)
) engine=innodb auto_increment=100 comment = '西瓜地块表';

-- ----------------------------
-- 2、种植批次表（一批一码）
-- ----------------------------
drop table if exists wm_batch;
create table wm_batch (
  batch_id          bigint(20)      not null auto_increment    comment '批次ID',
  batch_code        varchar(32)     not null                   comment '批次溯源码',
  plot_id           bigint(20)      default null               comment '地块ID',
  variety           varchar(64)     default ''                 comment '品种',
  plant_date        date                                       comment '定植日期',
  harvest_date      date                                       comment '采收日期',
  expected_yield_kg decimal(12,2)   default null               comment '预计产量(kg)',
  brand_name        varchar(64)     default ''                 comment '品牌名称',
  brand_logo        varchar(255)    default ''                 comment '品牌LOGO',
  brand_desc        varchar(1000)   default ''                 comment '品牌介绍',
  cover_image       varchar(255)    default ''                 comment '封面图',
  qr_path           varchar(255)    default ''                 comment '二维码图片路径',
  scan_count        int(11)         default 0                  comment '扫码次数',
  farmer_id         bigint(20)      default null               comment '农户用户ID',
  status            char(1)         default '0'                comment '状态（0种植中 1已采收 2已归档）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (batch_id),
  unique key uk_batch_code (batch_code),
  key idx_batch_plot (plot_id),
  key idx_batch_farmer (farmer_id)
) engine=innodb auto_increment=100 comment = '西瓜种植批次表';

-- ----------------------------
-- 3、农事记录表
-- ----------------------------
drop table if exists wm_farm_record;
create table wm_farm_record (
  record_id         bigint(20)      not null auto_increment    comment '记录ID',
  batch_id          bigint(20)      not null                   comment '批次ID',
  record_type       varchar(16)     not null                   comment '农事类型（字典 wm_farm_type）',
  content           varchar(1000)   default ''                 comment '作业内容',
  input_name        varchar(128)    default ''                 comment '投入品名称(肥料/农药)',
  input_amount      varchar(64)     default ''                 comment '投入品用量',
  record_time       datetime                                   comment '作业时间',
  weather           varchar(32)     default ''                 comment '天气',
  images            varchar(2000)   default ''                 comment '图片(逗号分隔)',
  operator          varchar(64)     default ''                 comment '操作人',
  client_id         varchar(64)     default null               comment '客户端幂等ID(离线同步)',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (record_id),
  unique key uk_farm_client (client_id),
  key idx_farm_batch (batch_id, record_time)
) engine=innodb auto_increment=100 comment = '西瓜农事记录表';

-- ----------------------------
-- 4、品控记录表（糖度/果型/重量）
-- ----------------------------
drop table if exists wm_qc_record;
create table wm_qc_record (
  qc_id             bigint(20)      not null auto_increment    comment '品控ID',
  batch_id          bigint(20)      not null                   comment '批次ID',
  sample_no         varchar(32)     default ''                 comment '样品编号',
  sugar_center      decimal(4,1)    not null                   comment '中心糖度(°Brix)',
  sugar_edge        decimal(4,1)    default null               comment '边糖(°Brix)',
  fruit_shape       char(1)         default '1'                comment '果型（字典 wm_fruit_shape）',
  weight_kg         decimal(6,2)    default null               comment '单果重(kg)',
  grade_code        varchar(16)     default ''                 comment '判定等级编码',
  grade_name        varchar(32)     default ''                 comment '判定等级名称',
  check_time        datetime                                   comment '检测时间',
  images            varchar(2000)   default ''                 comment '图片(逗号分隔)',
  inspector         varchar(64)     default ''                 comment '检测人',
  client_id         varchar(64)     default null               comment '客户端幂等ID(离线同步)',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (qc_id),
  unique key uk_qc_client (client_id),
  key idx_qc_batch (batch_id)
) engine=innodb auto_increment=100 comment = '西瓜品控记录表';

-- ----------------------------
-- 5、分级标准表
-- 判定规则: 按 sort 升序取第一条满足全部条件的标准；全不满足为“等外”
-- 空值条件表示不限制
-- ----------------------------
drop table if exists wm_grade_standard;
create table wm_grade_standard (
  standard_id       bigint(20)      not null auto_increment    comment '标准ID',
  grade_code        varchar(16)     not null                   comment '等级编码',
  grade_name        varchar(32)     not null                   comment '等级名称',
  variety           varchar(64)     default ''                 comment '适用品种(空=全部)',
  sugar_center_min  decimal(4,1)    default null               comment '中心糖度下限',
  sugar_edge_min    decimal(4,1)    default null               comment '边糖下限',
  weight_min        decimal(6,2)    default null               comment '单果重下限(kg)',
  weight_max        decimal(6,2)    default null               comment '单果重上限(kg)',
  shape_allow       varchar(32)     default ''                 comment '允许果型(逗号分隔，空=不限)',
  sort              int(4)          default 0                  comment '优先级(越小越优)',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (standard_id)
) engine=innodb auto_increment=100 comment = '西瓜分级标准表';

-- 参考 DB3209 东台西瓜分等分级思路的示例阈值（可在管理端调整）
insert into wm_grade_standard values(1, 'SPECIAL', '特等果', '', 12.0, 9.0, 5.00, 9.00, '1',   1, '0', 'admin', sysdate(), '', null, '中心糖≥12，边糖≥9，5-9kg，果型端正');
insert into wm_grade_standard values(2, 'FIRST',   '一等果', '', 11.0, 8.0, 4.00, 10.00, '1,2', 2, '0', 'admin', sysdate(), '', null, '中心糖≥11，边糖≥8，4-10kg，果型端正/基本端正');
insert into wm_grade_standard values(3, 'SECOND',  '二等果', '', 10.0, null, 3.00, null, '1,2', 3, '0', 'admin', sysdate(), '', null, '中心糖≥10，≥3kg');

-- ----------------------------
-- 6、离线同步日志
-- ----------------------------
drop table if exists wm_sync_log;
create table wm_sync_log (
  log_id            bigint(20)      not null auto_increment    comment '日志ID',
  client_id         varchar(64)     not null                   comment '客户端幂等ID',
  data_type         varchar(16)     not null                   comment '数据类型(farm/qc)',
  result            varchar(16)     not null                   comment '结果(accepted/duplicate/rejected)',
  message           varchar(500)    default ''                 comment '说明',
  biz_id            bigint(20)      default null               comment '落库业务ID',
  create_by         varchar(64)     default ''                 comment '提交人',
  create_time       datetime                                   comment '提交时间',
  primary key (log_id),
  key idx_sync_client (client_id)
) engine=innodb auto_increment=1 comment = '离线同步日志';

-- ----------------------------
-- 7、字典 (dict_id 100-104, dict_code 1000+)
-- ----------------------------
delete from sys_dict_type where dict_id between 100 and 109;
delete from sys_dict_data where dict_code between 1000 and 1099;
insert into sys_dict_type values(100, '农事类型', 'wm_farm_type',    '0', 'admin', sysdate(), '', null, '西瓜农事作业类型');
insert into sys_dict_type values(101, '果型',     'wm_fruit_shape',  '0', 'admin', sysdate(), '', null, '西瓜果型');
insert into sys_dict_type values(102, '批次状态', 'wm_batch_status', '0', 'admin', sysdate(), '', null, '种植批次状态');

insert into sys_dict_data values(1000, 1, '整地',   'TILL',      'wm_farm_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1001, 2, '育苗定植', 'PLANT',   'wm_farm_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1002, 3, '施肥',   'FERTILIZE', 'wm_farm_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1003, 4, '灌溉',   'IRRIGATE',  'wm_farm_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1004, 5, '植保用药', 'PESTICIDE', 'wm_farm_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1005, 6, '授粉',   'POLLINATE', 'wm_farm_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1006, 7, '整枝压蔓', 'PRUNE',   'wm_farm_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1007, 8, '采收',   'HARVEST',   'wm_farm_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1008, 9, '其他',   'OTHER',     'wm_farm_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(1020, 1, '端正',     '1', 'wm_fruit_shape', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1021, 2, '基本端正', '2', 'wm_fruit_shape', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1022, 3, '畸形',     '3', 'wm_fruit_shape', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');

insert into sys_dict_data values(1030, 1, '种植中', '0', 'wm_batch_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1031, 2, '已采收', '1', 'wm_batch_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1032, 3, '已归档', '2', 'wm_batch_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 8、菜单 (menu_id 2000-2199)
-- ----------------------------
delete from sys_role_menu where menu_id between 2000 and 2199;
delete from sys_menu where menu_id between 2000 and 2199;

insert into sys_menu values(2000, '西瓜溯源', 0, 5, 'trace', null, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '西瓜溯源目录');
insert into sys_menu values(2001, '地块管理', 2000, 1, 'plot',  'trace/plot/index',  '', '', 1, 0, 'C', '0', '0', 'trace:plot:list',  'build',     'admin', sysdate(), '', null, '');
insert into sys_menu values(2002, '种植批次', 2000, 2, 'batch', 'trace/batch/index', '', '', 1, 0, 'C', '0', '0', 'trace:batch:list', 'list',      'admin', sysdate(), '', null, '');
insert into sys_menu values(2003, '农事记录', 2000, 3, 'farm',  'trace/farm/index',  '', '', 1, 0, 'C', '0', '0', 'trace:farm:list',  'edit',      'admin', sysdate(), '', null, '');
insert into sys_menu values(2004, '品控记录', 2000, 4, 'qc',    'trace/qc/index',    '', '', 1, 0, 'C', '0', '0', 'trace:qc:list',    'checkbox',  'admin', sysdate(), '', null, '');
insert into sys_menu values(2005, '分级标准', 2000, 5, 'grade', 'trace/grade/index', '', '', 1, 0, 'C', '0', '0', 'trace:grade:list', 'dict',      'admin', sysdate(), '', null, '');
insert into sys_menu values(2006, '品控统计', 2000, 6, 'stats', 'trace/stats/index', '', '', 1, 0, 'C', '0', '0', 'trace:stats:view', 'chart',     'admin', sysdate(), '', null, '');
insert into sys_menu values(2007, '导出归档', 2000, 7, 'archive', 'trace/archive/index', '', '', 1, 0, 'C', '0', '0', 'trace:archive:export', 'download', 'admin', sysdate(), '', null, '');

-- 按钮
insert into sys_menu values(2100, '地块查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:plot:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2101, '地块新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:plot:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2102, '地块修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:plot:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2103, '地块删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:plot:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2104, '地块导出', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:plot:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2110, '批次查询', 2002, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2111, '批次新增', 2002, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2112, '批次修改', 2002, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2113, '批次删除', 2002, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2114, '批次导出', 2002, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2115, '生成二维码', 2002, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:qrcode', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2116, '批次归档', 2002, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:batch:archive', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2120, '农事查询', 2003, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:farm:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2121, '农事新增', 2003, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:farm:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2122, '农事修改', 2003, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:farm:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2123, '农事删除', 2003, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:farm:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2124, '农事导出', 2003, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:farm:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2130, '品控查询', 2004, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:qc:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2131, '品控新增', 2004, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:qc:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2132, '品控修改', 2004, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:qc:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2133, '品控删除', 2004, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:qc:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2134, '品控导出', 2004, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:qc:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2140, '标准查询', 2005, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:grade:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2141, '标准新增', 2005, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:grade:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2142, '标准修改', 2005, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:grade:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2143, '标准删除', 2005, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:grade:remove', '#', 'admin', sysdate(), '', null, '');

-- 隐藏权限：离线同步 / 查看全部农户数据
insert into sys_menu values(2150, '离线同步', 2000, 90, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:sync:submit', '#', 'admin', sysdate(), '', null, '小程序离线队列提交');
insert into sys_menu values(2151, '全部数据', 2000, 91, '#', '', '', '', 1, 0, 'F', '0', '0', 'trace:data:all',    '#', 'admin', sysdate(), '', null, '可查看所有农户的批次数据');

-- ----------------------------
-- 9、角色: 农户 farmer (role_id 100) + 示例账号 farmer / admin123
-- ----------------------------
delete from sys_user_role where role_id = 100 or user_id = 100;
delete from sys_role where role_id = 100;
delete from sys_user where user_id = 100;
insert into sys_role values(100, '农户', 'farmer', 3, 5, 1, 1, '0', '0', 'admin', sysdate(), '', null, '西瓜种植农户：录入农事/品控、生成二维码');
insert into sys_user values(100, 103, 'farmer', '张农户', '00', '', '', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', null, sysdate(), 'admin', sysdate(), '', null, '示例农户（默认密码同 admin）');
insert into sys_user_role values (100, 100);

insert into sys_role_menu (role_id, menu_id) values
 (100, 2000),
 (100, 2001), (100, 2100), (100, 2101), (100, 2102),
 (100, 2002), (100, 2110), (100, 2111), (100, 2112), (100, 2115),
 (100, 2003), (100, 2120), (100, 2121), (100, 2122), (100, 2123),
 (100, 2004), (100, 2130), (100, 2131), (100, 2132), (100, 2133),
 (100, 2005), (100, 2140),
 (100, 2150);
