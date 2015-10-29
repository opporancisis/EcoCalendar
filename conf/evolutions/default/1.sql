# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table blog_post (
  id                            bigint not null,
  title                         varchar(255),
  content                       TEXT,
  owner_id                      bigint,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_blog_post primary key (id)
);
create sequence blog_post_seq;

create table blog_post_uploaded_file (
  blog_post_id                  bigint not null,
  uploaded_file_id              bigint not null,
  constraint pk_blog_post_uploaded_file primary key (blog_post_id,uploaded_file_id)
);

create table city (
  id                            bigint not null,
  disabled                      boolean,
  name                          varchar(255),
  code                          varchar(255),
  country_id                    bigint,
  zone                          varchar(60),
  center_latitude               double,
  center_longitude              double,
  default_zoom                  integer,
  weight                        integer,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_city primary key (id)
);
create sequence city_seq;

create table country (
  id                            bigint not null,
  disabled                      boolean,
  name                          varchar(255),
  code                          varchar(255),
  center_latitude               double,
  center_longitude              double,
  default_zoom                  integer,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_country primary key (id)
);
create sequence country_seq;

create table event (
  id                            bigint not null,
  published                     boolean,
  author_id                     bigint,
  name                          varchar(255),
  description                   clob,
  additional_info_link          varchar(255),
  parent_id                     bigint,
  use_contact_info              boolean,
  use_author_contact_info       boolean,
  contact_name                  varchar(255),
  contact_phone                 varchar(255),
  contact_profile               varchar(255),
  city_id                       bigint,
  address                       varchar(255),
  extended_geo_settings         boolean,
  start                         timestamp,
  finish                        timestamp,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_event primary key (id)
);
create sequence event_seq;

create table event_tag (
  id                            bigint not null,
  name                          varchar(255),
  constraint pk_event_tag primary key (id)
);
create sequence event_tag_seq;

create table event_tag_event (
  event_tag_id                  bigint not null,
  event_id                      bigint not null,
  constraint pk_event_tag_event primary key (event_tag_id,event_id)
);

create table event_tag_grand_event (
  event_tag_id                  bigint not null,
  grand_event_id                bigint not null,
  constraint pk_event_tag_grand_event primary key (event_tag_id,grand_event_id)
);

create table geo_coords (
  id                            bigint not null,
  event_id                      bigint not null,
  latitude                      double,
  longitude                     double,
  comment                       varchar(255),
  constraint pk_geo_coords primary key (id)
);
create sequence geo_coords_seq;

create table grand_event (
  id                            bigint not null,
  author_id                     bigint,
  published                     boolean,
  pre_moderation                boolean,
  name                          varchar(255),
  description                   clob,
  start_date                    date,
  end_date                      date,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_grand_event primary key (id)
);
create sequence grand_event_seq;

create table home_page (
  id                            bigint not null,
  title                         varchar(255),
  content                       TEXT,
  latest_news_max               integer,
  constraint pk_home_page primary key (id)
);
create sequence home_page_seq;

create table home_page_uploaded_file (
  home_page_id                  bigint not null,
  uploaded_file_id              bigint not null,
  constraint pk_home_page_uploaded_file primary key (home_page_id,uploaded_file_id)
);

create table karma_change (
  id                            bigint not null,
  change_type                   integer,
  custom_reason                 varchar(255),
  value                         bigint,
  created                       timestamp not null,
  constraint ck_karma_change_change_type check (change_type in (0,1,2,3,4,5)),
  constraint pk_karma_change primary key (id)
);
create sequence karma_change_seq;

create table linked_account (
  id                            bigint not null,
  user_id                       bigint,
  provider_user_id              varchar(255),
  provider_key                  varchar(255),
  constraint pk_linked_account primary key (id)
);
create sequence linked_account_seq;

create table message (
  id                            bigint not null,
  owner_id                      bigint,
  subject                       varchar(255),
  body                          clob,
  severity                      integer,
  unread                        boolean,
  created                       timestamp not null,
  constraint ck_message_severity check (severity in (0,1)),
  constraint pk_message primary key (id)
);
create sequence message_seq;

create table organization (
  id                            bigint not null,
  name                          varchar(255),
  description                   clob,
  constraint pk_organization primary key (id)
);
create sequence organization_seq;

