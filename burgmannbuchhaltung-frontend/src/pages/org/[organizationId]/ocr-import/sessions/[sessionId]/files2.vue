<template>
    <div>
        <div class="mt-2 sm:col-span-2 sm:mt-0">
            <div class="flex max-w-2xl justify-center rounded-md border-2 border-dashed border-gray-400 px-6 pt-3 pb-3" v-bind="getRootProps()">
                <div class="space-y-1 text-center">
                    <DocumentPlusIcon class="mx-auto h-12 w-12 text-gray-400" />
                    <div v-if="isDragActive" class="rounded-md bg-white font-medium text-indigo-600">Loslassen um Dateien abzulegen...</div>
                    <div v-else class="flex text-sm text-gray-600">
                        <label
                            class="relative cursor-pointer rounded-md bg-white font-medium text-indigo-600 focus-within:outline-none focus-within:ring-2 focus-within:ring-indigo-500 focus-within:ring-offset-2 hover:text-indigo-500"
                            for="file-upload"
                        >
                            <button accesskey="a" @click="fileChooserOpen">Klicken um Dokumente <u>a</u>uszuwählen</button>
                            <input v-bind="getInputProps()" />
                            <!--id="file-upload" class="sr-only" name="file-upload" type="file"-->
                        </label>
                        <p class="pl-1 hidden sm:block">oder per <span class="font-semibold">drag & drop</span> in dieses Feld ziehen.</p>
                    </div>
                    <p class="text-xs text-gray-500">PDF, PNG, JPG; bis zu 10MB</p>
                </div>
            </div>
        </div>

        <h2 class="text-1xl mt-3 text-gray-800 sm:text-xl flex flex-row justify-between">
            <span>
                Dateiliste<span v-if="!requestPending"> ({{ files.length }})</span>:
            </span>

            <span v-if="uploadingCount > 0" class="text-xs sm:text-lg inline-flex items-center"> <InlineLoadingCircle class="pr-1.5" size="4" />Upload - {{ uploadingCount }} verbleibend... </span>
        </h2>

        <div class="border-gray-300 px-4 py-5 sm:px-6 border-2 overflow-x-scroll" style="min-height: 50vh; max-height: 50vh">
            <ul class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mt-1" role="list">
                <li v-if="requestPending" class="rounded-lg border-2 border-dotted border-gray-200 p-3 text-center mt-1">
                    <InlineLoadingCircle />
                    <span class="mt-2 block text-sm font-semibold text-gray-900">Lade Dateien...</span>
                </li>
                <li v-else-if="files.length === 0" class="rounded-lg border-2 border-dotted border-gray-200 p-3 text-center mt-1">
                    <FolderMinusIcon class="mx-auto h-9 w-9 text-gray-400" />
                    <span class="mt-2 block text-sm font-semibold text-gray-900">Noch keine Dateien hochgeladen</span>
                </li>

                <li v-for="file in files" :key="file.id" class="col-span-1 divide-y divide-gray-200 rounded-lg bg-white shadow">
                    <div class="flex w-full items-center justify-between space-x-6 p-4 pb-3">
                        <div class="flex-1 truncate">
                            <div class="flex items-center space-x-3">
                                <h3 class="truncate text-sm font-medium text-gray-900">{{ file.fileName }}</h3>
                                <span v-if="file['edited'] === true" class="inline-block flex-shrink-0 rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">Bearbeitet.</span>
                            </div>

                            <div v-if="file['state'] === 'PROCESSING'" class="bg-blue-400 p-1 mt-2">
                                <LoadingCircle>Verarbeite...</LoadingCircle>
                            </div>
                            <div v-else-if="file['state'] === 'READY'" class="bg-green-50 p-1 mt-2">
                                <p class="truncate text-sm text-green-800 inline-flex flex-row items-center">
                                    <CheckIcon class="h-5 mr-1" />
                                    {{ file.title }}
                                </p>
                            </div>
                            <div v-else-if="file['state'] === 'CORRECTION'" class="bg-yellow-50 p-1 mt-2">
                                <p class="truncate text-sm text-yellow-600 inline-flex flex-row items-center">
                                    <ExclamationTriangleIcon class="h-5 mr-1" />
                                    {{ file.title }}
                                </p>
                            </div>
                            <div v-else-if="file['state'] === 'FAILURE'" class="bg-red-50 p-1 mt-2">
                                <p class="truncate text-sm text-red-800 inline-flex flex-row items-center">
                                    <XMarkIcon class="h-5 mr-1" />
                                    Scan passt nicht zu Schablone.
                                </p>
                            </div>
                            <div v-else>
                                <p class="truncate text-sm">Unbekannter Status.</p>
                            </div>
                        </div>
                    </div>
                    <div>
                        <div class="-mt-px flex divide-x divide-gray-200">
                            <div class="flex w-0 flex-1">
                                <CardActionButton
                                    :icon="TrashIcon"
                                    action-text="Löschen..."
                                    class="text-gray-900 hover:bg-red-600 hover:text-white rounded-bl-md"
                                    display-text="Löschen"
                                    @click="deleteFile(file.id)"
                                />
                            </div>
                            <div class="-ml-px flex w-0 flex-1">
                                <NuxtLink
                                    v-if="file['state'] !== 'LOADING'"
                                    :to="`/org/${orgId}/ocr-import/sessions/${sessionId}/edit/${file.id}`"
                                    class="relative inline-flex w-0 flex-1 items-center justify-center gap-x-1.5 rounded-br-lg border border-transparent py-4 text-sm font-semibold group text-gray-900 hover:bg-blue-400 hover:text-white"
                                >
                                    <PencilSquareIcon aria-hidden="true" class="h-5 w-5 text-gray-400 group-hover:text-gray-100" />
                                    <span class="group-hover:text-gray-100">Bearbeiten</span>
                                </NuxtLink>
                                <LoadingCircle v-else class="self-center mx-auto" colors="dark" />
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
        </div>

        <div class="flex justify-between gap-2 mt-1 pr-2">
            <SubmitButton :icon="TrashIcon" class="bg-red-600 text-white" @click="deleteImportSession">Diese Importsitzung löschen</SubmitButton>

            <div>
                <SubmitButton v-if="files.length === 0" class="bg-gray-300 text-white" disabled="disabled">Noch keine Dokumente zum Fortfahren</SubmitButton>
                <SubmitButton
                    v-else-if="files.every((element) => element.state === 'READY')"
                    :icon="CheckIcon"
                    class="bg-green-600 hover:bg-green-700 focus:ring-green-500 border-green-500 text-neutral-50"
                    @click="generateEntries"
                    >Mit Importsitzung fortfahren
                </SubmitButton>
                <SubmitButton
                    v-else-if="files.some((element) => element.state === 'PROCESSING')"
                    class="bg-indigo-600 text-white hover:bg-indigo-600 focus:ring-indigo-500 border-indigo-500"
                    loading="true"
                    @click="generateEntries"
                    >Verarbeite...
                </SubmitButton>
                <SubmitButton
                    v-else-if="files.some((element) => element.state === 'READY')"
                    :icon="ExclamationTriangleIcon"
                    class="bg-yellow-500 hover:bg-yellow-600 focus:ring-yellow-400 border-yellow-400 text-white"
                    @click="generateEntries"
                    >Teilweise fortfahren ({{ files.filter((element) => element.state === "READY").length }} valide Dokumente)
                </SubmitButton>
                <SubmitButton disabled v-else :icon="ExclamationTriangleIcon" class="bg-red-400 text-neutral-200" @click="generateEntries">Keine validen Dokumente. </SubmitButton>
            </div>
        </div>
    </div>
