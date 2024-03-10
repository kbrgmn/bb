<template>
    <main>
        <div class="ml-1">
            <NuxtLink to="create">
                <button accesskey="e"
                        class="items-center bg-indigo-600 text-white font-semibold inline-flex justify-center rounded-xl border border-indigo-500 px-4 py-2 text-sm shadow-sm hover:bg-indigo-800 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-gray-100">
                    <FolderPlusIcon class="h-5 pr-1"/>
                    <span>Neue Importsitzung <u>e</u>rstellen</span>
                </button>
            </NuxtLink>
        </div>

        <div class="mt-3">

            <div v-if="requestPending" class="text-center p-2 m-2">
                <InlineLoadingCircle/>
                <h3 class="mt-2 text-sm font-semibold text-gray-900">Lade Importsitzungen...</h3>
            </div>

            <div v-else-if="sessions.length === 0" class="text-center p-2 m-2">
                <svg aria-hidden="true" class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          vector-effect="non-scaling-stroke"/>
                </svg>
                <h3 class="mt-2 text-sm font-semibold text-gray-900">Noch keine Importsitzungen</h3>
                <p class="mt-1 text-sm text-gray-500">Fangen Sie an, indem Sie eine neue erstellen:</p>
                <div class="mt-2">
                    <NuxtLink to="create">
                        <button
                            class="animate-pulse inline-flex items-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                            type="button">
                            <FolderPlusIcon aria-hidden="true" class="-ml-0.5 mr-1.5 h-5 w-5"/>
                            Neue Importsitzung erstellen
                        </button>
                    </NuxtLink>
                </div>
            </div>

            <div class="overflow-hidden bg-white shadow sm:rounded-md">
                <ul class="divide-y divide-gray-200" role="list">
                    <li v-for="session in sessions" :key="session.id">
                        <NuxtLink :to="`${session.id}/files`" class="block hover:bg-gray-50">
                            <div class="flex items-center px-4 py-4 sm:px-6">
                                <div class="flex min-w-0 flex-1 items-center">
                                    <div class="min-w-0 flex-1 px-4 md:grid md:grid-cols-2 md:gap-4">
                                        <div>
                                            <p class="truncate text-sm font-medium text-indigo-600">
                                                {{ session.name }}
                                            </p>
                                            <p class="mt-2 flex items-center text-sm text-gray-500">
                                                <ClipboardDocumentListIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-gray-400"/>
                                                <span v-if="session.receiptCount === 0" class="truncate">Noch keine Dokumente</span>
                                                <span v-else-if="session.receiptCount === 1" class="truncate">1 Dokument</span>
                                                <span v-else class="truncate">{{ session.receiptCount }} Dokumente</span>
                                            </p>
                                        </div>
                                        <div class="hidden md:block">
                                            <div>
                                                <p class="text-sm text-gray-900">
                                                    Erstellt am {{ " " }}
                                                    <time :datetime="session.creationDate">
                                                        {{ useDateFormat(session.creationDate, "DD.MM.YYYY HH:mm:ss").value }}
                                                    </time>
                                                </p>

                                                <p v-if="session.status === 'EMPTY'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <FolderMinusIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-blue-400"/>
                                                    Neue leere Sitzung
                                                </p>

                                                <p v-else-if="session.status === 'PROCESSING'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <svg class="animate-spin mr-1.5 h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                                        <path class="opacity-75" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" fill="currentColor"></path>
                                                    </svg>
                                                    In Verarbeitung
                                                </p>

                                                <p v-else-if="session.status === 'CORRECTION'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <PencilSquareIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-yellow-500"/>
                                                    Korrektur
                                                </p>

                                                <p v-else-if="session.status === 'READY'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <ArchiveBoxArrowDownIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-lime-500"/>
                                                    Bereit für Import
                                                </p>

                                                <p v-else-if="session.status === 'IMPORTED'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <ClipboardDocumentCheckIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-green-600"/>
                                                    Import erfolgreich
                                                </p>

                                                <p v-else-if="session.status === 'FAILURE'" class="mt-2 flex items-center text-sm text-gray-500">
                                                    <ExclamationTriangleIcon aria-hidden="true" class="mr-1.5 h-5 w-5 flex-shrink-0 text-red-600"/>
                                                    Keine validen Dokumente
                                                </p>

                                                <p v-else class="mt-2 flex items-center text-sm text-gray-500">
                                                    {{ session.status }}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <ChevronRightIcon aria-hidden="true" class="h-5 w-5 text-gray-400"/>
                                </div>
                            </div>
                        </NuxtLink>
                    </li>
                </ul>
            </div>
        </div>
    </main>
</template>

<script setup>
import {
    ArchiveBoxArrowDownIcon,
    ChevronRightIcon,
    ClipboardDocumentCheckIcon,
    ClipboardDocumentListIcon,
    FolderMinusIcon,
    FolderPlusIcon,
    PencilSquareIcon,
    ExclamationTriangleIcon,
} from "@heroicons/vue/20/solid";
import {useDateFormat} from "@vueuse/core";
import InlineLoadingCircle from "~/components/loading/InlineLoadingCircle.vue";
import {useOrganization} from "~/composables/organizations";

const sessions = ref([]);

let requestPending = ref(true)

const orgId = useOrganization()
async function updateSessions() {
    const {data, pending, error, refresh} = await useLazyFetch(`/r/org/${orgId.value}/receipt-import/get-sessions`, {
        server: false
    })
    requestPending = pending
    await refresh()

    console.log("Update session data: ", data.value)
    sessions.value = data.value
}

updateSessions()

definePageMeta({
    title: "Importsitzungen",
});
</script>

<style scoped></style>
