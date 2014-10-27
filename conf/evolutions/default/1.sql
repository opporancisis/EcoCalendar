# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table city (
  id                        bigint not null,
  name                      varchar(255),
  country_id                bigint,
  center_latitude           double,
  center_longitude          double,
  default_zoom              integer,
  weight                    integer,
  constraint pk_city primary key (id))
;

create table country (
  id                        bigint not null,
  name                      varchar(255),
  center_latitude           double,
  center_longitude          double,
  default_zoom              integer,
  constraint pk_country primary key (id))
;

create table event (
  id                        bigint not null,
  author_id                 bigint,
  use_author_name_and_phone boolean,
  contact_name              varchar(255),
  contact_phone             varchar(255),
  published                 boolean,
  parent_id                 bigint,
  coords_id                 bigint,
  name                      varchar(255),
  description               clob,
  additional_info_link      varchar(255),
  post_release_link         varchar(255),
  created                   timestamp not null,
  updated                   timestamp not null,
  constraint pk_event primary key (id))
;

create table event_day_program (
  id                        bigint not null,
  date                      date,
  event_id                  bigint,
  constraint pk_event_day_program primary key (id))
;

create table event_progam_item (
  id                        bigint not null,
  start                     time,
  end                       time,
  description               varchar(255),
  program_id                bigint,
  constraint pk_event_progam_item primary key (id))
;

create table event_tag (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_event_tag primary key (id))
;

create table geo_coords (
  id                        bigint not null,
  latitude                  double,
  longitude                 double,
  city_id                   bigint,
  address                   varchar(255),
  constraint pk_geo_coords primary key (id))
;

create table grand_event (
  id                        bigint not null,
  author_id                 bigint,
  published                 boolean,
  pre_moderation            boolean,
  name                      varchar(255),
  description               clob,
  start_date                date,
  end_date                  date,
  created                   timestamp not null,
  updated                   timestamp not null,
  constraint pk_grand_event primary key (id))
;

create table home_page (
  id                        bigint not null,
  title                     varchar(255),
  body                      TEXT,
  constraint pk_home_page primary key (id))
;

create table karma_change (
  id                        bigint not null,
  user_id                   bigint not null,
  change_type               integer,
  custom_reason             varchar(255),
  value                     bigint,
  created                   timestamp not null,
  constraint ck_karma_change_change_type check (change_type in (0,1,2,3,4,5)),
  constraint pk_karma_change primary key (id))
;

create table linked_account (
  id                        bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_linked_account primary key (id))
;

create table message (
  id                        bigint not null,
  time                      timestamp,
  owner_id                  bigint,
  subject                   varchar(255),
  body                      clob,
  severity                  integer,
  unread                    boolean,
  constraint ck_message_severity check (severity in (0,1)),
  constraint pk_message primary key (id))
;

create table organization (
  id                        bigint not null,
  name                      varchar(255),
  description               clob,
  constraint pk_organization primary key (id))
;

create table security_role (
  id                        bigint not null,
  role_name                 varchar(255),
  constraint pk_security_role primary key (id))
;

create table setting (
  id                        bigint not null,
  clazz                     varchar(255),
  name                      varchar(255),
  value                     TEXT,
  editable                  boolean,
  constraint pk_setting primary key (id))
;

create table standard_page (
  id                        bigint not null,
  order_ind                 bigint,
  disabled                  boolean,
  title                     varchar(255),
  link                      varchar(255),
  description               TEXT,
  constraint pk_standard_page primary key (id))
;

create table token_action (
  id                        bigint not null,
  token                     varchar(255),
  target_user_id            bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id))
;

create table uploaded_file (
  id                        bigint not null,
  name                      varchar(255),
  created                   timestamp,
  modified                  timestamp,
  mime                      varchar(255),
  constraint pk_uploaded_file primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  email_public              boolean,
  nick                      varchar(255),
  name                      varchar(255),
  phone                     varchar(255),
  karma                     bigint,
  city_id                   bigint,
  country_for_unknown_city_id bigint,
  unknown_city              varchar(255),
  note                      varchar(255),
  last_login                timestamp,
  blocked                   boolean,
  email_validated           boolean,
  constraint uq_user_1 unique (NICK),
  constraint pk_user primary key (id))