create table organization_event (
  organization_id               bigint not null,
  event_id                      bigint not null,
  constraint pk_organization_event primary key (organization_id,event_id)
);

create table organization_grand_event (
  organization_id               bigint not null,
  grand_event_id                bigint not null,
  constraint pk_organization_grand_event primary key (organization_id,grand_event_id)
);

create table security_role (
  id                            bigint not null,
  role_name                     varchar(255),
  constraint pk_security_role primary key (id)
);
create sequence security_role_seq;

create table setting (
  id                            bigint not null,
  clazz                         varchar(255),
  name                          varchar(255),
  value                         TEXT,
  editable                      boolean,
  constraint pk_setting primary key (id)
);
create sequence setting_seq;

create table standard_page (
  id                            bigint not null,
  order_ind                     bigint,
  disabled                      boolean,
  title                         varchar(255),
  link                          varchar(255),
  content                       TEXT,
  constraint pk_standard_page primary key (id)
);
create sequence standard_page_seq;

create table standard_page_uploaded_file (
  standard_page_id              bigint not null,
  uploaded_file_id              bigint not null,
  constraint pk_standard_page_uploaded_file primary key (standard_page_id,uploaded_file_id)
);

create table token_action (
  id                            bigint not null,
  token                         varchar(255),
  target_user_id                bigint,
  type                          varchar(2),
  created                       timestamp,
  expires                       timestamp,
  constraint ck_token_action_type check (type in ('PR','EV')),
  constraint uq_token_action_token unique (token),
  constraint pk_token_action primary key (id)
);
create sequence token_action_seq;

create table uploaded_file (
  id                            bigint not null,
  name                          varchar(255),
  mime                          varchar(255),
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_uploaded_file primary key (id)
);
create sequence uploaded_file_seq;

create table user (
  id                            bigint not null,
  email                         varchar(255),
  name                          varchar(255),
  phone                         varchar(255),
  profile_link                  varchar(255),
  city_id                       bigint,
  last_login                    timestamp,
  blocked                       boolean,
  email_validated               boolean,
  constraint pk_user primary key (id)
);
create sequence user_seq;

create table user_security_role (
  user_id                       bigint not null,
  security_role_id              bigint not null,
  constraint pk_user_security_role primary key (user_id,security_role_id)
);

create table user_permission (
  id                            bigint not null,
  value                         varchar(255),
  constraint pk_user_permission primary key (id)
);
create sequence user_permission_seq;

alter table blog_post add constraint fk_blog_post_owner_id foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_blog_post_owner_id on blog_post (owner_id);

alter table blog_post_uploaded_file add constraint fk_blog_post_uploaded_file_blog_post foreign key (blog_post_id) references blog_post (id) on delete restrict on update restrict;
create index ix_blog_post_uploaded_file_blog_post on blog_post_uploaded_file (blog_post_id);

alter table blog_post_uploaded_file add constraint fk_blog_post_uploaded_file_uploaded_file foreign key (uploaded_file_id) references uploaded_file (id) on delete restrict on update restrict;
create index ix_blog_post_uploaded_file_uploaded_file on blog_post_uploaded_file (uploaded_file_id);

alter table city add constraint fk_city_country_id foreign key (country_id) references country (id) on delete restrict on update restrict;
create index ix_city_country_id on city (country_id);

alter table event add constraint fk_event_author_id foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_event_author_id on event (author_id);

alter table event add constraint fk_event_parent_id foreign key (parent_id) references grand_event (id) on delete restrict on update restrict;
create index ix_event_parent_id on event (parent_id);

alter table event add constraint fk_event_city_id foreign key (city_id) references city (id) on delete restrict on update restrict;
create index ix_event_city_id on event (city_id);

alter table event_tag_event add constraint fk_event_tag_event_event_tag foreign key (event_tag_id) references event_tag (id) on delete restrict on update restrict;
create index ix_event_tag_event_event_tag on event_tag_event (event_tag_id);

alter table event_tag_event add constraint fk_event_tag_event_event foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_event_tag_event_event on event_tag_event (event_id);

alter table event_tag_grand_event add constraint fk_event_tag_grand_event_event_tag foreign key (event_tag_id) references event_tag (id) on delete restrict on update restrict;
create index ix_event_tag_grand_event_event_tag on event_tag_grand_event (event_tag_id);

