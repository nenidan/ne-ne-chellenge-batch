drop table if exists challenge;
drop table if exists history;
drop table if exists point_transaction;
drop table if exists point_wallet;
drop table if exists tmp_finished_challenge;

create table challenge
(
    current_participant_count int                                               not null,
    due_at                    date                                              null,
    max_participants          int                                               not null,
    min_participants          int                                               not null,
    participation_fee         int                                               not null,
    start_at                  date                                              null,
    total_fee                 int                                               not null,
    created_at                datetime(6)                                       not null,
    deleted_at                datetime(6)                                       null,
    host_id                   bigint                                            null,
    id                        bigint auto_increment
        primary key,
    updated_at                datetime(6)                                       null,
    description               varchar(255)                                      null,
    name                      varchar(255)                                      null,
    category                  enum ('HABIT', 'HEALTH', 'RELATIONSHIP', 'STUDY') null,
    status                    enum ('FINISHED', 'ONGOING', 'READY', 'WAITING')  null
);

create table history
(
    date         date         null,
    is_success   bit          not null,
    challenge_id bigint       null,
    created_at   datetime(6)  not null,
    deleted_at   datetime(6)  null,
    id           bigint auto_increment
        primary key,
    updated_at   datetime(6)  null,
    user_id      bigint       null,
    content      varchar(255) null,
    constraint UKmxnhqtn5ktt2j59fbk08seol6
        unique (user_id, challenge_id, date)
);

create table point_wallet
(
    balance    int         not null,
    created_at datetime(6) not null,
    deleted_at datetime(6) null,
    id         bigint auto_increment
        primary key,
    updated_at datetime(6) null,
    user_id    bigint      not null
);

create table point_transaction
(
    amount          int                                                                                                                               not null,
    created_at      datetime(6)                                                                                                                       not null,
    deleted_at      datetime(6)                                                                                                                       null,
    id              bigint auto_increment
        primary key,
    point_wallet_id bigint                                                                                                                            not null,
    updated_at      datetime(6)                                                                                                                       null,
    description     varchar(255)                                                                                                                      null,
    reason          enum ('CHALLENGE_ENTRY', 'CHALLENGE_REFUND', 'CHALLENGE_REWARD', 'CHARGE', 'CHARGE_CANCEL', 'PRODUCT_PURCHASE', 'PRODUCT_REFUND') not null,
    constraint FKo0rx22v0tbn3pi21oj2pv5wv5
        foreign key (point_wallet_id) references point_wallet (id)
);


create table tmp_finished_challenge
(
    id           bigint auto_increment
        primary key,
    challenge_id bigint not null,
    total_fee    int    not null,
    total_days   int    not null
)