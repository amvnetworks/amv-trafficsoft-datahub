create table if not exists `amv_trafficsoft_xfcd_delivery` (
    `ID` bigint not null,
    `CONFIRMED` datetime,
    `TS` datetime not null,
    primary key (`ID`)
);

create table if not exists `amv_trafficsoft_xfcd_node` (
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
    `V_ID` bigint not null,
    `VDOP` decimal(10,1),
    `BPC_ID` integer not null,
    `IMXFCD_D_ID` bigint,
    primary key (`ID`)
);

create table if not exists `amv_trafficsoft_xfcd_state` (
    `IMXFCD_N_ID` bigint not null,
    `CD` varchar(10) not null,
    `VAL` longtext,
    primary key (`IMXFCD_N_ID`, `CD`)
);

create table `amv_trafficsoft_xfcd_xfcd` (
    `IMXFCD_N_ID` bigint not null,
    `TYPE` varchar(10) not null,
    `VAL` decimal(14,6),
    `VALSTR` varchar(254),
    primary key (`IMXFCD_N_ID`, `TYPE`)
);
