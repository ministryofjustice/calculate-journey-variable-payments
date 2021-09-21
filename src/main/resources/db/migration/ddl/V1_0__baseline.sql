create table audit_events
(
    audit_event_id uuid         not null,
    created_at     timestamp    not null,
    event_type     varchar(255) not null,
    metadata       varchar(1024),
    username       varchar(255) not null,
    constraint AUDIT_EVENTS_PK primary key (audit_event_id)
);

create table events
(
    event_id       varchar(255) not null,
    eventable_id   varchar(255),
    eventable_type varchar(255),
    notes          varchar(1024),
    occurred_at    timestamp,
    recorded_at    timestamp,
    supplier       varchar(255),
    event_type     varchar(255),
    updated_at     timestamp,
    constraint EVENTS_PK primary key (event_id)
);

create table journeys
(
    journey_id           varchar(255) not null,
    billable             boolean      not null,
    client_timestamp     timestamp,
    drop_off             timestamp,
    effective_year       int4         not null,
    from_nomis_agency_id varchar(255) not null,
    move_id              varchar(255) not null,
    notes                varchar(1024),
    pick_up              timestamp,
    state                varchar(255) not null,
    supplier             varchar(255) not null,
    to_nomis_agency_id   varchar(255),
    updated_at           timestamp,
    vehicle_registration varchar(255),
    constraint JOURNEYS_PK primary key (journey_id)
);

create table locations
(
    location_id     uuid         not null,
    added_at        timestamp    not null,
    location_type   varchar(255) not null,
    nomis_agency_id varchar(255) not null,
    site_name       varchar(255) not null,
    updated_at      timestamp    not null,
    constraint LOCATIONS_PK primary key (location_id),
    constraint LOCATIONS_NAI_UNIQUE unique (nomis_agency_id),
    constraint LOCATIONS_SN_UNIQUE unique (site_name)
);

create table moves
(
    move_id                     varchar(255)  not null,
    cancellation_reason         varchar(255),
    cancellation_reason_comment varchar(1024),
    drop_off_or_cancelled       timestamp,
    from_nomis_agency_id        varchar(255)  not null,
    move_date                   date,
    move_type                   varchar(255),
    notes                       varchar(1024) not null,
    pick_up                     timestamp,
    profile_id                  varchar(255),
    reference                   varchar(255)  not null,
    report_from_location_type   varchar(255)  not null,
    report_to_location_type     varchar(255),
    status                      varchar(255)  not null,
    supplier                    varchar(255)  not null,
    to_nomis_agency_id          varchar(255),
    updated_at                  timestamp     not null,
    vehicle_registration        varchar(255),
    constraint MOVES_PK primary key (move_id)
);

create table people
(
    person_id               varchar(255) not null,
    date_of_birth           date,
    ethnicity               varchar(255),
    first_names             varchar(255),
    gender                  varchar(255),
    last_name               varchar(255),
    latest_nomis_booking_id int4,
    prison_number           varchar(255),
    updated_at              timestamp    not null,
    constraint PEOPLE_PK primary key (person_id)
);

create table prices
(
    price_id         uuid not null,
    added_at         timestamp,
    effective_year   int4 not null,
    price_in_pence   int4 not null,
    supplier         varchar(255),
    from_location_id uuid,
    to_location_id   uuid,
    constraint PRICES_PK primary key (price_id),
    constraint SUPPLIER_FROM_TO_YEAR_UNIQUE unique (supplier, from_location_id, to_location_id, effective_year),
    constraint FROM_LOCATION_FK foreign key (from_location_id) references locations,
    constraint TO_LOCATION_FK foreign key (to_location_id) references locations
);

create table profiles
(
    profile_id varchar(255) not null,
    person_id  varchar(255) not null,
    updated_at timestamp    not null,
    constraint PROFILES_PK primary key (profile_id)
);

create table shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL,
    constraint SHEDLOCK_PK PRIMARY KEY (name)
);

create index EVENTS_EVENTABLE_ID_IDX on events (eventable_id);

create index JOURNEYS_MOVE_ID_IDX on journeys (move_id);

CREATE TABLE SPRING_SESSION
(
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES
(
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BYTEA NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);