create table messages
(
    id        text         default nextval('messages_id_seq'::regclass) not null
        primary key,
    payload   text                                                      not null,
    status    varchar(255) default false,
    timestamp timestamp(6)
);
