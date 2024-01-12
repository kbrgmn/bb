drop table if exists events;
create table if not exists events
(
    organization UUID,
    grouping     UUID,
    id           UUID,
    name         String,
    timestamp    DateTime64(3, 'UTC'),
    billable     Boolean,
    reference    String,
    properties   String
)
    engine = MergeTree PRIMARY KEY (organization, grouping, id)
        ORDER BY (organization, grouping, id)
;

drop table if exists resources;
create table if not exists resources
(
    organization UUID,
    grouping     UUID,
    id           UUID,
    name         String,
    startDate    DateTime64(3, 'UTC'),
    endDate Nullable(DateTime64(3, 'UTC')),
    billable     Boolean,
    reference    String,
    properties   String
)
    engine = MergeTree PRIMARY KEY (organization, grouping, id)
        ORDER BY (organization, grouping, id)
;

-- https://clickhouse.com/docs/en/integrations/postgresql#using-the-postgresql-table-engine
