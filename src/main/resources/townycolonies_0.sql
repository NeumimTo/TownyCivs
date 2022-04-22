create table %prefix%townycolonies_version (
    version int not null
);

create table %prefix%townycolonies_structure (
    structure_uuid varchar(16) not null,
    town_uuid varchar(16) not null,
    last_tick_time bigint not null,
    structure_id varchar(64) not null,
    center varchar(32) not null,
    containers text,
    edit_mode bool default false,
    storage text,
    primary key(structure_uuid)
);

insert into %prefix%townycolonies_version values(1);