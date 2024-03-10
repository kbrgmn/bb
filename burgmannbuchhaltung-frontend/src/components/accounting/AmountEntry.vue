<template>
    <div class="relative rounded-md shadow-sm flex">
        <div v-if="currency != null && displayShortCurrencyPrefix" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-1.5">
            <!--            <span :class="[isInvalid ? 'text-neutral-50' : 'text-gray-500']" class="text-gray-500 sm:text-sm">€</span>-->
            <!--            <Icon name="heroicons:exclamation-circle" class="w-5 h-5 text-red-600"/>-->
            <CurrencySymbolDisplay :currency="currency" class="h-5 w-5"/>
            <!--            <Icon class="h-5 w-5" name="heroicons:calendar-days"/>-->
        </div>

        <input id="amount" v-model="displayValue" :class="[currency != null && displayShortCurrencyPrefix ? 'pl-7' : '', displayCurrencySuffix ? 'pr-12' : '',
               isInvalid ? 'bg-red-600 text-white' : '']" :placeholder="fallback ?? '0,00'" aria-describedby="amount-currency"
               class="block w-full rounded-md border-0 py-1.5 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-500 placeholder:underline focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
               name="amount" pattern="^\d{1,3}(\.\d{3})*,?(\d{1,5})?$" type="text" @blur="isFocused = false" @focus="isFocused = true" @keydown="inputKeyCheck"/>
        <!-- REGEX: ^\d{1,3}(\.\d{3})*,?(\d{1,5})?$ -->

        <div v-if="displayCurrencySuffix" class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
            <span id="amount-currency" class="text-gray-500 sm:text-sm">{{ currency }}</span>
        </div>
    </div>
</template>

<script lang="ts" setup>
import {formatMoney, parseMoney} from "~/composables/moneyparsing";
import CurrencySymbolDisplay from "~/components/accounting/CurrencySymbolDisplay.vue";
import BigNumber from "bignumber.js";

const props = withDefaults(defineProps<{
    currency: string | undefined,
    modelValue: BigNumber | null,
    displayShortCurrencyPrefix: boolean,
    displayCurrencySuffix: boolean,
    fallback: string | null
}>(), {
    displayShortCurrencyPrefix: true,
    displayCurrencySuffix: true,
    fallback: undefined
})

const emit = defineEmits(['update:modelValue'])

const isFocused = ref(false)

//const isInvalid = ref(false)
const isInvalid = computed(() => {
    return parseMoney(inputBacking.value) == null
})

const inputBacking: Ref<string | null> = ref("")
const displayValue: WritableComputedRef<string | null> = computed({
    get() {
        if (isFocused.value) {
            return inputBacking.value//.toString()
        } else {
            //const inputString = props.modelValue//.toString()
            //const parsed = parseMoney(inputString)
            const inputMoney = props.modelValue

            console.log("Input money: ", inputMoney)

            if (false && inputMoney ) {
                return null // NULL as value will display `fallback` as placeholder
            } else {
                // noinspection JSIncompatibleTypesComparison
                if (inputMoney != null) {
                    //isInvalid.value = false
                    return formatMoney(inputMoney, false)
                } else {
                    //isInvalid.value = true
                    return inputBacking.value
                }
            }
        }
    },
    set(modifiedValue) {
        console.log("Updating inputBacking: ", modifiedValue)
        inputBacking.value = modifiedValue

        const bigNum: BigNumber | null = (modifiedValue instanceof BigNumber) ? modifiedValue : parseMoney(modifiedValue)
        emit('update:modelValue', bigNum)

        //emit('update:modelValue', modifiedValue)
    }
})

function inputKeyCheck(evt: KeyboardEvent) {
    if (
        !evt.ctrlKey
        && !evt.altKey
        && !evt.shiftKey
        && evt.key.length == 1
        && !'0123456789,.%'.includes(evt.key)
    ) {
        console.log('blocked: ' + evt.key);
        evt.preventDefault()
    }
}

</script>

<style scoped>

</style>