alter table event_tag_grand_event add constraint fk_event_tag_grand_event_grand_event foreign key (grand_event_id) references grand_event (id) on delete restrict on update restrict;
create index ix_event_tag_grand_event_grand_event on event_tag_grand_event (grand_event_id);

alter table geo_coords add constraint fk_geo_coords_event_id foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_geo_coords_event_id on geo_coords (event_id);

alter table grand_event add constraint fk_grand_event_author_id foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_grand_event_author_id on grand_event (author_id);

alter table home_page_uploaded_file add constraint fk_home_page_uploaded_file_home_page foreign key (home_page_id) references home_page (id) on delete restrict on update restrict;
create index ix_home_page_uploaded_file_home_page on home_page_uploaded_file (home_page_id);

alter table home_page_uploaded_file add constraint fk_home_page_uploaded_file_uploaded_file foreign key (uploaded_file_id) references uploaded_file (id) on delete restrict on update restrict;
create index ix_home_page_uploaded_file_uploaded_file on home_page_uploaded_file (uploaded_file_id);

alter table linked_account add constraint fk_linked_account_user_id foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_linked_account_user_id on linked_account (user_id);

alter table message add constraint fk_message_owner_id foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_message_owner_id on message (owner_id);

alter table organization_event add constraint fk_organization_event_organization foreign key (organization_id) references organization (id) on delete restrict on update restrict;
create index ix_organization_event_organization on organization_event (organization_id);

alter table organization_event add constraint fk_organization_event_event foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_organization_event_event on organization_event (event_id);

alter table organization_grand_event add constraint fk_organization_grand_event_organization foreign key (organization_id) references organization (id) on delete restrict on update restrict;
create index ix_organization_grand_event_organization on organization_grand_event (organization_id);

alter table organization_grand_event add constraint fk_organization_grand_event_grand_event foreign key (grand_event_id) references grand_event (id) on delete restrict on update restrict;
create index ix_organization_grand_event_grand_event on organization_grand_event (grand_event_id);

alter table standard_page_uploaded_file add constraint fk_standard_page_uploaded_file_standard_page foreign key (standard_page_id) references standard_page (id) on delete restrict on update restrict;
create index ix_standard_page_uploaded_file_standard_page on standard_page_uploaded_file (standard_page_id);

alter table standard_page_uploaded_file add constraint fk_standard_page_uploaded_file_uploaded_file foreign key (uploaded_file_id) references uploaded_file (id) on delete restrict on update restrict;
create index ix_standard_page_uploaded_file_uploaded_file on standard_page_uploaded_file (uploaded_file_id);

alter table token_action add constraint fk_token_action_target_user_id foreign key (target_user_id) references user (id) on delete restrict on update restrict;
create index ix_token_action_target_user_id on token_action (target_user_id);

alter table user add constraint fk_user_city_id foreign key (city_id) references city (id) on delete restrict on update restrict;
create index ix_user_city_id on user (city_id);

alter table user_security_role add constraint fk_user_security_role_user foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_user_security_role_user on user_security_role (user_id);

alter table user_security_role add constraint fk_user_security_role_security_role foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;
create index ix_user_security_role_security_role on user_security_role (security_role_id);


# --- !Downs

alter table blog_post drop constraint if exists fk_blog_post_owner_id;
drop index if exists ix_blog_post_owner_id;

alter table blog_post_uploaded_file drop constraint if exists fk_blog_post_uploaded_file_blog_post;
drop index if exists ix_blog_post_uploaded_file_blog_post;

alter table blog_post_uploaded_file drop constraint if exists fk_blog_post_uploaded_file_uploaded_file;
drop index if exists ix_blog_post_uploaded_file_uploaded_file;

alter table city drop constraint if exists fk_city_country_id;
drop index if exists ix_city_country_id;

alter table event drop constraint if exists fk_event_author_id;
drop index if exists ix_event_author_id;

alter table event drop constraint if exists fk_event_parent_id;
drop index if exists ix_event_parent_id;

alter table event drop constraint if exists fk_event_city_id;
drop index if exists ix_event_city_id;

alter table event_tag_event drop constraint if exists fk_event_tag_event_event_tag;
drop index if exists ix_event_tag_event_event_tag;

