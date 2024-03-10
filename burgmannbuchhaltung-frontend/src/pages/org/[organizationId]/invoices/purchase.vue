<template>
    <div>
        <div>
            <div class="sm:hidden">
                <label class="sr-only" for="tabs">Tab auswählen</label>
                <!-- Use an "onChange" listener to redirect the user to the selected tab URL. -->
                <select id="tabs" class="block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-base focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" name="tabs">
                    <option :selected="false">Kunden</option>
                    <option :selected="true">Eingangsrechnungen</option>
                </select>
            </div>
            <div class="hidden sm:block">
                <div class="border-b border-gray-200">
                    <nav aria-label="Tabs" class="-mb-px flex space-x-8">
                        <NuxtLink
                            :aria-current="false"
                            class="border-transparent text-gray-500 hover:border-gray-200 hover:text-gray-700 flex whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium"
                            to="customers"
                        >
                            Ausgangsrechnungen (an Kunden)
                            <!--                            <span
                                                            v-if="numData['customers']"
                                                            :class="[
                                                                numData['customers'] ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-900',
                                                                'ml-1.5 hidden rounded-full py-0.5 px-1.5 text-xs font-medium md:inline-block',
                                                            ]"
                                                        >{{ numData["customers"] }}</span
                                                        >-->
                        </NuxtLink>

                        <NuxtLink :aria-current="true" class="border-indigo-500 text-indigo-600 flex whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium" to="invoices">
                            Eingangsrechnungen (von Eingangsrechnungen)
                            <!--                            <span
                                                            v-if="numData['invoices']"
                                                            :class="[
                                                                numData['invoices'] ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-900',
                                                                'ml-1.5 hidden rounded-full py-0.5 px-1.5 text-xs font-medium md:inline-block',
                                                            ]"
                                                        >{{ numData["invoices"] }}</span
                                                        >-->
                        </NuxtLink>
                    </nav>
                </div>
            </div>
        </div>

        <div class="grid sm:grid-cols-6 gap-3 mt-2 max-w-max overflow-x-clip">
            <div class="sm:col-span-2 flex flex-col gap-1 border px-2">
                <button
                    class="mt-2 flex items-center gap-1.5 justify-center rounded-md bg-indigo-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                    type="button"
                    @click="createInvoice"
                >
                    <Icon class="h-5 w-5" name="heroicons:folder-plus" />
                    Erhaltene Eingangsrechnung verbuchen
                </button>
                <div>
                    <div class="relative mt-0.5 rounded-md shadow-sm">
                        <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                            <Icon aria-hidden="true" class="h-5 w-5 text-gray-400" name="heroicons:magnifying-glass" />
                        </div>
                        <input
                            id="search"
                            class="block w-full rounded-md border-0 py-1.5 pl-10 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            name="search"
                            placeholder="Suche Eingangsrechnung"
                            type="text"
                        />
                    </div>
                </div>

                <nav class="overflow-y-auto max-h-56 sm:max-h-[71vh]">
                    <ul class="divide-y divide-gray-200" role="list">
                        <li
                            v-for="invoice in invoices"
                            :key="invoice.id"
                            :class="[selectedInvoice === invoice.id ? 'bg-gray-200' : 'hover:bg-gray-100']"
                            class="flex gap-x-4 px-5 py-5 hover:cursor-pointer"
                            @click="selectInvoice(invoice.id)"
                        >
                            <div class="w-full">
                                <p class="text-sm font-semibold leading-6 text-gray-900">{{ invoice.name }}</p>
                                <div class="grid grid-cols-2 items-stretch place-content-evenly">
                                    <span class="m-1 mt-1 truncate text-xs leading-5 text-gray-500">Bezahlt: 21.215€</span>
                                    <span class="m-1 mt-1 truncate text-xs leading-5 text-gray-500">Offen: 1020€</span>

                                    <span class="m-1 mt-1 truncate text-xs leading-5 text-gray-500"><span class="tracking-tighter text-xxs">Rechnungen</span> bez.: 1400</span>
                                    <span class="m-1 mt-1 truncate text-xs leading-5 text-gray-500"><span class="tracking-tighter text-xxs">Rechnungen</span> offen: 1400</span>
                                </div>
                            </div>
                        </li>
                    </ul>
                </nav>
            </div>

            <div class="sm:col-span-4 p-3 px-4 shadow bg-white sm:max-h-[82vh] sm:overflow-y-scroll">
                <div class="px-4 sm:px-0 flex justify-between gap-1">
                    <div>
                        <h3 class="text-base font-semibold leading-7 text-gray-900">Eingangsrechnung: {{ selectedInvoice }}</h3>
                        <p class="mt-1 max-w-2xl text-sm leading-6 text-gray-500">Eingangsrechnungeninformationen</p>
                    </div>
                    <div v-if="selectedInvoice" class="mr-2">
                        <button
                            v-if="!isEditingInvoice"
                            class="mx-0.5 rounded bg-white px-2 py-1 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-red-600 hover:text-white"
                            @click="deleteInvoice"
                        >
                            <span class="flex items-center gap-1">
                                Löschen
                                <Icon class="h-5 w-5" name="heroicons:trash" />
                            </span>
                        </button>
                        <button
                            v-if="!isEditingInvoice"
                            class="mx-0.5 rounded bg-white px-2 py-1 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                            @click="isEditingInvoice = !isEditingInvoice"
                        >
                            <span class="flex items-center gap-1">
                                Bearbeiten
                                <Icon class="h-5 w-5" name="heroicons:pencil-square" />
                            </span>
                        </button>
                        <button
                            v-else
                            class="mx-0.5 rounded bg-white px-2 py-1 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-green-600 hover:text-white"
                            @click="
                                saveInvoice();
                                isEditingInvoice = !isEditingInvoice;
                            "
                        >
                            <span class="flex items-center gap-1">
                                Speichern
                                <Icon class="h-5 w-5" name="heroicons:check-circle" />
                            </span>
                        </button>
                    </div>
                </div>
                <div v-if="selectedInvoice && currentInvoice" class="mt-6 border-t border-gray-100">
                    <dl class="divide-y divide-gray-100">
                        <CrudEntryWithText v-model="currentInvoice['name']" :editable="isEditingInvoice" title="Name" />

                        <CrudEntryWithText v-model="currentInvoice['country']" :editable="isEditingInvoice" title="Land" />

                        <CrudEntry title="UID (VAT) Nr.">
                            <CrudInputText v-model="currentInvoice['vat']" :editable="isEditingInvoice" class="font-mono" />
                        </CrudEntry>

                        <CrudEntry title="€ Netto">
                            <CrudInputText v-model="currentInvoice['amountNet']" :editable="isEditingInvoice" class="font-mono" />
                        </CrudEntry>

                        <CrudEntry title="€ USt">
                            <CrudInputText v-model="currentInvoice['amountVat']" :editable="isEditingInvoice" class="font-mono" />
                        </CrudEntry>

                        <CrudEntryWithMultilineText v-model="currentInvoice['notes']" :editable="isEditingInvoice" title="Notizen" />
                        <div class="px-4 py-4 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-0">
                            <dt class="text-sm font-semibold leading-6 text-gray-900">Angehängte Dateien</dt>
                            <dd class="mt-2 text-sm text-gray-900 sm:col-span-2 sm:mt-0">
                                <ul class="divide-y divide-gray-100 rounded-md border border-gray-200" role="list">
                                    <li class="flex items-center justify-between py-4 pl-4 pr-5 text-sm leading-6">
                                        <div class="flex w-0 flex-1 items-center">
                                            <Icon aria-hidden="true" class="h-5 w-5 flex-shrink-0 text-gray-400" name="heroicons:paper-clip" />
                                            <div class="ml-4 flex min-w-0 flex-1 gap-2">
                                                <span class="truncate font-medium">Datei1.pdf</span>
                                                <span class="flex-shrink-0 text-gray-400">2.4mb</span>
                                            </div>
                                        </div>
                                        <div class="ml-4 flex-shrink-0">
                                            <a class="font-medium text-indigo-600 hover:text-indigo-500" href="#">Download</a>
                                        </div>
                                    </li>
                                    <li class="flex items-center justify-between py-4 pl-4 pr-5 text-sm leading-6">
                                        <div class="flex w-0 flex-1 items-center">
                                            <Icon aria-hidden="true" class="h-5 w-5 flex-shrink-0 text-gray-400" name="heroicons:paper-clip" />
                                            <div class="ml-4 flex min-w-0 flex-1 gap-2">
                                                <span class="truncate font-medium">Datei2.pdf</span>
                                                <span class="flex-shrink-0 text-gray-400">4.5mb</span>
                                            </div>
                                        </div>
                                        <div class="ml-4 flex-shrink-0">
                                            <a class="font-medium text-indigo-600 hover:text-indigo-500" href="#">Download</a>
                                        </div>
                                    </li>
                                </ul>
                            </dd>
                        </div>
                    </dl>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import CrudEntry from "~/components/crudentry/CrudEntry.vue";
