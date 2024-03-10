<template>
    <main class="p-1">
        <div class="flex flex-row place-content-between items-center">
            <h1 class="text-2xl text-gray-900">
                Datei bearbeiten: <span class="underline">{{ fileName }}</span>
            </h1>

            <div class="flex">
                <div class="relative inline-block text-left p-1 pb-2">
                    <ActionButton
                        v-if="isChanged"
                        :icon="TrashIcon"
                        :icon-right="true"
                        class="items-center bg-red-600 text-white font-semibold inline-flex w-full justify-center rounded-md border border-red-400 px-4 py-2 text-sm shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-gray-100"
                        display-text="Zurücksetzen"
                        @click="resetData"
                    />
                </div>
                <div class="relative inline-block text-left p-1 pb-2">
                    <ActionButton
                        v-if="isChanged === false"
                        :icon="CheckIcon"
                        :icon-right="true"
                        class="items-center bg-indigo-600 text-white font-semibold inline-flex w-full justify-center rounded-md border border-indigo-500 px-4 py-2 text-sm shadow-sm hover:bg-indigo-800 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-gray-100"
                        display-text="Unbearbeitet fortfahren"
                        @click="navigateBackToSession"
                    />
                    <ActionButton
                        v-else-if="isChanged && isValid()"
                        :icon="CheckIcon"
                        :icon-right="true"
                        class="items-center bg-green-500 text-white font-semibold inline-flex w-full justify-center rounded-md border border-green-400 px-4 py-2 text-sm shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-gray-100"
                        display-text="Korrigiert speichern"
                        @click="saveData"
                    />
                    <ActionButton
                        v-else
                        :icon="ExclamationTriangleIcon"
                        :icon-right="true"
                        class="items-center bg-yellow-500 text-white font-semibold inline-flex w-full justify-center rounded-md border border-yellow-400 px-4 py-2 text-sm shadow-sm hover:bg-yellow-600 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 focus:ring-offset-gray-100"
                        display-text="Vorübergehend invalid speichern"
                        @click="saveData"
                    />
                </div>
                <dropdown-button name="Optionen">
                    <div class="py-1">
                        <p class="font-bold ml-4">Einstellungen</p>
                        <SingleLineCheckbox :input-checked="true" class="py-2" input-id="reload-pfd">Bei Größenänderung PDF-Dokument neuladen </SingleLineCheckbox>
                    </div>

                    <div class="py-1">
                        <p class="font-bold ml-4">Exportieren</p>
                        <!--
												<MenuItem>
													<a class="text-gray-700 group flex items-center px-4 py-2 text-sm hover:bg-gray-100" href="#">
														Als CSV
													</a>
												</MenuItem>-->
                        <MenuItem>
                            <button class="text-gray-700 px-4 py-2 text-sm hover:bg-gray-100 w-full text-left" @click="exportJson">Als JSON</button>
                        </MenuItem>
                        <!--<MenuItem>
													<a class="text-gray-700 group flex items-center px-4 py-2 text-sm hover:bg-gray-100" href="#">
														Als XLSX
													</a>
												</MenuItem>-->
                    </div>

                    <div class="py-1">
                        <p class="font-bold ml-4">Importieren</p>
                        <!--<MenuItem>
													<button class="text-gray-700 px-4 py-2 text-sm hover:bg-gray-100 w-full text-left" @click="importJson">
														Aus JSON
													</button>
												</MenuItem>-->
                        <MenuItem>
                            <button class="text-gray-700 px-4 py-2 text-sm hover:bg-gray-100 w-full text-left" @click="importJson">Aus JSON</button>
                        </MenuItem>
                        <!--<MenuItem>
													<button class="text-gray-700 px-4 py-2 text-sm hover:bg-gray-100 w-full text-left" @click="importJson">
														Aus JSON
													</button>
												</MenuItem>-->
                    </div>
                </dropdown-button>
            </div>
        </div>
        <hr class="" />

        <div class="mt-2">
            <div>
                <div>
                    <div class="grid text-left shadow p-1 mb-2">
                        <table>
                            <tr>
                                <th>Kategorie</th>
                                <th
                                    v-for="cat in categories"
                                    :key="cat.name"
                                    :class="[cat.expected === cat.actual ? 'text-green-600' : [cat.ignored ? 'text-blue-800' : 'text-red-500 animate-pulse']]"
                                    class="font-bold"
                                >
                                    {{ cat.name }}
                                </th>
                            </tr>
                            <tr>
                                <td class="font-bold">Soll-Stand</td>
                                <td v-for="cat in categories" :key="cat.name" :class="[cat.expected === cat.actual ? 'text-green-600' : [cat.ignored ? 'text-blue-800' : 'text-red-500']]">
                                    {{ cat.expected }}
                                </td>
                            </tr>
                            <tr>
                                <td class="font-bold">Ist-Stand</td>
                                <td v-for="cat in categories" :key="cat.name" :class="[cat.expected === cat.actual ? 'text-green-600' : [cat.ignored ? 'text-blue-800' : 'text-red-500']]">
                                    <span v-if="cat.ignored">Ignoriert.</span> <span v-else>{{ cat.actual }}</span>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="grid lg:grid-cols-2 gap-1" style="max-height: 50vh">
                        <ReceiptImportFileEditTable
                            v-model="jsondata"
                            class="shadow"
                            @edit-table="
                                (arg) => {
                                    console.log(`edited table, jsondata[${arg.index}][${arg.name}] = ${arg.value}`);
                                    jsondata[arg.index][arg.name] = arg.value;
                                    recalculateActualValues();
                                }
                            "
                        />
                        <ClientOnly>
                            <PdfViewer class="" />
                            <!--<vue-pdf-embed ref="el" class="shadow" source="http://localhost:8080/somepdf.pdf" :page="1" />-->
                        </ClientOnly>
                    </div>
                </div>
            </div>
        </div>
    </main>