;

create table user_permission (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_user_permission primary key (id))
;


create table event_tag_event (
  event_tag_id                   bigint not null,
  event_id                       bigint not null,
  constraint pk_event_tag_event primary key (event_tag_id, event_id))
;

create table event_tag_grand_event (
  event_tag_id                   bigint not null,
  grand_event_id                 bigint not null,
  constraint pk_event_tag_grand_event primary key (event_tag_id, grand_event_id))
;

create table home_page_uploaded_file (
  home_page_id                   bigint not null,
  uploaded_file_id               bigint not null,
  constraint pk_home_page_uploaded_file primary key (home_page_id, uploaded_file_id))
;

create table organization_event (
  organization_id                bigint not null,
  event_id                       bigint not null,
  constraint pk_organization_event primary key (organization_id, event_id))
;

create table organization_grand_event (
  organization_id                bigint not null,
  grand_event_id                 bigint not null,
  constraint pk_organization_grand_event primary key (organization_id, grand_event_id))
;

create table standard_page_uploaded_file (
  standard_page_id               bigint not null,
  uploaded_file_id               bigint not null,
  constraint pk_standard_page_uploaded_file primary key (standard_page_id, uploaded_file_id))
;

create table user_security_role (
  user_id                        bigint not null,
  security_role_id               bigint not null,
  constraint pk_user_security_role primary key (user_id, security_role_id))
;

create table user_user_permission (
  user_id                        bigint not null,
  user_permission_id             bigint not null,
  constraint pk_user_user_permission primary key (user_id, user_permission_id))
;
create sequence city_seq;

create sequence country_seq;

create sequence event_seq;

create sequence event_day_program_seq;

create sequence event_progam_item_seq;

create sequence event_tag_seq;

create sequence geo_coords_seq;

create sequence grand_event_seq;

create sequence home_page_seq;

create sequence karma_change_seq;

create sequence linked_account_seq;

create sequence message_seq;

create sequence organization_seq;

create sequence security_role_seq;

create sequence setting_seq;

create sequence standard_page_seq;

create sequence token_action_seq;

create sequence uploaded_file_seq;

create sequence user_seq;

create sequence user_permission_seq;

alter table city add constraint fk_city_country_1 foreign key (country_id) references country (id) on delete restrict on update restrict;
create index ix_city_country_1 on city (country_id);
alter table event add constraint fk_event_author_2 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_event_author_2 on event (author_id);
alter table event add constraint fk_event_parent_3 foreign key (parent_id) references grand_event (id) on delete restrict on update restrict;
create index ix_event_parent_3 on event (parent_id);
alter table event add constraint fk_event_coords_4 foreign key (coords_id) references geo_coords (id) on delete restrict on update restrict;
create index ix_event_coords_4 on event (coords_id);
alter table event_day_program add constraint fk_event_day_program_event_5 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_event_day_program_event_5 on event_day_program (event_id);
alter table event_progam_item add constraint fk_event_progam_item_program_6 foreign key (program_id) references event_day_program (id) on delete restrict on update restrict;
create index ix_event_progam_item_program_6 on event_progam_item (program_id);
alter table geo_coords add constraint fk_geo_coords_city_7 foreign key (city_id) references city (id) on delete restrict on update restrict;
create index ix_geo_coords_city_7 on geo_coords (city_id);
alter table grand_event add constraint fk_grand_event_author_8 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_grand_event_author_8 on grand_event (author_id);
alter table karma_change add constraint fk_karma_change_user_9 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_karma_change_user_9 on karma_change (user_id);
alter table linked_account add constraint fk_linked_account_user_10 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_linked_account_user_10 on linked_account (user_id);
alter table message add constraint fk_message_owner_11 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_message_owner_11 on message (owner_id);
alter table token_action add constraint fk_token_action_targetUser_12 foreign key (target_user_id) references user (id) on delete restrict on update restrict;
create index ix_token_action_targetUser_12 on token_action (target_user_id);
alter table user add constraint fk_user_city_13 foreign key (city_id) references city (id) on delete restrict on update restrict;
create index ix_user_city_13 on user (city_id);
alter table user add constraint fk_user_countryForUnknownCity_14 foreign key (country_for_unknown_city_id) references country (id) on delete restrict on update restrict;
create index ix_user_countryForUnknownCity_14 on user (country_for_unknown_city_id);



