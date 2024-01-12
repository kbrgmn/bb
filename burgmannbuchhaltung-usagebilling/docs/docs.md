# BurgmannBuchhaltung

## Usage billing module developer documentation

### Introduction

- Usage billing works out dynamically priced billing/invoices depending on how many resources/services/units a customer
  used in a specific time frame.
- The used resources are accumulated over the billing period (e.g., over the course of a month) and invoiced at the end
  of it.
- The base of this module is made up of the Data structure "UsageEvent":
    - **Organization identifier `orgId`: UUID**
        - The organization you have configured with accounting master data and setup for usage billing in
          BurgmannBuchhaltung
        - (Provided to you by BurgmannBuchhaltung when creating an organization with the web application or API)
    - **Project identifier `projectId`: UUID**
        - The project that the usage billing is accounted for.
        - This will in most cases be linked to your customer.
        - (Provided to you by BurgmannBuchhaltung when creating a project with the web application or API)
    - **Event identifier `eventId`: UUID**
        - An *unique* identifier for an billable event
        - Events that have the same organizationId+projectId+eventId (i.e.: are not unique) will be treated as updated
          in reference to the old version of the event, and merged in the next automatic event reorganization cycle
    - **Timestamp `timestamp`: Instant**
        - UTC! see [Timestamps](#timestamps)
    - **Event type/name `eventName`: String**
        - an identifier for the type of event, e.g. `WeatherForecast24hRequest`
        - Pricing is set up in the PricingPlan/TariffPlan based on this name
    - **Is billable? `isBillable`: Boolean**
        - If the event shall be considered when processing billing
        - `True` by default.
        - Can be disabled again before the billing period ends (e.g., the customer used a bunch of resources they did
          not intend to use, and you do not charge them out of courtesy)
    - **Reference `reference`: String**
        - An additional reference you can add to this event, usually used for verification purposes.
        - e.g., the IP address of the service creating the usage with you + the API key (-reference/name) used
        - this will show up in the itemized billing invoice (it is more extensive and thorough than the standard invoice
          sent out to customers)
        - Can be left empty if not needed yet
    - **Additional properties `properties`: String**
        - You can provide additional information for your internal use, e.g. JSON `{"key1": "value1"}`
        - Can be left empty if not needed yet

### Important notes

#### Timestamps

All timestamps are ALWAYS in UTC [!!!]

- You have to provide them in UTC
    - Should you provide them in [your local time zone / your customers time zone / any other incorrect time zone] this
      **will break your billing**
- They will always be reported back to you in UTC
    - Recommendation: You should probably never store or process timestamps in a different zone other than UTC, keep
      them in UTC for as long as possible, and if possible, only convert them in the frontend to the users system time
      zone.

### Usage

Generate your usage billing API token with the BurgmannBuchhaltung Frontend or API, and continue with page:

#### API token

- Your API token is always associated with an organization.
- Additionally, it can also be restricted to a certain project.

#### Account for usage

#### List & filter usage events

The Organization to show is chosen through your API token.

- Optionally: set an `projectId` to filter by
    - If your API token is restricted to a specific project (i.e., not an organization-wide token with no restrictions),
      you will have to provide the projectId the API token is chosen for
- Optionally: set a list of event types to filter for with `filterEvents`, e.g. `["WeatherForecast24hRequest", "WeatherForecast7dRequest", "WeatherForecast30dRequest"]`
- Optionally: set int-pair `limit` to limit and offset the list
  - The first integer is the limit (how many result items you want to receive max)
  - The second integer is the offset index (how many items you want to skip in the list)
- Optionally: set timestamp-pair `duringTime` to filter for events happening in a specific timeframe 
  - The first timestamp (in UTC!) is the (inclusive / `>=`) start timestamp (events have to have happened after this timestamp)
  - The second timestamp (in UTC!) is the (exclusive / `<`) end timestamp (events have to have happened before this timestamp)
- Optionally: set descending order with `orderDescending` (newest events first)
  - The order is always by timestamp.
  - By default `False` -> order is ascending by default (oldest events first)

#### Disable charging for an event
