<template>
    <div class="shadow px-2 py-3 rounded-xl flex flex-col">
        <div class="grid grid-cols-2 gap-3 mb-1">
            <EntryList ref="debitList" v-model:entries="entriesDebit" v-model:total="debitTotal" shortcut="q" title="Soll" @focusin="focusedEntryList = 'debit'" @focusout="focusedEntryList = ''" />
            <EntryList
                ref="creditList"
                v-model:entries="entriesCredit"
                v-model:total="creditTotal"
                shortcut="w"
                title="Haben"
                @focusin="focusedEntryList = 'credit'"
                @focusout="focusedEntryList = ''"
            />
        </div>

        <div class="flex justify-between px-1">
            <SubmitButton :icon="TrashIcon" class="bg-white hover:bg-gray-100" @click="clearAll">Leeren</SubmitButton>

            <NumberComparisonIcon :number1="debitTotal.base.plus(debitTotal.fallback)" :number2="creditTotal.base.isZero() ? creditTotal.fallback : creditTotal.base" />

            <IndigoSubmitButton :icon="FolderPlusIcon" :loading="false" @click="bookEntry">
                Verbuchen
                <AccessKeyInfo>Ctrl+Enter</AccessKeyInfo>
            </IndigoSubmitButton>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { useMagicKeys } from "@vueuse/core";
import EntryList from "~/components/accounting/EntryList.vue";
import Total from "~/components/accounting/EntryList.vue";
import ListEntry from "~/components/accounting/EntryList.vue";
import { FolderPlusIcon, TrashIcon } from "@heroicons/vue/24/outline";
import IndigoSubmitButton from "~/components/buttons/IndigoSubmitButton.vue";
import AccessKeyInfo from "~/components/AccessKeyInfo.vue";
import SubmitButton from "~/components/buttons/SubmitButton.vue";
import BigNumber from "bignumber.js";
import NumberComparisonIcon from "~/components/accounting/NumberComparisonIcon.vue";

const { current } = useMagicKeys();

const focusedEntryList = ref("");

const debitList: Ref<typeof EntryList | null> = ref(null);
const creditList: Ref<typeof EntryList | null> = ref(null);

const entriesDebit: ListEntry[] = reactive([
    {
        account: null,
        amount: null,
    },
]);
const entriesCredit: ListEntry[] = reactive([
    {
        account: null,
        amount: null,
    },
]);

// noinspection TypeScriptValidateTypes
const debitTotal: Ref<Total> = ref({ base: BigNumber(0), fallback: BigNumber(0) });
// noinspection TypeScriptValidateTypes
const creditTotal: Ref<Total> = ref({ base: BigNumber(0), fallback: BigNumber(0) });

function onEntriesDebitUpdate() {
    /*nextTick(() => {*/
    console.log("Entries debit watch XXXXXX");
    console.log("Entries credit: ", entriesCredit);
    if (entriesCredit.length === 1 && entriesCredit[0].amount === null) {
        //const fallbackAmount = entriesDebit.reduce((partialSum, entry) => partialSum.plus(parseMoney(entry.amount)), BigNumber(0));
        const fallbackAmount = entriesDebit.reduce((partialSum, entry) => (entry?.amount ? partialSum.plus(entry.amount) : partialSum), BigNumber(0));

        console.log(`Calculated fallback: >${fallbackAmount}<: `, fallbackAmount);

        entriesCredit[0].fallbackAmount = fallbackAmount;
    } else {
        if (entriesCredit[0] !== undefined) {
            entriesCredit[0].fallbackAmount = null;
        }
    }
    /*})*/
}

watch(entriesDebit, async () => {
    onEntriesDebitUpdate();
});

/*
function getDebitCurrencies(): string[] {

}

function getCreditCurrencies() {

}*/

function bookEntry() {
    console.log(entriesDebit);
    console.log(entriesCredit);
}

function clearAll() {
    debitList.value?.removeAllEntries();
    creditList.value?.removeAllEntries();

    nextTick(() => {
        creditList.value?.addAdditionalEntry();
        debitList.value?.addAdditionalEntry();
    });
}

// Keyboard shortcuts
watch(current, () => {
    if (current.size === 2) {
        if (current.has("alt") || current.has("control")) {
            if (current.has("q")) {
                debitList.value?.selectInput(1);
            } else if (current.has("w")) {
                creditList.value?.selectInput(1);
            } else {
                for (const c of "123456789") {
                    if (current.has(c)) {
                        console.log("List is: " + focusedEntryList.value + ", row index: " + c);

                        const idx = parseInt(c);

                        if (focusedEntryList.value === "debit") {
                            debitList.value?.selectInput(idx);
                        } else if (focusedEntryList.value === "credit") {
                            creditList.value?.selectInput(idx);
                        } else {
                            // no list selected
                        }
                    }
                }
            }
        } else if (current.has("shift") && current.has("enter")) {
            if (focusedEntryList.value === "debit") {
                debitList.value?.addAdditionalEntry();
            } else if (focusedEntryList.value === "credit") {
                creditList.value?.addAdditionalEntry();
            }
        } else if (current.has("control") && current.has("enter")) {
            console.log("ENTER");
            bookEntry();
        }
    }
});
</script>
