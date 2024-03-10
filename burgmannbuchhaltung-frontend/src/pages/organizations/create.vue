<template>
    <MassDataEntry>
        <div class="rounded-md bg-blue-50 p-4 -mb-4">
            <div class="mx-2 flex-1 md:flex md:justify-around items-center">
                <p class="text-sm text-blue-700 flex items-center">
                    <Icon name="heroicons:pencil-square" aria-hidden="true" class="h-5 w-5 text-blue-400 mr-2" />
                    Haben Sie eine Österreichische UID-Nummer? Dann können bereits einige Daten vorausgefüllt werden:
                </p>

                <div class="flex gap-2">
                    <input
                        id="email"
                        class="block w-full rounded-md border-0 py-0.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                        name="email"
                        placeholder="ATU12345678"
                        type="email"
                    />
                    <button
                        class="rounded-md bg-indigo-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        type="button"
                    >
                        Vorbefüllen
                    </button>
                </div>
            </div>
        </div>

        <MassDataEntryGroup title="Stammdaten" description="Tragen Sie hier die grundlegenden Informationen über die Organisation ein.">
            <MassDataEntryItem
                title="Name"
                labeled-id="name"
                class="sm:col-span-4"
                description="Bitte darauf achten, dass der Name genau mit dem im Firmenbuch / GISA hinterlegtem Namen übereinstimmt."
                :required="true"
            >
                <input
                    id="name"
                    autocomplete="organization"
                    class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                    name="name"
                    type="text"
                    required
                    v-model="data.name"
                />
            </MassDataEntryItem>
            <MassDataEntryItem title="Rechtsform" labeled-id="legal-structure" class="col-span-full" :required="true">
                <select
                    id="legal-structure"
                    class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:max-w-sm sm:text-sm sm:leading-6"
                    name="legal-structure"
                    required
                    v-model="data.legalStructure"
                >
                    <option value="Einzelunternehmer">Einzelunternehmer/in</option>
                    <option value="Eingetragener_Unternehmer">Eingetragener Unternehmer (e.U.)</option>
                    <option value="Kommanditgesellschaft">Offene Gesellschaft (OG)</option>
                    <option value="Offene_Gesellschaft">Kommanditgesellschaft (KG)</option>
                    <option value="Gesellschaft_mit_beschränkter_Haftung">Gesellschaft mit beschränkter Haftung (GmbH)</option>
                    <!-- <option value="Aktiengesellschaft">Aktiengesellschaft (AG)</option>-->
                </select>
            </MassDataEntryItem>

            <MassDataEntryItem title="Adresse" labeled-id="address" class="sm:col-span-3" required>
                <fieldset class="-space-y-px rounded-md shadow-sm bg-white w-full" id="address">
                    <div>
                        <label for="country" class="sr-only">Land</label>
                        <select
                            id="country"
                            name="country"
                            autocomplete="country-name"
                            class="relative block w-full rounded-none rounded-t-md border-0 bg-transparent py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            v-model="data.country"
                        >
                            <option value="AT">Österreich</option>
                            <option value="DE">Deutschland</option>
                        </select>
                    </div>
                    <div class="grid grid-cols-3">
                        <label for="postal-code" class="sr-only">PLZ</label>
                        <input
                            type="text"
                            name="postal-code"
                            id="postal-code"
                            autocomplete="postal-code"
                            class="relative block w-full rounded-none border-0 bg-transparent py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            placeholder="PLZ"
                            pattern="(\d{4,5})"
                            @keydown="inputKeyCheck('0123456789', $event)"
                            v-model="data.zip"
                        />

                        <label for="city" class="sr-only">Ort</label>
                        <input
                            type="text"
                            name="city"
                            id="city"
                            autocomplete="address-level2"
                            class="col-span-2 relative block w-full rounded-none border-0 bg-transparent py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            placeholder="Ort"
                            v-model="data.city"
                        />
                    </div>
                    <div>
                        <label for="street-address" class="sr-only">Adresse</label>
                        <input
                            type="text"
                            name="street-address"
                            id="street-address"
                            autocomplete="street-address"
                            class="relative block w-full rounded-none rounded-b-md border-0 bg-transparent py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:z-10 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                            placeholder="Adresse"
                            v-model="data.address"
                        />
                    </div>
                </fieldset>
            </MassDataEntryItem>
        </MassDataEntryGroup>

        <MassDataEntryGroup
            title="Kundeninformationen"
            description="Tragen Sie hier die öffentlichen Kontaktinformationen für diese Organisation ein. Diese Informationen scheinen z.B. in Rechnungen an Kunden auf."
        >
            <MassDataEntryItem title="Website" labeled-id="website" class="sm:col-span-4" inline-icon="heroicons:globe-asia-australia">
                <div class="pl-10 w-full flex items-center rounded-md shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600 sm:max-w-md">
                    <span class="flex select-none items-center text-gray-500 sm:text-sm">https://</span>
                    <input
                        id="website"
                        class="block flex-1 border-0 bg-transparent py-1.5 pl-1 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm sm:leading-6"
                        name="website"
                        placeholder="www.example.com"
                        type="text"
                        v-model="data.website"
                    />
                </div>
            </MassDataEntryItem>
            <MassDataEntryItem title="E-Mail Adresse" labeled-id="email" class="sm:col-span-4" inline-icon="heroicons:envelope">
                <input
                    id="email"
                    autocomplete="email"
                    class="pl-10 block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                    name="email"
                    type="email"
                    placeholder="max@mustermann.at"
                    v-model="data.email"
                />
            </MassDataEntryItem>
            <MassDataEntryItem title="Telefonnummer" labeled-id="phone" class="sm:col-span-3" inline-icon="heroicons:phone">
                <input
                    id="phone"
                    autocomplete="tel"
                    class="pl-10 block w-full font-mono rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                    name="phone"
                    type="tel"
                    pattern="(\d+)"
                    placeholder="+43 1 123 456 789"
                    @keydown="inputKeyCheck('0123456789+ ', $event)"
                    v-model="data.phone"
                />
            </MassDataEntryItem>
        </MassDataEntryGroup>

        <MassDataEntryGroup title="Steuern- & Abgabendaten" description="Tragen Sie hier die grundlegenden Informationen über die Organisation ein.">
            <MassDataEntryItem title="Kleinunternehmerregelung" description="" labeled-id="kleinunternehmerregelung" class="sm:col-span-4">
                <div class="flex h-6 items-center">
                    <input
                        id="kleinunternehmerregelung"
                        class="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                        name="kleinunternehmerregelung"
                        type="checkbox"
                        v-model="data.kleinunternehmerregelung"
                    />
                </div>
                <div class="text-sm leading-6">
                    <label class="font-medium text-gray-900" for="comments">Kleinunternehmerregelung</label>
                    <p class="text-gray-500">Machen Sie von der Kleinunternehmerregelung von Gebrauch?</p>
                </div>
            </MassDataEntryItem>

            <MassDataEntryItem title="Umsatzsteuer-Identifikationsnummer" description='Auch als "ATU-Nummer" bzw. englisch "VAT-Nummer" bekannt.' labeled-id="vat" class="sm:col-span-4">
                <input
                    id="vat"
                    autocomplete="vat"
                    class="block w-32 rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                    name="vat"
                    placeholder="ATU12345678"
                    type="text"
                    v-model="data.vat"
                />
            </MassDataEntryItem>
        </MassDataEntryGroup>
    </MassDataEntry>
</template>

<script lang="ts" setup>
import MassDataEntry from "~/components/massdataentry/MassDataEntry.vue";
import MassDataEntryGroup from "~/components/massdataentry/MassDataEntryGroup.vue";
import MassDataEntryItem from "~/components/massdataentry/MassDataEntryItem.vue";
import { inputKeyCheck } from "~/composables/stringutils";

definePageMeta({
    title: "Organisation anlegen",
    layout: "menunonavs",
});

type OrganisationData = {
    name: string | null;
    legalStructure: string | null;
    country: string | null;
    zip: string | null;
    city: string | null;
    address: string | null;
    website: string | null;
    email: string | null;
    phone: string | null;
    kleinunternehmerregelung: boolean | null;
    vat: string | null;
};

const data: Ref<OrganisationData> = ref({
    country: "AT",
} as OrganisationData);
</script>

<style scoped></style>
