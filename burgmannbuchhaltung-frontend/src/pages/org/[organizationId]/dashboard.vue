<template>
    <main v-if="organizationData === null">
        <LoadingIndicator> Lade Organisation... </LoadingIndicator>
    </main>

    <main v-else class="min-h-full mx-auto mt-1">
        <h1 class="sr-only">Profile</h1>
        <!-- Main 3 column grid -->
        <div class="grid grid-cols-1 items-start gap-4 lg:grid-cols-3 lg:gap-8">
            <!-- Left column -->
            <div class="grid grid-cols-1 gap-4 lg:col-span-2">
                <!-- Welcome panel -->
                <section aria-labelledby="profile-overview-title">
                    <div class="overflow-hidden rounded-lg bg-white shadow">
                        <h2 id="profile-overview-title" class="sr-only">Profile Overview</h2>
                        <div class="bg-white px-6 py-4">
                            <div class="sm:flex sm:items-center sm:justify-between">
                                <div class="sm:flex sm:space-x-5">
                                    <div class="flex-shrink-0">
                                        <img :src="user.imageUrl" alt="" class="mx-auto h-20 w-20 rounded-full" />
                                    </div>
                                    <div class="mt-4 text-center sm:mt-0 sm:text-left">
                                        <p class="text-sm font-medium text-gray-600">
                                            <TimeBasedGreeting />
                                            <span>,</span>
                                        </p>
                                        <p class="text-xl font-bold text-gray-900 sm:text-2xl">{{ user.name }}</p>

                                        <dl class="flex flex-col sm:flex-row sm:flex-wrap text-sm text-gray-600">
                                            <dt class="sr-only">Company</dt>
                                            <dd class="flex items-center text-sm font-medium sm:mr-6">
                                                <BuildingOfficeIcon class="mr-1.5 h-5 w-5 flex-shrink-0 text-gray-400"></BuildingOfficeIcon>
                                                {{ organizationData.name }}
                                            </dd>
                                            <dd class="flex items-center text-sm font-medium capitalize sm:mr-6 sm:mt-0">
                                                <UserIcon class="mr-1.5 h-5 w-5 flex-shrink-0 text-gray-400" fill="currentColor" />
                                                Geschäftsführerin
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                                <div class="mt-5 flex justify-center sm:mt-0">
                                    <a
                                        class="flex items-center justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                        href="#"
                                    >
                                        Profil
                                    </a>
                                </div>
                            </div>
                        </div>
                        <div class="grid grid-cols-1 divide-y divide-gray-200 border-t border-gray-200 bg-gray-50 sm:grid-cols-3 sm:divide-y-0">
                            <div v-for="stat in stats" :key="stat.label" class="px-3 py-2 text-center text-sm font-medium">
                                <div class="overflow-hidden rounded-lg bg-white shadow">
                                    <dl>
                                        <div class="py-2.5 px-5">
                                            <div class="flex items-center">
                                                <div class="flex-shrink-0">
                                                    <Icon :name="stat.icon" class="h-6 w-6 text-gray-400" />
                                                </div>
                                                <div class="ml-5 w-0 flex-1">
                                                    <dd>
                                                        <div class="text-lg font-medium text-gray-900">{{ stat.value }}</div>
                                                    </dd>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="bg-gray-50 px-5 py-2">
                                            <div class="text-sm">
                                                <dt>
                                                    <a class="font-medium text-cyan-700 hover:text-cyan-900 truncate" href="#">{{ stat.label }}</a>
                                                </dt>
                                            </div>
                                        </div>
                                    </dl>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Actions panel -->
                <section aria-labelledby="quick-links-title">
                    <div class="divide-y divide-gray-200 overflow-hidden rounded-lg bg-gray-200 shadow sm:grid sm:grid-cols-2 sm:gap-px sm:divide-y-0">
                        <h2 id="quick-links-title" class="sr-only">Quick links</h2>
                        <div
                            v-for="(action, actionIdx) in actions"
                            :key="action.name"
                            :class="[
                                actionIdx === 0 ? 'rounded-tl-lg rounded-tr-lg sm:rounded-tr-none' : '',
                                actionIdx === 1 ? 'sm:rounded-tr-lg' : '',
                                actionIdx === actions.length - 2 ? 'sm:rounded-bl-lg' : '',
                                actionIdx === actions.length - 1 ? 'rounded-bl-lg rounded-br-lg sm:rounded-bl-none' : '',
                                'group relative bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-cyan-500',
                            ]"
                        >
                            <div>
                                <span :class="[action.iconBackground, action.iconForeground, 'inline-flex rounded-lg p-3 ring-4 ring-white']">
                                    <Icon :name="action.icon" aria-hidden="true" class="h-6 w-6" />
                                </span>
                            </div>
                            <div class="mt-5">
                                <h3 class="text-lg font-medium">
                                    <NuxtLink :to="action.href" class="focus:outline-none">
                                        <!-- Extend touch target to entire panel -->
                                        <span aria-hidden="true" class="absolute inset-0" /> {{ action.name }}
                                    </NuxtLink>
                                </h3>
                                <p class="mt-2 text-sm text-gray-500">{{ action.description }}</p>
                            </div>
                            <span aria-hidden="true" class="pointer-events-none absolute right-6 top-6 text-gray-300 group-hover:text-gray-400">
                                <svg class="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                                    <path
                                        d="M20 4h1a1 1 0 00-1-1v1zm-1 12a1 1 0 102 0h-2zM8 3a1 1 0 000 2V3zM3.293 19.293a1 1 0 101.414 1.414l-1.414-1.414zM19 4v12h2V4h-2zm1-1H8v2h12V3zm-.707.293l-16 16 1.414 1.414 16-16-1.414-1.414z"
                                    />
                                </svg>
                            </span>
                        </div>
                    </div>
                </section>
            </div>

            <!-- Right column -->
            <div class="grid grid-cols-1 gap-4">
                <!-- Announcements -->
                <section aria-labelledby="announcements-title" class="order-last sm:order-none">
                    <div class="overflow-hidden rounded-lg bg-white shadow">
                        <div class="px-6 pt-5 pb-4">
                            <h2 id="announcements-title" class="text-base font-medium text-gray-900 flex items-center gap-1">
                                <Icon class="h-5 w-5 text-gray-700" name="heroicons:newspaper" />
                                Neuigkeiten
                            </h2>
                            <div class="mt-5 flow-root">
                                <ul class="-my-5 divide-y divide-gray-200" role="list">
                                    <li v-for="announcement in announcements" :key="announcement.id" class="py-3">
                                        <div class="relative focus-within:ring-2 focus-within:ring-cyan-500">
                                            <h3 class="text-sm font-semibold text-gray-800">
                                                <a :href="announcement.href" class="hover:underline focus:outline-none">
                                                    <!-- Extend touch target to entire panel -->
                                                    <span aria-hidden="true" class="absolute inset-0" /> {{ announcement.title }}
                                                </a>
                                            </h3>
                                            <p class="mt-1 line-clamp-2 text-sm text-gray-600">{{ announcement.preview }}</p>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                            <div class="mt-6">
                                <a
                                    class="flex w-full items-center justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                    href="#"
                                >
                                    Alle ansehen
                                </a>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Recent Transactions -->
                <section aria-labelledby="recent-transactions-title" class="">
                    <div class="overflow-hidden rounded-lg bg-white shadow">
                        <div class="px-6 pt-5 pb-4">
                            <h2 id="recent-transactions-title" class="text-base font-medium text-gray-900 flex items-center gap-1">
                                <Icon class="h-5 w-5 text-gray-700" name="heroicons:clipboard-document-list" />
                                Letzte Transaktionen
                            </h2>
                            <div class="mt-5 flow-root">
                                <ul class="-my-5 divide-y divide-gray-200" role="list">
                                    <li v-for="transaction in recentTransactions" :key="transaction.id" class="py-4">
                                        <div class="flex items-center space-x-4">
                                            <div class="flex-shrink-0">
                                                <Icon :name="transaction.primaryIcon" class="h-6 w-6" />
                                            </div>
                                            <div class="min-w-0 flex-1">
                                                <p class="text-sm font-medium text-gray-900 line-clamp-2 overflow-scroll">
                                                    {{ transaction.title }}: <span class="underline">{{ transaction.name }}</span>
                                                </p>

                                                <div class="flex justify-between">
                                                    <p class="truncate text-sm text-gray-500 flex gap-1">
                                                        <!-- TODO: match datetime with transaction.date -->
                                                        <Icon class="h-5 w-5" name="heroicons:calendar-days" />
                                                        <time datetime="2023-01-01">{{ transaction.date }}</time>
                                                    </p>

                                                    <p class="truncate text-sm text-gray-500 flex gap-1">
                                                        <Icon class="h-5 w-5" name="heroicons:document-check" />
                                                        <span>{{ transaction.handle }}</span>
                                                    </p>
                                                </div>
                                            </div>
                                            <div>
                                                <a
                                                    :href="transaction.href"
                                                    class="inline-flex items-center rounded-full bg-white px-2.5 py-1 text-xs font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                                >
                                                    Ansehen
                                                </a>
                                            </div>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                            <div class="mt-6">
                                <a
                                    class="flex w-full items-center justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                    href="#"
                                >
                                    Alle ansehen
                                </a>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </main>