</template>

<script setup>
import { CheckIcon, DocumentPlusIcon, ExclamationTriangleIcon, FolderMinusIcon, PencilSquareIcon, TrashIcon, XMarkIcon } from "@heroicons/vue/24/outline";
import { useEventSource } from "@vueuse/core";
import { useDropzone } from "vue3-dropzone";
import CardActionButton from "~/components/buttons/CardActionButton.vue";
import SubmitButton from "~/components/buttons/SubmitButton.vue";
import InlineLoadingCircle from "~/components/loading/InlineLoadingCircle.vue";
import LoadingCircle from "~/components/LoadingCircle.vue";
import { useOrganization } from "~/composables/organizations";

const route = useRoute();
const orgId = useOrganization();

const sessionId = route.params.sessionId;

const fileChooserOpen = ref(false);

const files = ref([]);

let requestPending = ref(true);

// FETCH CONTENT

async function fetchContent() {
    const { data, pending, error, refresh } = await useLazyFetch(`/r/org/${orgId.value}/receipt-import/${sessionId}`, {
        initialCache: false,
        server: false,
    });
    requestPending = pending;

    watch(data, (newData) => {
        console.log("NEW DATA: ", newData);
        if (data != null && data.value != null) {
            const session = data.value;

            console.log("Received session data: ", session);
            files.value = session.files;
        } else {
            console.log("No session data received!");
        }
    });

    console.log("Fetch error: ", error.value);
}

