<template>
    <div>
        <TransitionRoot :show="sidebarOpen" as="template">
            <Dialog as="div" class="relative z-40 lg:hidden" @close="sidebarOpen = false">
                <TransitionChild
                    as="template"
                    enter="transition-opacity ease-linear duration-300"
                    enter-from="opacity-0"
                    enter-to="opacity-100"
                    leave="transition-opacity ease-linear duration-300"
                    leave-from="opacity-100"
                    leave-to="opacity-0"
                >
                    <div class="fixed inset-0 bg-gray-600 bg-opacity-75" />
                </TransitionChild>

                <div class="fixed inset-0 z-40 flex">
                    <TransitionChild
                        as="template"
                        enter="transition ease-in-out duration-300 transform"
                        enter-from="-translate-x-full"
                        enter-to="translate-x-0"
                        leave="transition ease-in-out duration-300 transform"
                        leave-from="translate-x-0"
                        leave-to="-translate-x-full"
                    >
                        <DialogPanel class="relative flex w-full max-w-xs flex-1 flex-col bg-white">
                            <TransitionChild
                                as="template"
                                enter="ease-in-out duration-300"
                                enter-from="opacity-0"
                                enter-to="opacity-100"
                                leave="ease-in-out duration-300"
                                leave-from="opacity-100"
                                leave-to="opacity-0"
                            >
                                <div class="absolute top-0 right-0 -mr-12 pt-2">
                                    <button
                                        class="ml-1 flex h-10 w-10 items-center justify-center rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                                        type="button"
                                        @click="sidebarOpen = false"
                                    >
                                        <span class="sr-only">Close sidebar</span>
                                        <XMarkIcon aria-hidden="true" class="h-6 w-6 text-white" />
                                    </button>
                                </div>
                            </TransitionChild>
                            <div class="h-0 flex-1 overflow-y-auto pt-5 pb-4">
                                <div class="flex flex-shrink-0 items-center px-4">
                                    <img alt="Your Company" class="h-8 w-auto" src="https://tailwindui.com/img/logos/mark.svg?color=indigo&shade=600" />
                                </div>
                                <nav :class="[notLoggedIn ? 'hidden' : '']" class="mt-5 space-y-1 px-2">
                                    <NavigationLinks :not-logged-in="notLoggedIn"/>
                                </nav>
                            </div>
                            <OrganizationSelector />
                        </DialogPanel>
                    </TransitionChild>
                    <div class="w-14 flex-shrink-0">
                        <!-- Force sidebar to shrink to fit close icon -->
                    </div>
                </div>
            </Dialog>
        </TransitionRoot>

        <!-- Static sidebar for desktop -->
        <div class="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
            <!-- Sidebar component, swap this element with another sidebar if you like -->
            <div class="flex min-h-0 flex-1 flex-col border-r border-gray-200 bg-white">
                <div class="flex flex-1 flex-col overflow-y-auto pt-5 pb-4">
                    <div class="flex flex-shrink-0 items-center px-4 text-xl">
                        <BurgmannBuchhaltung />
                        <!--                        <img alt="Your Company" class="h-8 w-auto" src="https://tailwindui.com/img/logos/mark.svg?color=indigo&shade=600"/>-->
                    </div>
                    <nav class="mt-5 flex-1 space-y-1 bg-white px-2">
                        <NavigationLinks :not-logged-in="notLoggedIn"/>
                    </nav>
                </div>

                <OrganizationSelector />
            </div>
        </div>
        <div class="sticky top-0 z-10 bg-gray-100 pl-1 pt-1 sm:pl-3 sm:pt-3 lg:hidden flex items-center -ml-0.5 -mt-0.5">
            <button
                class="inline-flex h-12 w-12 items-center justify-center rounded-md text-gray-500 hover:text-gray-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500"
                type="button"
                @click="sidebarOpen = true"
            >
                <span class="sr-only">Open sidebar</span>
                <Bars3Icon aria-hidden="true" class="h-6 w-6" />
            </button>

            <div>
                <h1 class="text-xl text-gray-900">{{ route.meta.title ?? "BurgmannBuchhaltung" }}</h1>
                <hr />
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { Dialog, DialogPanel, TransitionChild, TransitionRoot } from "@headlessui/vue";
import { Bars3Icon, XMarkIcon } from "@heroicons/vue/24/outline";
import { ref } from "vue";
import { useNavigation } from "~/composables/navigation";
import OrganizationSelector from "~/components/organizations/OrganizationSelector.vue";
import BurgmannBuchhaltung from "~/components/BurgmannBuchhaltung.vue";
import NavigationLinks from "~/components/layout/NavigationLinks.vue";

const props = defineProps({
    notLoggedIn: {
        type: Boolean,
        required: false,
        default: false,
    },
});

const route = useRoute();
const sidebarOpen = ref(false);
</script>
