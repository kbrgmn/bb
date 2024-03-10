<template>
    <div>
        <div>
            <div class="sm:hidden">
                <label class="sr-only" for="tabs">Tab auswählen</label>
                <!-- Use an "onChange" listener to redirect the user to the selected tab URL. -->
                <select id="tabs" class="block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-base focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" name="tabs">
                    <option :selected="true">Kunden</option>
                    <option :selected="false">Lieferanten</option>
                </select>
            </div>
            <div class="hidden sm:block">
                <div class="border-b border-gray-200">
                    <nav aria-label="Tabs" class="-mb-px flex space-x-5">
                        <NuxtLink
                            :aria-current="true"
                            class="border-indigo-500 text-indigo-600 flex whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium"
                            to="sales"
                        >
                            Ausgangsrechnungen (an Kunden)
                            <!--<span
                                v-if="numData['customers']"
                                :class="[
                                    numData['customers'] ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-900',
                                    'ml-1.5 hidden rounded-full py-0.5 px-1.5 text-xs font-medium md:inline-block',
                                ]"
                            >{{ numData["customers"] }}</span>-->
                        </NuxtLink>

                        <NuxtLink
                            :aria-current="false"
                            class="border-transparent text-gray-500 hover:border-gray-200 hover:text-gray-700 flex whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium"
                            to="purchase"
                        >
                            Eingangsrechnungen (von Lieferanten)
<!--                            <span
                                v-if="numData['suppliers']"
                                :class="[
                                    numData['suppliers'] ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-900',
                                    'ml-1.5 hidden rounded-full py-0.5 px-1.5 text-xs font-medium md:inline-block',
                                ]"
                            >{{ numData["suppliers"] }}</span>-->
                        </NuxtLink>
                    </nav>
                </div>
            </div>
        </div>

        <h1 class="mt-3 -mb-3 font-light text-xl">Letzte Verkäufe:</h1>

        <ul class="divide-y divide-gray-100" role="list">
            <li v-for="invoice of invoices" :key="invoice.id" class="flex shadow px-2.5 py-1.5 justify-between mt-4">
                <div class="flex flex-col">
                    <span class="font-semibold">{{ invoice._companyName }} - Rechnung {{ invoice.id }}</span>
                    <span class="">vom: {{ invoice.creationDate.toLocaleDateString("de") }}</span>

                    <span>Betrag: <span class="">{{ invoice.currency }} {{ invoice.amountGross }}</span> netto / {{ invoice.currency }} {{ invoice.amountTotal }} brutto</span>
                </div>

                <div v-if="invoice.paidInFullDate" class="text-green-600 flex gap-1.5 p-1">
                    <Icon class="h-7 w-7" name="heroicons:document-check" />
                    <div class="flex flex-col w-40">
                        <span class="">Vollständig bezahlt</span>
                        per {{ invoice.paidInFullDate.toLocaleDateString("de") }}
                    </div>
                </div>
                <div v-else-if="invoice.paid != null && invoice.paid < invoice.amountTotal" class="text-green-900 flex gap-1.5 p-1">
                    <Icon class="h-7 w-7" name="heroicons:chart-pie" />
                    <div class="flex flex-col w-40">
                        <span class="">Teilweise bezahlt</span>
                        {{ invoice.amountTotal - invoice.paid }} {{ invoice.currency }} verbleibend
                    </div>
                </div>
                <div v-else-if="invoice.paid == null && (new Date()) < invoice.payBy" class="flex gap-1.5 p-1">
                    <Icon class="h-7 w-7" name="heroicons:clock" />
                    <div class="flex flex-col w-40">
                        <span class="">Noch nicht bezahlt</span>
                        {{ Math.floor((invoice.payBy - new Date()) / 1000 / 60 / 60 / 24) }} Tage verbleibend
                    </div>
                </div>
                <div v-else-if="invoice.paid == null && invoice.paid < invoice.amountTotal" class="text-red-600 flex gap-1.5 p-1">
                    <Icon class="h-7 w-7" name="heroicons:exclamation-circle" />
                    <div class="flex flex-col w-40">
                        <span class="">Nicht bezahlt</span>
                        {{ Math.floor((new Date() - invoice.payBy) / 1000 / 60 / 60 / 24) }} Tage überfällig
                    </div>
                </div>
                <div v-else>
                    {{ invoice }}
                </div>
            </li>
        </ul>
    </div>
</template>

<script lang="ts" setup>
const _currentDate = new Date();

const _addWeeksToDate = (dateObj: Date, numberOfWeeks: number) => {
    const newDate = new Date(dateObj);
    newDate.setDate(dateObj.getDate() + numberOfWeeks * 7);
    return newDate;
};

type Invoice = {
    id: string;
    companyId: number;
    _companyName: string;
    currency: string;
    amountGross: number;
    amountTotal: number;
    paid: number | null;
    creationDate: Date;
    payBy: Date;
    paidInFullDate: Date | null;
};

const invoices: Invoice[] = [
    {
        id: "00006",
        companyId: 123,
        _companyName: "Erste Österreichische Musterfirma AG",
        currency: "EUR",
        amountGross: 1234.56,
        amountTotal: 1481.47,
        paid: 1481.47,
        creationDate: _currentDate,
        payBy: _addWeeksToDate(_currentDate, 1),
        paidInFullDate: _addWeeksToDate(_currentDate, 1),
    },
    {
        id: "00007",
        companyId: 124,
        _companyName: "Zweite Österreichische Musterfirma AG",
        currency: "EUR",
        amountGross: 463.56,
        amountTotal: 566.28,
        paid: null,
        creationDate: _currentDate,
        payBy: _addWeeksToDate(_currentDate, 1),
        paidInFullDate: null,
    },
    {
        id: "00008",
        companyId: 124,
        _companyName: "Zweite Österreichische Musterfirma AG",
        currency: "EUR",
        amountGross: 463.56,
        amountTotal: 566.28,
        paid: null,
        creationDate: _currentDate,
        payBy: _addWeeksToDate(_currentDate, -2),
        paidInFullDate: null,
    },
    {
        id: "00009",
        companyId: 124,
        _companyName: "Zweite Österreichische Musterfirma AG",
        currency: "EUR",
        amountGross: 463.56,
        amountTotal: 566.28,
        paid: 100,
        creationDate: _currentDate,
        payBy: _addWeeksToDate(_currentDate, 2),
        paidInFullDate: null,
    }
];
</script>
