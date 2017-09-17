
create or replace view `amv_trafficsoft_xfcd_node_with_states` as
    select n.*, s.`CD` as `STATE_CD`, s.`VAL` as `STATE_VAL`
    from `amv_trafficsoft_xfcd_node` n inner join `amv_trafficsoft_xfcd_state` s
    on n.`ID` = s.`IMXFCD_N_ID`
    order by n.`ID` asc, n.`TS` asc;

create or replace view `amv_trafficsoft_xfcd_node_with_xfcds` as
    select n.*, x.`TYPE` as `XFCD_TYPE`, x.`VAL` as `XFCD_VAL`, x.`VALSTR` as `XFCD_VALSTR`
    from `amv_trafficsoft_xfcd_node` n inner join `amv_trafficsoft_xfcd_xfcd` x
    on n.`ID` = x.`IMXFCD_N_ID`
    order by n.`ID` asc, n.`TS` asc;