import CrudEntryWithText from "~/components/crudentry/CrudEntryWithText.vue";
import CrudEntryWithMultilineText from "~/components/crudentry/CrudEntryWithMultilineText.vue";
import CrudInputText from "~/components/crudentry/CrudInputText.vue";
import { useOrganization } from "~/composables/organizations";

const numData = {
    customers: 42,
    invoices: 12,
};

const isEditingInvoice = ref(false);
let currentInvoice = ref(undefined);
// const currentInvoice = reactive({
//     name: "Erste Österreichische Musterfirma GmbH",
//     country: "Österreich (EU)",
//     vat: "ATU123456789",
//     address: {
//         street: "Längsgerade Doktor-Johannes-Weiß-Alleegasse 123/45/21",
//         zip: "1234",
//         city: "Leobersdorf",
//     },
//     contact: {
//         name: "Max Mustermann",
//         email: "max@mustermann.com",
//         phone: "+43 123 456 789",
//     },
//     notes: "Fugiat ipsum ipsum deserunt culpa aute sint do nostrud anim incididunt cillum culpa consequat. Excepteur qui ipsum aliquip consequat sint. Sit id mollit nulla mollit nostrud in ea officia proident. Irure nostrud pariatur mollit ad adipisicing reprehenderit deserunt qui eu.",
// });

