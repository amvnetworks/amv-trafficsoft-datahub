create table if not exists amv_trafficsoft_xfcd_delivery (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    ID bigint not null,
    CONFIRMED timestamp null,
    TS timestamp not null,
    primary key (ID)
);

create table if not exists amv_trafficsoft_xfcd_node (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    ID bigint not null,
    BPC_ID bigint not null,
    TRIPID bigint not null,
    V_ID bigint null,
    TS bigint not null,
    SATCNT integer null,
    LATDEG decimal(10,6) null,
    LONDEG decimal(10,6) null,
    HDOP decimal(10,1) null,
    VDOP decimal(10,1) null,
    SPEED decimal(10,2) null,
    ALTITUDE decimal(10,2) null,
    HEADING decimal(10,6) null,
    IMXFCD_D_ID bigint null,
    primary key (ID),
    foreign key (IMXFCD_D_ID)
      references amv_trafficsoft_xfcd_delivery(ID)
      on update cascade on delete cascade
);

CREATE INDEX IDX_ATXN1_DELIVERY_ID ON amv_trafficsoft_xfcd_node (IMXFCD_D_ID);


create table if not exists amv_trafficsoft_xfcd_state (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    IMXFCD_N_ID bigint not null,
    CD varchar(10) not null,
    VAL varchar(1023) null,
    foreign key (IMXFCD_N_ID)
      references amv_trafficsoft_xfcd_node(ID)
      on update cascade on delete cascade
);

CREATE INDEX IDX_ATXS1_NODE_ID ON amv_trafficsoft_xfcd_state (IMXFCD_N_ID);


create table if not exists amv_trafficsoft_xfcd_xfcd (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    IMXFCD_N_ID bigint not null,
    TYPE varchar(10) not null,
    VAL decimal(14,6) null,
    VALSTR varchar(255) null,
    foreign key (IMXFCD_N_ID)
      references amv_trafficsoft_xfcd_node(ID)
      on update cascade on delete cascade
);

CREATE INDEX IDX_ATXX1_NODE_ID ON amv_trafficsoft_xfcd_xfcd (IMXFCD_N_ID);


create table if not exists amv_trafficsoft_xfcd_latest_node (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    ID bigint not null,
    BPC_ID bigint not null,
    TRIPID bigint not null,
    V_ID bigint not null,
    TS bigint not null,
    SATCNT integer null,
    LATDEG decimal(10,6) null,
    LONDEG decimal(10,6) null,
    HDOP decimal(10,1) null,
    VDOP decimal(10,1) null,
    SPEED decimal(10,2) null,
    ALTITUDE decimal(10,2) null,
    HEADING decimal(10,6) null,
    primary key (V_ID, BPC_ID)
);

create table if not exists amv_trafficsoft_xfcd_latest_state (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    TS bigint not null,
    V_ID bigint not null,
    BPC_ID bigint not null,
    CD varchar(10) not null,
    VAL varchar(1023) null,
    LATDEG decimal(10,6) null,
    LONDEG decimal(10,6) null,
    primary key (V_ID, BPC_ID, CD)
);

create table if not exists amv_trafficsoft_xfcd_latest_xfcd (
    CREATED_AT timestamp not null,
    UPDATED_AT timestamp null,
    TS bigint not null,
    V_ID bigint not null,
    BPC_ID bigint not null,
    TYPE varchar(10) not null,
    VAL decimal(14,6) null,
    VALSTR varchar(255) null,
    LATDEG decimal(10,6) null,
    LONDEG decimal(10,6) null,
    primary key (V_ID, BPC_ID, TYPE)
);
