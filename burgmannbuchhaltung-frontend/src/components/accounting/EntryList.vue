<template>
    <div class="border border-gray-200 focus-within:border-indigo-200 focus-within:shadow-xl bg-white px-3 pt-4 pb-5 rounded-xl ">
        <AccessKeyInfo>Alt+{{ shortcut }}</AccessKeyInfo>
        <div class="flex justify-between">
            <h3 class="grow text-base font-semibold leading-6 text-gray-900">{{ title }}</h3>
            <p class="font-normal mr-2">
                <span >Gesamt: </span>{{ " " }}
                <span v-if="!total.base.isZero()">{{ formatMoney(total.base) }}{{ " " }}</span>
                <span v-if="total?.fallback?.isZero() === false" class="text-gray-500">(<span v-if="!total.base.isZero()">+ </span><span class="underline">{{ formatMoney(total.fallback) }}</span>)</span>
                <span v-else-if="total.base.isZero() && total?.fallback?.isZero() !== false" class="text-xs">Keine Eingabe</span>
            </p>
        </div>
        <div v-for="(entry, idx) of entries" :ref="el => { divs[idx] = el }" class="flex gap-2 py-1 mt-1">
            <div v-if="(idx + 1) < 10">
                <AccessKeyInfo>Alt+{{ idx + 1 }}</AccessKeyInfo>
                <AccessKeyInfo>/</AccessKeyInfo>
                <AccessKeyInfo>Ctrl+{{ idx + 1 }}</AccessKeyInfo>
            </div>
            <AccountEntry v-model="entries[idx].account" class="grow"/>
            <AmountEntry v-model="entries[idx].amount" :currency="getAccountEntry('todo', entries[idx].account?.id)?.currency" :fallback="formatMoney(entry.fallbackAmount, false)"
                         class="w-48" :displayShortCurrencyPrefix="true" :displayCurrencySuffix="true"/>
            <button :disabled="entries.length <= 1" class="disabled:hidden rounded-full px-0.5 font-semibold text-gray-700 hover:text-red-600 hover:scale-125 duration-75"
                    @click="removeEntry(entry)">
                <BackspaceIcon class="h-5 w-5"/>
            </button>
            <!--A{{ entry.amount }}F{{ formatMoney(entry.fallbackAmount, false) }}-->
        </div>
        <button class="mt-1 rounded-full bg-white px-3 py-1 font-semibold text-gray-900
                shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50
                flex items-center" type="button" @click="addAdditionalEntry">
            <span class="text-xl">+</span> <span class="text-sm sm ml-sm ml-2">Zeile hinzufügen <AccessKeyInfo>Shift+Enter</AccessKeyInfo></span>
        </button>
    </div>
</template>

<script lang="ts" setup>
import AccountEntry from "~/components/accounting/AccountEntry.vue";
import AmountEntry from "~/components/accounting/AmountEntry.vue";
import {BackspaceIcon} from "@heroicons/vue/24/outline"
import AccessKeyInfo from "~/components/AccessKeyInfo.vue";
import {getAccountEntry} from "~/composables/accounts";
import {formatMoney} from "~/composables/moneyparsing";
import BigNumber from "bignumber.js";

export type Total = {base: BigNumber, fallback: BigNumber}
const props = defineProps<{
    title: string,
    shortcut: string,
    entries: ListEntry[],
    total: Total
}>()

export type ListEntry = {
    account: typeof AccountEntry | null,
    //amount: number,
    amount: BigNumber | null,
    fallbackAmount?: BigNumber | null,
}

const emit = defineEmits(['update:entries', 'update:total'])

const divs = ref([]);
onBeforeUpdate(() => {
    divs.value = [];
});

function updateTotal(value: ListEntry[] = props.entries) {
    const total = value.reduce((partialSum, entry) => entry?.amount ? partialSum.plus(entry.amount) : partialSum, BigNumber(0))
    const fallback = value.reduce((partialSum, entry) => entry?.amount ? partialSum : (entry?.fallbackAmount ? partialSum.plus(entry.fallbackAmount) : partialSum), BigNumber(0))

    const newTotal: Total = {base: total, fallback: fallback}
    console.log("Updating total: ", newTotal)

    emit("update:total", newTotal)
}

watch(props.entries, (value) => {
    updateTotal(value)
})

function selectInput(idx: number) {
    console.log(`Selecting ${idx}`)
    if (idx > 0) {
        const selectedDiv = divs.value[idx - 1] as HTMLDivElement;
        if (selectedDiv instanceof HTMLDivElement) {
            (selectedDiv as HTMLDivElement).getElementsByTagName("input")[0].focus()
        }
    }
}

function addAdditionalEntry() {
    const entries = props.entries
    entries.push({account: null, amount: null})
    console.log(JSON.stringify(entries))

    nextTick(() => {
        selectInput(entries.length)
    })
}

function removeAllEntries() {
    const entries = props.entries
    entries.length = 0
}

function removeEntry(entry: ListEntry) {
    const entries = props.entries
    entries.splice(entries.indexOf(entry), 1)
    updateTotal()
}

defineExpose({selectInput, addAdditionalEntry, removeAllEntries})
</script>

<style scoped>

</style>