// const invoices = ref([
// { id: "0", name: "Mustermeister GmbH" },
// { id: "1", name: "Mann des Musters GmbH" },
// { id: "2", name: "Erste Österreichische Musterfirma GmbH" },
// { id: "3", name: "Eberhart Karl Muster e.U." },
// { id: "4", name: "Markus Muster AG" },
// { id: "5", name: "Bernd Beispiel OG" },
// { id: "6", name: "Zulu Zum Beispiel OG" },
// { id: "7", name: "Elfriede Example KG" },
// { id: "8", name: "Fegyver- és Gépgyár Kft." },
// { id: "9", name: "Nacht und Nebel 1 AG & KGaA" },
// { id: "10", name: "Nacht und Nebel 2 AG & KGaA" },
// { id: "11", name: "Nacht und Nebel 3 AG & KGaA" },
// { id: "12", name: "Nacht und Nebel 4 AG & KGaA" },
// { id: "13", name: "Nacht und Nebel 5 AG & KGaA" },
// { id: "14", name: "Nacht und Nebel 6 AG & KGaA" },
// { id: "15", name: "Mike Muster" },
// { id: "16", name: "Magdalena Muster" },
// { id: "17", name: "Mark Muster" },
// { id: "18", name: "Mini Muster Gesellschaft mbH" },
// { id: "19", name: "Maximilian Muster" },
// { id: "20", name: "Muster 2020 AG" },
// { id: "21", name: "ABC XYZ" },
// { id: "22", name: "Raab-Oedenburg-Ebenfurter Eisenbahn Győr-Sopron-Ebenfurti Vasút Zrt." },
// ]);
const currentOrg = useOrganization();
const { data: invoices } = await useFetch(`/r/org/${currentOrg.value}/invoices/list`);

const selectedInvoice: Ref<string | null> = ref(null);

watch(selectedInvoice, async (newSelectedInvoice, oldSelectedInvoice) => {
    console.log(`Selected invoice: ${selectedInvoice.value}, new: ${newSelectedInvoice}, old: ${oldSelectedInvoice}`);
    const { data: returned } = await useFetch(`/r/org/${currentOrg.value}/invoices/${selectedInvoice.value}`);
    currentInvoice.value = returned.value;
    console.log(`Updated current invoice (new: ${selectedInvoice.value}): `, returned.value);
});

function selectInvoice(invoiceId: string) {
    selectedInvoice.value = invoiceId;
}

async function saveInvoice() {
    console.log("Saving invoice", currentInvoice.value);
    await $fetch(`/r/org/${currentOrg.value}/invoices/${selectedInvoice.value}`, {
        method: "PATCH",
        body: currentInvoice.value,
    });
    refreshNuxtData();
}

async function deleteInvoice() {
    console.log("Deleting invoice", currentInvoice.value);
    await $fetch(`/r/org/${currentOrg.value}/invoices/${selectedInvoice.value}`, {
        method: "DELETE",
    });
    await refreshNuxtData();
    selectedInvoice.value = null;
}

async function createInvoice() {
    console.log("Creating invoice");
    const createdId: string = await $fetch<string>(`/r/org/${currentOrg.value}/invoices/create`, {
        method: "POST",
        body: {
            name: "",
            country: "AT",
        },
    });
    refreshNuxtData();
    console.log(`Created invoice: ${createdId}, selected.`);
    selectInvoice(createdId);
    isEditingInvoice.value = true;
}

/*
function sortedMap(map: Map<string, string[]>) {
    return new Map([...map].sort((a, b) => String(a[0]).localeCompare(b[0])));
}

function groupBy(list: any, keyGetter: any): Map<string, string[]> {
    const map = new Map();
    console.log("AAAA", list.value);
    if (list == null) return map;

    for (const item of list.value) {
        const key = keyGetter(item);
        const collection = map.get(key);
        if (!collection) {
            map.set(key, [item]);
        } else {
            collection.push(item);
        }
    }
    return sortedMap(map);
}

const invoicesFiltered = computed(() => {
    return groupBy(invoices, (entry) => entry.name[0]?.toUpperCase());
});
*/
</script>