</template>

<script setup>
import { MenuItem } from "@headlessui/vue";
import { CheckIcon, ExclamationTriangleIcon, TrashIcon } from "@heroicons/vue/24/outline";
import { useFileDialog } from "@vueuse/core";
import ActionButton from "~/components/buttons/ActionButton.vue";
import { useOrganization } from "~/composables/organizations";

const route = useRoute();
const orgId = useOrganization();

const sessionId = route.params.sessionId;
const fileId = route.params.fileId;

const categories = ref([]);

const jsondata = ref([]);
const original = ref([]);

function isValid() {
    for (const category of categories.value) {
        if (category.actual !== category.expected && !category.ignored) {
            console.log("Error for " + category.name + `: expected ${category.expected} - actual ${category.actual}`);
            return false;
        }
    }

    return true;
}

function resetData() {
    jsondata.value = JSON.parse(JSON.stringify(original.value));
    recalculateActualValues();
}

function recalculateActualValues() {
    const newCategories = JSON.parse(JSON.stringify(categories.value));
    newCategories.forEach(function (part, index, theArray) {
        theArray[index].actual = 0;
    });

    for (const line of jsondata.value) {
        for (const category of newCategories) {
            const catName = category.name;

            if (line[catName] !== undefined && line[catName] !== null) {
                newCategories[newCategories.findIndex((element) => element.name === catName)].actual += line[catName] * 100;
            }
        }
    }

    newCategories.forEach(function (part, index, theArray) {
        theArray[index].actual /= 100;
    });

    categories.value = newCategories;
}

function parseIntoTableData(lines) {
    const list = [];

    lines.forEach(function (l) {
        const item = { name: l.name };
        if (l.category != null) {
            item[l.category] = l.amount;
        }
        list.push(item);
    });
    console.log("Parsed into table: ", lines);

    jsondata.value = list;
    original.value = JSON.parse(JSON.stringify(jsondata.value));
}

function getNormalizedDataFromTable() {
    const list = [];

    jsondata.value.forEach(function (l) {
        const item = { name: l.name };

        if (l.Eingang) {
            item.category = "Eingang";
            item.amount = l.Eingang;
        } else if (l.Ausgang) {
            item.category = "Ausgang";
            item.amount = l.Ausgang;
        } else if (l.Kassastand) {
            item.category = "Kassastand";
            item.amount = l.Kassastand;
        }

        list.push(item);
    });

    console.log("List: ", list);
    return list;
}

const fileName = ref("");

async function fetchFileName() {
    const { data, pending, error, refresh } = await useLazyFetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/${fileId}`, {
        server: false,
    });
    await refresh();

    const file = data.value;

    if (file != null && file.fileName != null) {
        fileName.value = file.fileName;
    }
}

fetchFileName();

async function fetchContent() {
    console.log("Fetch content...");
    const { data, pending, error, refresh } = await useLazyFetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/${fileId}/content`, {
        server: false,
    });
    console.log("Fetch content errors: ", error);
    await refresh();

    if (data != null && data.value != null) {
        const file = data.value;

        console.log("Received file data: ", file);

        if (file != null && file.categories != null) {
            categories.value = file.categories;

            parseIntoTableData(file.lines);
        }
    } else {
        console.log("FETCHED DATA IS NULL: ", data);
    }
}

fetchContent();

const isChanged = computed(() => {
    return !isEqual(original.value, jsondata.value);
});

function importJson() {
    console.log("import json");
    const { files, open, reset, onChange } = useFileDialog({
        multiple: false,
        accept: "application/json",
    });

    onChange(async (files) => {
        console.log("chooser change");
        if (files.length > 0) {
            const newText = await files[0].text();
            console.log("Setting ", newText);
            jsondata.value = newText;
        } else {
            alert("Keine Datei ausgewählt!");
        }
    });

    open();
}

function exportJson() {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(jsondata.value));
    window.open(dataStr);
}

async function navigateBackToSession() {
    await navigateTo(`/org/${orgId.value}/ocr-import/sessions/${sessionId}/files`);
}

async function saveData() {
    await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/${fileId}`, {
        method: "PUT",
        body: { categories: categories.value, lines: getNormalizedDataFromTable() },
    });
    await navigateBackToSession();
}

definePageMeta({
    title: "Datei bearbeiten",
    dynamicHeader: true,
    //layout: "min"
});
</script>

<style scoped></style>