</template>

<script setup>
import { BuildingOfficeIcon, UserIcon } from "@heroicons/vue/24/outline";
import TimeBasedGreeting from "~/components/TimeBasedGreeting.vue";
import { useCurrentOrganization, useOrganization } from "~/composables/organizations";
import LoadingIndicator from "~/components/loading/LoadingIndicator.vue";

const user = {
    name: "Maximilia Muster",
    role: "Geschäftsführerin",
    imageUrl: "https://images.unsplash.com/photo-1550525811-e5869dd03032?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80",
};

const organizationData = useCurrentOrganization();

const stats = [
    { label: "Verbundenes Bankkonto", value: "€123.456,78", icon: "heroicons:building-library" },
    { label: "Umsatz (Monat)", value: "€10.012,-", icon: "heroicons:banknotes" },
    { label: "Gewinn (Monat)", value: "€7.351,21", icon: "heroicons:scale" },
];
const actions = [
    {
        icon: "material-symbols:point-of-sale",
        name: "Verkauf",
        description: "Ausgangsrechnung an einen Kunden schreiben",
        href: "#",
        iconForeground: "text-teal-700",
        iconBackground: "bg-teal-50",
    },
    {
        icon: "heroicons:receipt-refund",
        name: "Einkauf / Ausgaben",
        description: "Eingangsrechnung eines Lieferanten verbuchen",
        href: "#",
        iconForeground: "text-rose-700",
        iconBackground: "bg-rose-50",
    },
    {
        icon: "bi:graph-down-arrow",
        name: "Abschreibungen",
        description: "Kilometergeld, Arbeitsgeräte & -maschinen, Immobilien, ...",
        href: "#",
        iconForeground: "text-purple-700",
        iconBackground: "bg-purple-50",
    },
    {
        icon: "fluent-emoji-high-contrast:customs",
        name: "Steuern & Abgaben",
        description: "USt-Voranmeldung & -Erklärung, EkSt-Erklärung, ZM, ...",
        href: "#",
        iconForeground: "text-sky-700",
        iconBackground: "bg-sky-50",
    },
    {
        icon: "heroicons:table-cells",
        name: "Manuell buchen",
        description: "Manuell einen Buchungssatz verbuchen",
        href: "manual",
        iconForeground: "text-yellow-700",
        iconBackground: "bg-yellow-50",
    },

    {
        icon: "heroicons:academic-cap",
        name: "Training",
        description: "Die umfangreiche Enzyklopädie aufschlagen",
        href: "#",
        iconForeground: "text-indigo-700",
        iconBackground: "bg-indigo-50",
    },
];
const recentTransactions = [
    {
        id: "ABC",
        date: "01.01.2023",
        title: "Zahlung gesendet",
        name: "Bernd Beispiel",
        handle: "EUR 20.000",
        tags: ["success"],
        primaryIcon: "heroicons:banknotes",
        amountIcon: "heroicons:document-minus",
        href: "#",
    },
    {
        id: "ABC",
        date: "01.11.2022",
        title: "Zahlung erhalten",
        name: "Ernst Example",
        handle: "EUR 10.000",
        tags: ["success"],
        primaryIcon: "heroicons:banknotes",
        amountIcon: "heroicons:document-plus",
        href: "#",
    },
    {
        id: "ABC",
        date: "01.01.2023",
        title: "Zahlung gesendet",
        name: "Sozialversicherungsanstalt der Selbstständingen",
        handle: "EUR 20.000",
        tags: ["success"],
        primaryIcon: "heroicons:banknotes",
        amountIcon: "heroicons:document-minus",
        href: "#",
    },
];
const announcements = [
    {
        id: 1,
        title: "Fahrtkostenabrechnung nun verfügbar",
        preview: "Sie können nun vollautomatisch Kilometergeld mit Ihrem Mobiltelefon abrechnen. Klicken Sie hier um mehr zu erfahren.",
        href: "#",
    },
    {
        id: 2,
        title: "EU-OSS",
        preview: "Sie können nun bei Verkäufen außerhalb Ihres Mitgliedstaates das EU-One-Stop-Shop einsetzen. Klicken Sie hier um mehr zu erfahren.",
        href: "#",
    },
    {
        id: 3,
        title: "Titel",
        href: "#",
        preview: "Beschreibung",
    },
];
</script>
