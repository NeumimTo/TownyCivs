create table %prefix%townycolonies_version (
    int version not null
);

create table %prefix%townycolonies_structure (
    structure_uuid varchar primary key,
    town_uuid varchar not null,
    last_tick_time bigint not null,
    structure_id varchar not null,
    storage text
)

insert into %prefix%townycolonies_version values(1);