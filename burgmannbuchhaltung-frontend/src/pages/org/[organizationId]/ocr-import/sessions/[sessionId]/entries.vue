<template>
    <div class="grid gap-4">
        <div>
            <dl class="grid grid-cols-1 gap-5 sm:grid-cols-3">
                <div class="overflow-hidden rounded-lg bg-white px-4 py-5 shadow sm:p-6">
                    <dt class="truncate text-sm font-medium text-gray-500">Anzahl Buchungszeilen</dt>
                    <dd class="mt-1 text-xl font-semibold tracking-tight text-gray-900">{{ entryList.length }}</dd>
                </div>
                <div class="overflow-hidden rounded-lg bg-white px-4 py-5 shadow sm:p-6">
                    <dt class="truncate text-sm font-medium text-gray-500">Anzahl Tage</dt>
                    <dd class="mt-1 text-xl font-semibold tracking-tight text-gray-900">
                        {{
                        entryList.reduce((values, v) => {
                          if (!values.set[v.date]) {
                            values.set[v.date] = 1
                            values.count++
                          }
                          return values
                        }, {set: {}, count: 0}).count
                        }}
                    </dd>
                </div>
                <div class="overflow-hidden rounded-lg bg-white px-4 py-5 shadow sm:p-6">
                    <dt class="truncate text-sm font-medium text-gray-500">Beträge total</dt>
                    <dd class="mt-1 text-xl font-semibold tracking-tight text-gray-900">{{ formatter.format(entryList.reduce((partialSum, a) => partialSum + a.amount, 0)) }}</dd>
                </div>
            </dl>
        </div>
        <div class="overflow-y-scroll border rounded-xl" style="max-height: 53vh">
            <ReceiptImportEntryEditTable
                v-model="entryList"
                :loading="requestPending"
                class="shadow"
                @edit-table="(arg) => {entryList[arg.index][arg.name] = arg.value}"
            />
        </div>

        <div class="shadow-md bg-white p-2 rounded-xl border">
            <p class="font-semibold text-base">Exportieren als:</p>
            <div class="grid grid-cols-3 gap-2">
                <IndigoSubmitButton @click="exportJson">JSON</IndigoSubmitButton>
                <IndigoSubmitButton>XLSX</IndigoSubmitButton>
                <IndigoSubmitButton @click="generateCsv">CSV</IndigoSubmitButton>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>

import IndigoSubmitButton from "~/components/buttons/IndigoSubmitButton.vue";
import {useOrganization} from "~/composables/organizations";

const route = useRoute()

const sessionId = route.params.sessionId

const entryList = ref([])
const original = ref([])

const formatter = new Intl.NumberFormat('de-AT', {
    style: 'currency',
    currency: 'EUR',

    // These options are needed to round to whole numbers if that's what you want.
    //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
    //maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
});

function parseIntoTableData(data) {
    const list = data.flatMap(entry => entry.lines)

    console.log("Result: ", list)

    entryList.value = list
    original.value = list
}

let requestPending = ref(true)

const orgId = useOrganization()

async function fetchContent() {
    const {data, pending, error, refresh} = await useLazyFetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/generate`, {
        method: 'POST', // TEMPORARY
        server: false
    })
    requestPending = pending

    watch(data, (newData) => {
        console.log("NEW DATA: ", newData)
        if (data != null && data.value != null) {
            const response = data.value.entries
            if (response != null) {
                parseIntoTableData(response)
            }
        } else {
            console.log("No session data received!")
        }
    })

    console.log("Fetch error: ", error.value)
}

watchEffect(() => {
    fetchContent()
})

const isChanged = computed(() => {
    return !isEqual(original.value, entryList.value)
})

function exportJson() {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(entryList.value));
    window.open(dataStr)
}

async function generateCsv() {

    const data = await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/computed`)
    console.log("Session data:", data)

    const newFileName = `${data.name}${data.editedDate != null ? "_edit" + data.editedDate : ""}_count${data.receiptCount}_min${data.minDate}_max${data.maxDate}.csv`
    console.log("New file name:", newFileName)

    await $fetch(`/r/org/${orgId.value}/receipt-import/${sessionId}/makeCsv`, {
        method: "POST",
        body: entryList.value
    }).then(async (csvFile) => {
        const blob = new Blob([csvFile], { type: 'text/csv' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = newFileName
        link.click()
        URL.revokeObjectURL(link.href)
    })
}

definePageMeta({
    title: "Generierte Buchungszeilen",
    //layout: "min"
})
</script>

<style scoped>

</style>
