drop table if exists events sync;
create or replace table events
(
    organization UUID,
    group        UUID,
    id           UUID,
    type         String,
    timestamp    DateTime64(3, 'UTC'),
    billable     Boolean,
    reference    String,
    properties   String
)
    engine = ReplacingMergeTree
        PRIMARY KEY (organization, group, id)
        ORDER BY (organization, group, id)
;

drop table if exists resources sync;
create or replace table resources
(
    organization UUID,
    group        UUID,
    id           UUID,
    type         String,
    startDate    DateTime64(3, 'UTC'),
    endDate Nullable(DateTime64(3, 'UTC')),
    billable     Boolean,
    reference    String,
    properties   String
)
    engine = ReplacingMergeTree
        PRIMARY KEY (organization, group, id)
        ORDER BY (organization, group, id)
;

-- https://clickhouse.com/docs/en/integrations/postgresql#using-the-postgresql-table-engine
