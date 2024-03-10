<template>
    <form @submit.prevent="createSession">
        <div class="space-y-12 sm:space-y-16">
            <div>
                <p class="mt-4 max-w-2xl text-sm leading-6 text-gray-600">Legen Sie einige Optionen fest, mit welchen Ihr Import ablaufen soll.</p>

                <div class="mt-4 space-y-8 border-b border-gray-900/10 pb-12 sm:space-y-0 sm:divide-y sm:divide-gray-900/10 sm:border-t sm:pb-0">
                    <div class="sm:grid sm:grid-cols-3 sm:items-start sm:gap-4 sm:py-6">
                        <label class="block text-sm font-medium leading-6 text-gray-900 sm:pt-1.5" for="name">Sitzungs<u>n</u>ame</label>
                        <div class="mt-2 sm:col-span-2 sm:mt-0">
                            <div class="flex rounded-md shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600 sm:max-w-md">
                                <input
                                    v-model="name"
                                    accesskey="n"
                                    class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:max-w-xl sm:text-sm sm:leading-6"
                                    type="text"
                                    @keydown="nameCustomized = true"
                                />
                            </div>
                        </div>
                    </div>

                    <div class="sm:grid sm:grid-cols-3 sm:items-center sm:gap-4 sm:py-6">
                        <label class="block text-sm font-medium leading-6 text-gray-900" for="stencil"><u>S</u>chablone</label>

                        <div class="mt-2 sm:col-span-2 sm:mt-0">
                            <div class="flex items-center gap-x-3 sm:max-w-md">
                                <Listbox v-model="selected" as="div" class="w-full">
                                    <div class="relative mt-2">
                                        <ListboxButton
                                            accesskey="s"
                                            class="relative w-full sm:max-w-xl cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                        >
                                            <span class="block truncate">{{ selected.name }}</span>
                                            <span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                                <ChevronUpDownIcon aria-hidden="true" class="h-5 w-5 text-gray-400" />
                                            </span>
                                        </ListboxButton>

                                        <transition leave-active-class="transition ease-in duration-100" leave-from-class="opacity-100" leave-to-class="opacity-0">
                                            <ListboxOptions
                                                class="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm"
                                            >
                                                <ListboxOption v-for="person in stencils" :key="person.id" v-slot="{ active, selected }" :value="person" as="template">
                                                    <li :class="[active ? 'bg-indigo-600 text-white' : 'text-gray-900', 'relative cursor-default select-none py-2 pl-8 pr-4']">
                                                        <span :class="[selected ? 'font-semibold' : 'font-normal', 'block truncate']">
                                                            {{ person.name }}
                                                        </span>

                                                        <span v-if="selected" :class="[active ? 'text-white' : 'text-indigo-600', 'absolute inset-y-0 left-0 flex items-center pl-1.5']">
                                                            <CheckIcon aria-hidden="true" class="h-5 w-5" />
                                                        </span>
                                                    </li>
                                                </ListboxOption>
                                            </ListboxOptions>
                                        </transition>
                                    </div>
                                </Listbox>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div>
            <IndigoSubmitButton ref="submit" v-model:loading="loading" accesskey="e" :icon="FolderPlusIcon">
                <span>Neue Importsitzung <u>e</u>rstellen</span>
            </IndigoSubmitButton>
        </div>
    </form>
</template>

<script lang="ts" setup>
import { Listbox, ListboxButton, ListboxOption, ListboxOptions } from "@headlessui/vue";
import { CheckIcon, ChevronUpDownIcon, FolderPlusIcon } from "@heroicons/vue/20/solid";

import { useDateFormat, useNow } from "@vueuse/core";
import IndigoSubmitButton from "~/components/buttons/IndigoSubmitButton.vue";
import { useOrganization } from "~/composables/organizations";

const submit = ref(null);

const stencils = [
    {
        id: 1,
        name: "OCR Schablone Peter",
    },
    {
        id: 2,
        name: "Test 1",
    },
    {
        id: 3,
        name: "XLSX Import",
    },
];

const selected = ref(stencils[0]);

const nameCustomized = ref(false);
const formatted = useDateFormat(useNow(), "DD.MM.YYYY HH:mm:ss");
const name = ref(`Sitzung vom ${formatted.value}`);

watch(formatted, () => {
    if (!nameCustomized.value) {
        name.value = `Sitzung vom ${formatted.value}`;
    }
});

const loading = ref(false);

const orgId = useOrganization();

async function createSession() {
    loading.value = true;
    console.log(`Creating session: ${name.value}, using: ${selected.value.id} (${selected.value.name})`);

    await $fetch(`/r/org/${orgId.value}/receipt-import/start-session`, {
        method: "POST",
        query: {
            stencilId: selected.value.id,
            name: name.value,
        },
    }).then(async (session) => {
        console.log("Session: ", session);
        await navigateTo(`/org/${orgId.value}/ocr-import/sessions/${session.id}/files`);
    });
}

definePageMeta({
    title: "Neue Importsitzung anlegen",
});
</script>

<style scoped></style>
