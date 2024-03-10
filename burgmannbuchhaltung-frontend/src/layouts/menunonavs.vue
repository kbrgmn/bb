<template class="h-full">
    <div class="h-full bg-gray-50">
        <SidebarMenu :not-logged-in="true" />

        <div class="flex flex-1 flex-col lg:pl-64">
            <div class="flex h-16 flex-shrink-0 border-b border-gray-200 bg-white">
                <!-- Search bar -->
                <div class="flex flex-1 justify-between px-4 sm:mx-auto sm:max-w-6xl sm:px-8">
                    <div class="flex flex-1"></div>
                    <div class="ml-4 flex items-center md:ml-6">
                        <button
                            class="rounded-full bg-white p-1 text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                            type="button"
                            @click="reloadData"
                        >
                            <span class="sr-only">Refresh</span>
                            <ArrowPathIcon :class="[refreshing ? 'animate-spin' : '']" aria-hidden="true" class="h-6 w-6" />
                        </button>
                        <button class="rounded-full bg-white p-1 text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2" type="button">
                            <span class="sr-only">Benachrichtigungen</span>
                            <BellIcon aria-hidden="true" class="h-6 w-6" />
                        </button>

                        <!-- Profile dropdown -->
                        <Menu as="div" class="relative ml-3">
                            <div>
                                <MenuButton
                                    class="flex max-w-xs items-center rounded-full bg-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 lg:rounded-md lg:p-2 lg:hover:bg-gray-50"
                                >
                                    <img alt="" class="h-8 w-8 rounded-full" src="/favicon.ico" />
                                    <!-- src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80"/> -->
                                    <span class="ml-3 hidden text-sm font-medium text-gray-700 lg:block">Maximilia Muster</span>
                                    <ChevronDownIcon aria-hidden="true" class="ml-1 hidden h-5 w-5 flex-shrink-0 text-gray-400 lg:block" />
                                </MenuButton>
                            </div>
                            <transition
                                enter-active-class="transition ease-out duration-100"
                                enter-from-class="transform opacity-0 scale-95"
                                enter-to-class="transform opacity-100 scale-100"
                                leave-active-class="transition ease-in duration-75"
                                leave-from-class="transform opacity-100 scale-100"
                                leave-to-class="transform opacity-0 scale-95"
                            >
                                <MenuItems
                                    class="absolute right-0 sm:left-0 top-10 z-10 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
                                >
                                    <div class="px-4 py-3">
                                        <p class="text-sm">Angemeldet als:</p>
                                        <p class="truncate text-sm font-medium text-gray-900">{{ data.email }}</p>
                                    </div>
                                    <div class="py-1">
                                        <MenuItem v-slot="{ active }">
                                            <a :class="[active ? 'bg-gray-100 text-gray-900' : 'text-gray-700', 'block px-4 py-2 text-sm']" href="#">Konteneinstellungen</a>
                                        </MenuItem>
                                        <MenuItem v-slot="{ active }">
                                            <a :class="[active ? 'bg-gray-100 text-gray-900' : 'text-gray-700', 'block px-4 py-2 text-sm']" href="#">Support</a>
                                        </MenuItem>
                                        <MenuItem v-slot="{ active }">
                                            <a :class="[active ? 'bg-gray-100 text-gray-900' : 'text-gray-700', 'block px-4 py-2 text-sm']" href="#">Lizenz</a>
                                        </MenuItem>
                                    </div>
                                    <div class="py-1">
                                        <MenuItem v-slot="{ active }">
                                            <button :class="[active ? 'bg-gray-100 text-gray-900' : 'text-gray-700', 'block w-full px-4 py-2 text-left text-sm']" @click="logout">Ausloggen</button>
                                        </MenuItem>
                                    </div>
                                </MenuItems>
                            </transition>
                        </Menu>
                    </div>
                </div>
            </div>

            <main class="flex-1">
                <div class="py-2 pt-4">
                    <div v-if="route.meta.dynamicHeader !== true && route.meta && route.meta.title" class="mx-auto max-w-[88rem] px-4 sm:px-6 lg:px-8 pb-3 hidden md:block">
                        <h1 class="text-2xl text-gray-900">{{ route.meta.title ?? "BurgmannBuchhaltung" }}</h1>
                        <hr />
                    </div>
                    <div class="mx-auto max-w-[88rem] px-4 sm:px-6 lg:px-8">
                        <slot />
                    </div>
                </div>
            </main>
        </div>
    </div>
</template>

<script setup>
import SidebarMenu from "~/components/layout/SidebarMenu.vue";
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { ArrowPathIcon, BellIcon } from "@heroicons/vue/24/outline";
import { ChevronDownIcon, MagnifyingGlassIcon } from "@heroicons/vue/20/solid";
import { selectOrganization, useOrganization } from "~/composables/organizations";

const route = useRoute();

const refreshing = ref(false);

async function reloadData() {
    refreshing.value = true;
    console.log("Refreshing data");
    refreshNuxtData().then(() => {
        refreshing.value = false;
        console.log("Refreshed data");
    });
}

const { data, signOut } = useAuth();
function logout() {
    signOut({ callbackUrl: "/login" });
}

useHead({
    titleTemplate: (titleChunk) => {
        return titleChunk ? `${titleChunk} - BurgmannBuchhaltung` : route.meta.title ? `${route.meta.title} - BurgmannBuchhaltung` : "BurgmannBuchhaltung";
    },
    meta: [
        {
            property: "og:title",
            content: `BurgmannBuchhaltung - ${route.meta.title}`,
        },
    ],
});

selectOrganization(null);

/*
useHead({
  titleTemplate: (titleChunk) => {
    return titleChunk ? `${titleChunk} - BurgmannBuchhaltung` : 'BurgmannBuchhaltung';
  }
})
 */
</script>
