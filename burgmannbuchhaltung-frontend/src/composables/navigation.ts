import { useOrganization } from "~/composables/organizations";

const orgId = useOrganization();

watch(orgId, async (newValue, oldValue) => {
    console.log(`(info): ORGANIZATION CHANGED from ${oldValue} to ${newValue}`)
})

const navigation = computed(() => {
    const organizationId = orgId.value

    if (organizationId == null)
        return []

    return [
        {
            name: `Startseite`,
            href: "/org/" + organizationId + "/dashboard",
            icon: "heroicons:home",
            current: true,
        },
        {
            name: "Rechnungen",
            href: "/org/" + organizationId + "/invoices/sales",
            icon: "heroicons:document-text",
            current: false,
        },
        {
            name: "Kunden & Lieferanten",
            href: "/org/" + organizationId + "/ext/customers",
            icon: "heroicons:user-group",
            current: false,
        },
        {
            name: "Kalendar",
            href: "/org/" + organizationId + "/calendar",
            icon: "heroicons:calendar",
            current: false,
        },
        {
            name: "Dokumente",
            href: "/org/" + organizationId + "/documents",
            icon: "heroicons:document-duplicate",
            current: false,
        },
        {
            name: "Berichte",
            href: "/org/" + organizationId + "/reports",
            icon: "heroicons:chart-bar",
            current: false,
        },
        {
            name: "Erweitert",
            icon: "heroicons:chevron-down",
            children: [
                {
                    name: "Manuell verbuchen",
                    href: "/org/" + organizationId + "/manual",
                    icon: "heroicons:pencil-square",
                    current: false,
                },
                {
                    name: "OCR Daten-Import",
                    href: "/org/" + organizationId + "/ocr-import/sessions/list",
                    icon: "heroicons:inbox-arrow-down",
                    current: false,
                },
                {
                    name: "Daten-Export",
                    href: "/org/" + organizationId + "/export",
                    icon: "heroicons:inbox-stack",
                    current: false,
                },
            ],
        },
    ]
})

export function useNavigation() {
    console.log("Navigation is: ", navigation);
    return navigation;
}
