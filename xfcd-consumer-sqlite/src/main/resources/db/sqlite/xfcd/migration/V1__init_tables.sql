create table if not exists `amv_trafficsoft_xfcd_delivery` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `ID` bigint not null,
    `CONFIRMED` datetime,
    `TS` datetime not null,
    primary key (`ID`)
);

create table if not exists `amv_trafficsoft_xfcd_node` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `ID` bigint not null,
    `ALTITUDE` decimal(10,2),
    `HEADING` decimal(10,6),
    `HDOP` decimal(10,1),
    `LATDEG` decimal(10,6) not null,
    `LONDEG` decimal(10,6) not null,
    `TS` bigint not null,
    `SATCNT` integer,
    `SPEED` decimal(10,2),
    `TRIPID` bigint not null,
    `V_ID` bigint,
    `VDOP` decimal(10,1),
    `BPC_ID` integer not null,
    `IMXFCD_D_ID` bigint,
    primary key (`ID`),
    foreign key (`IMXFCD_D_ID`)
      references amv_trafficsoft_xfcd_delivery(`ID`)
      on update cascade on delete cascade
);

create table if not exists `amv_trafficsoft_xfcd_state` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `IMXFCD_N_ID` bigint not null,
    `CD` varchar(10) not null,
    `VAL` longtext,
    foreign key (`IMXFCD_N_ID`)
      references amv_trafficsoft_xfcd_node(`ID`)
      on update cascade on delete cascade
);

create table `amv_trafficsoft_xfcd_xfcd` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `IMXFCD_N_ID` bigint not null,
    `TYPE` varchar(10) not null,
    `VAL` decimal(14,6),
    `VALSTR` varchar(254),
    foreign key (`IMXFCD_N_ID`)
      references amv_trafficsoft_xfcd_node(`ID`)
      on update cascade on delete cascade
);

create table if not exists `amv_trafficsoft_xfcd_latest_fcd` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `V_ID` bigint,
    `BPC_ID` integer not null,
    `TS` bigint not null,
    `ALTITUDE` decimal(10,2),
    `HEADING` decimal(10,6),
    `HDOP` decimal(10,1),
    `LATDEG` decimal(10,6) not null,
    `LONDEG` decimal(10,6) not null,
    `SATCNT` integer,
    `SPEED` decimal(10,2),
    `TRIPID` bigint not null,
    `VDOP` decimal(10,1),
    primary key (`V_ID`, `BPC_ID`)
);

create table if not exists `amv_trafficsoft_xfcd_latest_state` (
    `CREATED_AT` datetime,
    `UPDATED_AT` datetime,
    `V_ID` bigint,
    `BPC_ID` integer not null,
    `CD` varchar(10) not null,
    `VAL` longtext,
    primary key (`V_ID`, `BPC_ID`, `CD`)
);
