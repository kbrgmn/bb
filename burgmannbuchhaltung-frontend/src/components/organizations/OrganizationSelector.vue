<template>
    <div class="flex flex-shrink-0 border-t border-gray-200">
        <button class="group block w-full flex-shrink-0 p-4 hover:bg-gray-50" @click="open = true">
            <div class="flex items-center">
                <div class="min-w-fit">
                    <img v-if="orgData != null" alt="" class="inline-block h-12 w-12 rounded-md" :src="orgData.image" />
                    <Icon v-else name="heroicons:list-bullet" class="h-10 w-10 p-1" />
                </div>
                <div class="ml-1.5 w-40">
                    <div v-if="organization != null">
                        <p v-if="orgData != null" class="break-words text-sm font-semibold text-gray-700 group-hover:text-gray-900">{{ orgData.name }}</p>
                        <p class="text-xs font-medium text-gray-500 group-hover:text-gray-700">Organisation wechseln</p>
                    </div>
                    <div v-else class="w-44">
                        <p class="text-sm font-semibold text-gray-700 group-hover:text-gray-900">Organisation auswählen</p>
                        <p class="text-xs tracking-tight text-gray-400 group-hover:text-gray-600">(Keine Organisation ausgewählt)</p>
                    </div>
                </div>
            </div>
        </button>

        <TransitionRoot :show="open" as="template">
            <Dialog as="div" class="relative z-10" @close="open = false">
                <TransitionChild as="template" enter="ease-out duration-300" enter-from="opacity-0" enter-to="opacity-100" leave="ease-in duration-200" leave-from="opacity-100" leave-to="opacity-0">
                    <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
                </TransitionChild>

                <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
                    <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
                        <TransitionChild
                            as="template"
                            enter="ease-out duration-300"
                            enter-from="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                            enter-to="opacity-100 translate-y-0 sm:scale-100"
                            leave="ease-in duration-200"
                            leave-from="opacity-100 translate-y-0 sm:scale-100"
                            leave-to="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
                        >
                            <DialogPanel class="relative transform overflow-hidden rounded-lg bg-white px-2 pb-4 pt-3 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-xl sm:p-6">
                                <div class="absolute right-0 top-0 pr-5 pt-4 sm:pr-7 sm:pt-7">
                                    <button
                                        class="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                                        type="button"
                                        @click="open = false"
                                    >
                                        <span class="sr-only">Close</span>
                                        <XMarkIcon aria-hidden="true" class="h-5 w-5" />
                                    </button>
                                </div>
                                <h1 class="text-lg font-semibold">Organisation auswählen:</h1>
                                <hr class="my-1" />
                                <OrganizationSelectionList @close-action="open = false"></OrganizationSelectionList>
                            </DialogPanel>
                        </TransitionChild>
                    </div>
                </div>
            </Dialog>
        </TransitionRoot>
    </div>
</template>

<script setup>
import { ref } from "vue";
import { Dialog, DialogPanel, TransitionChild, TransitionRoot } from "@headlessui/vue";
import { XMarkIcon } from "@heroicons/vue/24/outline";
import OrganizationSelectionList from "~/components/organizations/OrganizationSelectionList.vue";
import { useCurrentOrganization, useOrganization } from "~/composables/organizations";

const organization = useOrganization();
const orgData = useCurrentOrganization();

const open = ref(false);
</script>

<style scoped></style>
