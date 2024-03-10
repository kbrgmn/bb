<template>
    <Icon :name="getIconName(currency)"/>
</template>

<script lang="ts" setup>
const props = defineProps({
    "currency": String
})

const fallbackIcon = "tabler:currency"
const icons = {
    // most used
    EUR: "tabler:currency-euro",
    USD: "tabler:currency-dollar",

    GBP: "tabler:currency-pound",
    JPY: "tabler:currency-yen",
    RMB: "tabler:currency-renminbi",
    CNY: "tabler:currency-yuan",

    // Around Europe
    HUF: "tabler:currency-forint",
    CHF: "tabler:currency-frank", // unofficial symbol
    CZK: "tabler:currency-krone-czech",
    DKK: "tabler:currency-krone-danish",
    SEK: "tabler:currency-krone-swedish",
    RON: "tabler:currency-leu", // unofficial?
    MDL: "tabler:currency-leu",
    PLN: "tabler:currency-zloty",

    ISK: "tabler:currency-krone-swedish", // no specific icon
    NOK: "tabler:currency-krone-swedish", // no specific icon

    ALL: "tabler:currency-leu", // unofficial - just similar l
    GEL: "tabler:currency-lari",
    TRY: "tabler:currency-lira",


    // Dollars
    AUD: "tabler:currency-dollar-australian",
    CAD: "tabler:currency-dollar-canadian",
    SGD: "tabler:currency-dollar-singapore",
    GYD: "tabler:currency-dollar-guyanese",
    BND: "tabler:currency-dollar-brunei",
    ZWL: "tabler:currency-dollar-zimbabwean",

    HKD: "tabler:currency-dollar", // no specific icon
    NZD: "tabler:currency-dollar", // no specific icon

    // Fake dollars
    ARS: "tabler:currency-dollar",
    COP: "tabler:currency-dollar",
    CUC: "tabler:currency-dollar",
    CUP: "tabler:currency-dollar",
    DOP: "tabler:currency-dollar",
    MXN: "tabler:currency-dollar",
    UYU: "tabler:currency-dollar",
    BBD: "tabler:currency-dollar",
    BSD: "tabler:currency-dollar",
    BZD: "tabler:currency-dollar",
    BMD: "tabler:currency-dollar",
    CLP: "tabler:currency-dollar",
    XCD: "tabler:currency-dollar",
    FJD: "tabler:currency-dollar",
    JMD: "tabler:currency-dollar",
    KYD: "tabler:currency-dollar",
    LRD: "tabler:currency-dollar",
    NAD: "tabler:currency-dollar",
    SBD: "tabler:currency-dollar",
    SRD: "tabler:currency-dollar",
    TTD: "tabler:currency-dollar",

    // Fake euro
    CHE: "tabler:currency-euro",

    // Fake frank
    CDF: "tabler:currency-frank",
    CHW: "tabler:currency-frank",
    XPF: "tabler:currency-frank",
    XOF: "tabler:currency-frank",
    XAF: "tabler:currency-frank",
    BIF: "tabler:currency-frank",
    DJF: "tabler:currency-frank",
    GNF: "tabler:currency-frank",
    KMF: "tabler:currency-frank",
    RWF: "tabler:currency-frank",

    // Fake pound
    EGP: "tabler:currency-pound",
    FKP: "tabler:currency-pound",
    GIP: "tabler:currency-pound",
    SHP: "tabler:currency-pound",
    SYP: "tabler:currency-pound",
    SDG: "tabler:currency-pound",
    SSP: "tabler:currency-pound",

    // Other no specific icon
    TWD: "tabler:currency-renminbi",

    // Dinar
    KWD: "tabler:currency-dinar", // correct I think
    DZD: "tabler:currency-dinar", // incorrect symbol?
    IQD: "tabler:currency-dinar", // incorrect symbol?
    JOD: "tabler:currency-dinar", // incorrect symbol?
    TND: "tabler:currency-dinar", // incorrect symbol?

    BHD: "tabler:currency-bahraini",
    LYD: "tabler:currency-lyd",

    // Rest
    AFN: "tabler:currency-afghani",
    THB: "tabler:currency-baht",
    AED: "tabler:currency-dirham",
    VND: "tabler:currency-dong",
    AMD: "tabler:currency-dram",
    AWG: "tabler:currency-florin",
    ANG: "tabler:currency-florin",
    PYG: "tabler:currency-guarani",
    UAH: "tabler:currency-hryvnia",
    IRR: "tabler:currency-iranian-rial",
    LAK: "tabler:currency-kip",
    TMT: "tabler:currency-manat",
    AZN: "tabler:currency-manat", // unofficial?
    NGN: "tabler:currency-naira",
    TOP: "tabler:currency-paanga",
    PHP: "tabler:currency-peso",
    GTQ: "tabler:currency-quetzal",
    BRL: "tabler:currency-real",
    SAR: "tabler:currency-riyal",
    RUB: "tabler:currency-rubel",
    BYN: "tabler:currency-rubel", // unofficial
    MVR: "tabler:currency-rufiyaa",
    NPR: "tabler:currency-rupee-nepalese",
    INR: "tabler:currency-rupee",
    ILS: "tabler:currency-shekel",
    KGS: "tabler:currency-som", // unofficial?
    UZS: "tabler:currency-som", // unofficial?
    BDT: "tabler:currency-taka",
    KZT: "tabler:currency-tenge",
    MNT: "tabler:currency-tugrik",
    KRW: "tabler:currency-won",
    KPW: "tabler:currency-won",

    // Crypto
    BTC: "tabler:currency-bitcoin",
    DODGE: "tabler:currency-dogecoin",
    ETH: "tabler:currency-ethereum",
    LTC: "tabler:currency-litecoin",
    XMR: "tabler:currency-monero",
    XNO: "tabler:currency-nano",
    XRP: "tabler:currency-ripple",
    SOL: "tabler:currency-solana",

    // Precious metals
    XAU: "mdi:gold",

    // Basket units
    XBA: "tabler:file-euro",
    XBB: "tabler:file-euro",
    XBC: "tabler:file-euro",
    XBD: "tabler:file-euro",
    USN: "tabler:calendar-dollar",

    XTS: "tabler:test-pipe",
    XXX: fallbackIcon
}


function getIconName(currency: string | undefined): string {
    const icon = icons[currency]
    return icon != undefined ? icon : fallbackIcon;
}
</script>
