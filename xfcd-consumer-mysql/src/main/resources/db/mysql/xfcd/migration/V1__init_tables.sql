create table if not exists `amv_trafficsoft_xfcd_delivery` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `ID` bigint not null,
    `CONFIRMED` datetime null,
    `TS` datetime not null,
    primary key (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_node` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `ID` bigint not null,
    `BPC_ID` bigint not null,
    `TRIPID` bigint not null,
    `V_ID` bigint null,
    `TS` bigint not null,
    `SATCNT` integer null,
    `LATDEG` decimal(10,6) null,
    `LONDEG` decimal(10,6) null,
    `HDOP` decimal(10,1) null,
    `VDOP` decimal(10,1) null,
    `SPEED` decimal(10,2) null,
    `ALTITUDE` decimal(10,2) null,
    `HEADING` decimal(10,6) null,
    `IMXFCD_D_ID` bigint null,
    primary key (`ID`),
    foreign key (`IMXFCD_D_ID`)
      references amv_trafficsoft_xfcd_delivery(`ID`)
      on update cascade on delete cascade
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_state` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `IMXFCD_N_ID` bigint not null,
    `CD` varchar(10) not null,
    `VAL` varchar(1023) null,
    foreign key (`IMXFCD_N_ID`)
      references amv_trafficsoft_xfcd_node(`ID`)
      on update cascade on delete cascade
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_xfcd` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `IMXFCD_N_ID` bigint not null,
    `TYPE` varchar(10) not null,
    `VAL` decimal(14,6) null,
    `VALSTR` varchar(255) null,
    foreign key (`IMXFCD_N_ID`)
      references amv_trafficsoft_xfcd_node(`ID`)
      on update cascade on delete cascade
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_latest_node` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `ID` bigint not null,
    `BPC_ID` bigint not null,
    `TRIPID` bigint not null,
    `V_ID` bigint not null,
    `TS` bigint not null,
    `SATCNT` integer null,
    `LATDEG` decimal(10,6) null,
    `LONDEG` decimal(10,6) null,
    `HDOP` decimal(10,1) null,
    `VDOP` decimal(10,1) null,
    `SPEED` decimal(10,2) null,
    `ALTITUDE` decimal(10,2) null,
    `HEADING` decimal(10,6) null,
    primary key (`V_ID`, `BPC_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_latest_state` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `V_ID` bigint not null,
    `BPC_ID` bigint not null,
    `CD` varchar(10) not null,
    `VAL` longtext null,
    primary key (`V_ID`, `BPC_ID`, `CD`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists `amv_trafficsoft_xfcd_latest_xfcd` (
    `CREATED_AT` datetime not null,
    `UPDATED_AT` datetime null,
    `V_ID` bigint not null,
    `BPC_ID` bigint not null,
    `TYPE` varchar(10) not null,
    `VAL` decimal(14,6) null,
    `VALSTR` varchar(255) null,
    primary key (`V_ID`, `BPC_ID`, `TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