alter table event_tag_event drop constraint if exists fk_event_tag_event_event;
drop index if exists ix_event_tag_event_event;

alter table event_tag_grand_event drop constraint if exists fk_event_tag_grand_event_event_tag;
drop index if exists ix_event_tag_grand_event_event_tag;

alter table event_tag_grand_event drop constraint if exists fk_event_tag_grand_event_grand_event;
drop index if exists ix_event_tag_grand_event_grand_event;

alter table geo_coords drop constraint if exists fk_geo_coords_event_id;
drop index if exists ix_geo_coords_event_id;

alter table grand_event drop constraint if exists fk_grand_event_author_id;
drop index if exists ix_grand_event_author_id;

alter table home_page_uploaded_file drop constraint if exists fk_home_page_uploaded_file_home_page;
drop index if exists ix_home_page_uploaded_file_home_page;

alter table home_page_uploaded_file drop constraint if exists fk_home_page_uploaded_file_uploaded_file;
drop index if exists ix_home_page_uploaded_file_uploaded_file;

alter table linked_account drop constraint if exists fk_linked_account_user_id;
drop index if exists ix_linked_account_user_id;

alter table message drop constraint if exists fk_message_owner_id;
drop index if exists ix_message_owner_id;

alter table organization_event drop constraint if exists fk_organization_event_organization;
drop index if exists ix_organization_event_organization;

alter table organization_event drop constraint if exists fk_organization_event_event;
drop index if exists ix_organization_event_event;

alter table organization_grand_event drop constraint if exists fk_organization_grand_event_organization;
drop index if exists ix_organization_grand_event_organization;

alter table organization_grand_event drop constraint if exists fk_organization_grand_event_grand_event;
drop index if exists ix_organization_grand_event_grand_event;

alter table standard_page_uploaded_file drop constraint if exists fk_standard_page_uploaded_file_standard_page;
drop index if exists ix_standard_page_uploaded_file_standard_page;

alter table standard_page_uploaded_file drop constraint if exists fk_standard_page_uploaded_file_uploaded_file;
drop index if exists ix_standard_page_uploaded_file_uploaded_file;

alter table token_action drop constraint if exists fk_token_action_target_user_id;
drop index if exists ix_token_action_target_user_id;

alter table user drop constraint if exists fk_user_city_id;
drop index if exists ix_user_city_id;

alter table user_security_role drop constraint if exists fk_user_security_role_user;
drop index if exists ix_user_security_role_user;

alter table user_security_role drop constraint if exists fk_user_security_role_security_role;
drop index if exists ix_user_security_role_security_role;

drop table if exists blog_post;
drop sequence if exists blog_post_seq;

drop table if exists blog_post_uploaded_file;

drop table if exists city;
drop sequence if exists city_seq;

drop table if exists country;
drop sequence if exists country_seq;

drop table if exists event;
drop sequence if exists event_seq;

drop table if exists event_tag;
drop sequence if exists event_tag_seq;

drop table if exists event_tag_event;

drop table if exists event_tag_grand_event;

drop table if exists geo_coords;
drop sequence if exists geo_coords_seq;

drop table if exists grand_event;
drop sequence if exists grand_event_seq;

drop table if exists home_page;
drop sequence if exists home_page_seq;

drop table if exists home_page_uploaded_file;

drop table if exists karma_change;
drop sequence if exists karma_change_seq;

drop table if exists linked_account;
drop sequence if exists linked_account_seq;

drop table if exists message;
drop sequence if exists message_seq;

drop table if exists organization;
drop sequence if exists organization_seq;

drop table if exists organization_event;

drop table if exists organization_grand_event;

drop table if exists security_role;
drop sequence if exists security_role_seq;

drop table if exists setting;
drop sequence if exists setting_seq;

drop table if exists standard_page;
drop sequence if exists standard_page_seq;

drop table if exists standard_page_uploaded_file;

drop table if exists token_action;
drop sequence if exists token_action_seq;

drop table if exists uploaded_file;
drop sequence if exists uploaded_file_seq;

drop table if exists user;
drop sequence if exists user_seq;

drop table if exists user_security_role;

drop table if exists user_permission;
drop sequence if exists user_permission_seq;

