<template>
    <Combobox v-model="selectedAccount" as="div">
        <div class="relative">
            <ComboboxInput :display-value="(account) => account ? account?.id + ' ' + account?.name : ''"
                           class="w-full rounded-md border-0 bg-white py-1.5 pl-3 pr-10 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                           @change="query = $event.target.value"
            />
            <ComboboxButton class="absolute inset-y-0 right-0 flex items-center rounded-r-md px-2 focus:outline-none">
                <ChevronUpDownIcon aria-hidden="true" class="h-5 w-5 text-gray-400"/>
            </ComboboxButton>

            <ComboboxOptions v-if="filteredAccounts.length > 0" class="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1
                             text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                <ComboboxOption v-for="account in filteredAccounts" :key="account.id" v-slot="{ active, selected }" :value="account" as="template">
                    <li :class="[active ? 'bg-indigo-600 group text-white' : 'text-gray-900']" class="relative cursor-default select-none py-2 pl-3 pr-9">
                        <!-- -->
                        <span :class="['block truncate', selected && 'font-semibold']">
                            <span :class="[selected ? 'text-gray-200' : '']" class="font-mono text-xs text-gray-400 group-hover:text-gray-100">{{ account.id }}</span>
                            <span class="px-2">{{ account.name }}</span>
                            <span v-if="account.short" :class="[selected ? 'text-gray-200' : '']" class="text-ssm text-gray-400 group-hover:text-gray-100">
                                (<span class="px-[0.05rem]">{{ account.short }}</span>)
                            </span>
                        </span>
                        <!-- -->

                        <span v-if="selected" :class="[active ? 'text-white' : 'text-indigo-600']" class="absolute inset-y-0 right-0 flex items-center pr-4">
                          <CheckIcon aria-hidden="true" class="h-6 w-6"/>
                        </span>
                    </li>
                </ComboboxOption>
            </ComboboxOptions>
        </div>
    </Combobox>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {CheckIcon, ChevronUpDownIcon} from '@heroicons/vue/20/solid'
import {
    Combobox,
    ComboboxButton,
    ComboboxInput,
    ComboboxOption,
    ComboboxOptions,
} from '@headlessui/vue'
import {accounts} from "~/composables/accounts";

const props = defineProps(["modelValue"])
const emit = defineEmits(['update:modelValue'])

type AccountEntry = {
    id: string,
    short?: string,
    name: string,
    currency: string
}

const query = ref('')
const selectedAccount = ref(null)
const filteredAccounts = computed(() =>
    query.value === ''
        ? accounts
        : accounts.filter((account) => {
            return account.name.toLowerCase().includes(query.value.toLowerCase())
                || account.id.startsWith(query.value)
                || (account.short && account.short.toLowerCase().replaceAll(" ", "").startsWith(query.value.toLowerCase()))
        })
)

watch(selectedAccount, () => {
    emit('update:modelValue', selectedAccount.value)
})
</script>

<style scoped>

</style>