watchEffect(async () => {
    await fetchContent();
});

onMounted(() => {
    // if (process && process.client) { // FIXME
    const { status, data, error, close } = useEventSource(`/r/org/${orgId.value}/receipt-import/${sessionId}/subscribe/sse`);

    watch(status, (obj) => {
        console.log("SSE Status changed", obj);
    });
    watch(data, async (obj) => {
        console.log("SSE data:", obj);

        // noinspection JSUnresolvedReference
        const splitted = obj.split(":");
        const type = splitted[0];
        const args = splitted[1];

        switch (type) {
            case "update-status": {
                const fileId = args;

                const idx = files.value.findIndex((element) => element.id === args);
                console.log("Replacing idx", idx);

                await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/${fileId}`).then(async (fileStatusEntry) => {
                    console.log("New fileStatusEntry", fileStatusEntry);
                    files.value[idx] = fileStatusEntry;
                });
                break;
            }
        }
    });

    watch(error, (obj) => {
        console.log("SSE Error", obj);
    });
});
// }

async function deleteFile(fileId) {
    await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/${fileId}`, {
        method: "DELETE",
    });
    await fetchContent();
}

const uploadingCount = ref(0);

const saveFiles = async (filesToSave) => {
    uploadingCount.value = filesToSave.length;

    for (const file of filesToSave) {
        const formData = new FormData();
        formData.append("documents[]", file);

        const { data, pending, error } = await useFetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/file-submit`, {
            method: "POST",
            body: formData,
        });

        if (error.value == null) {
            console.log("Data: " + JSON.stringify(data.value));
            for (const entry of data.value) {
                files.value.push(entry);
                console.log("Added file: " + JSON.stringify(entry));
            }
        } else {
            console.log("Pending: " + JSON.stringify(pending.value));
            console.log("Error: " + JSON.stringify(error.value));

            errors += {
                files: filesToSave,
                error: error.value,
            };
        }

        uploadingCount.value--;
    }
};

let errors = [];

function onDrop(acceptFiles, rejectReasons) {
    saveFiles(acceptFiles);

    if (rejectReasons != null) {
        console.log("Reject reasons: " + rejectReasons);
        console.log("Errors: " + JSON.stringify(errors));
    }
}

const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

async function generateEntries() {
    await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/generate`, {
        method: "POST",
    });
    await navigateTo(`/org/${orgId.value}/ocr-import/sessions/${sessionId}/entries`);
}

async function deleteImportSession() {
    await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}`, {
        method: "DELETE",
    });
    navigateTo(`/org/${orgId.value}/ocr-import/sessions/list`);
}

definePageMeta({
    title: "Importsitzung",
});
</script>

<style scoped></style>