alter table event_tag_event add constraint fk_event_tag_event_event_tag_01 foreign key (event_tag_id) references event_tag (id) on delete restrict on update restrict;

alter table event_tag_event add constraint fk_event_tag_event_event_02 foreign key (event_id) references event (id) on delete restrict on update restrict;

alter table event_tag_grand_event add constraint fk_event_tag_grand_event_even_01 foreign key (event_tag_id) references event_tag (id) on delete restrict on update restrict;

alter table event_tag_grand_event add constraint fk_event_tag_grand_event_gran_02 foreign key (grand_event_id) references grand_event (id) on delete restrict on update restrict;

alter table home_page_uploaded_file add constraint fk_home_page_uploaded_file_ho_01 foreign key (home_page_id) references home_page (id) on delete restrict on update restrict;

alter table home_page_uploaded_file add constraint fk_home_page_uploaded_file_up_02 foreign key (uploaded_file_id) references uploaded_file (id) on delete restrict on update restrict;

alter table organization_event add constraint fk_organization_event_organiz_01 foreign key (organization_id) references organization (id) on delete restrict on update restrict;

alter table organization_event add constraint fk_organization_event_event_02 foreign key (event_id) references event (id) on delete restrict on update restrict;

alter table organization_grand_event add constraint fk_organization_grand_event_o_01 foreign key (organization_id) references organization (id) on delete restrict on update restrict;

alter table organization_grand_event add constraint fk_organization_grand_event_g_02 foreign key (grand_event_id) references grand_event (id) on delete restrict on update restrict;

alter table standard_page_uploaded_file add constraint fk_standard_page_uploaded_fil_01 foreign key (standard_page_id) references standard_page (id) on delete restrict on update restrict;

alter table standard_page_uploaded_file add constraint fk_standard_page_uploaded_fil_02 foreign key (uploaded_file_id) references uploaded_file (id) on delete restrict on update restrict;

alter table user_security_role add constraint fk_user_security_role_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_security_role add constraint fk_user_security_role_securit_02 foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

alter table user_user_permission add constraint fk_user_user_permission_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_user_permission add constraint fk_user_user_permission_user__02 foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists city;

drop table if exists country;

drop table if exists event;

drop table if exists event_tag_event;

drop table if exists organization_event;

drop table if exists event_day_program;

drop table if exists event_progam_item;

drop table if exists event_tag;

drop table if exists event_tag_grand_event;

drop table if exists geo_coords;

drop table if exists grand_event;

drop table if exists organization_grand_event;

drop table if exists home_page;

drop table if exists home_page_uploaded_file;

drop table if exists karma_change;

drop table if exists linked_account;

drop table if exists message;

drop table if exists organization;

drop table if exists security_role;

drop table if exists setting;

drop table if exists standard_page;

drop table if exists standard_page_uploaded_file;

drop table if exists token_action;

drop table if exists uploaded_file;

drop table if exists user;

drop table if exists user_security_role;

drop table if exists user_user_permission;

drop table if exists user_permission;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists city_seq;

drop sequence if exists country_seq;

drop sequence if exists event_seq;

drop sequence if exists event_day_program_seq;

drop sequence if exists event_progam_item_seq;

drop sequence if exists event_tag_seq;

drop sequence if exists geo_coords_seq;

drop sequence if exists grand_event_seq;

drop sequence if exists home_page_seq;

drop sequence if exists karma_change_seq;

drop sequence if exists linked_account_seq;

drop sequence if exists message_seq;

drop sequence if exists organization_seq;

drop sequence if exists security_role_seq;

drop sequence if exists setting_seq;

drop sequence if exists standard_page_seq;

drop sequence if exists token_action_seq;

drop sequence if exists uploaded_file_seq;

drop sequence if exists user_seq;

drop sequence if exists user_permission_seq;

