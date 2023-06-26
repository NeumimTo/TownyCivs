create table %prefix%townycivs_version (
    version int not null
);
--split
create table %prefix%townycivs_structure (
    structure_uuid varchar(36) not null,
    town_uuid varchar(36) not null,
    last_tick_time bigint not null,
    structure_id varchar(64) not null,
    center varchar(32) not null,
    containers text,
    edit_mode bool default false,
    storage text,
    primary key(structure_uuid)
);
--split
insert into %prefix%townycivs_version values(1);