const route = useRoute();

function getOrganizationFromUrl(): string | null {
    const orgId = route.params.organizationId;
    if (Array.isArray(orgId)) {
        return (orgId as string[])[0];
    }
    return orgId as string | null;
}


export const useOrganization = (): Ref<string | null> => {

    const createdRef = useState<string | null>("organization", () => getOrganizationFromUrl());
    if (createdRef === undefined) {
        console.log("CREATED REF IS UNDEFINED")
    }
    if (createdRef.value === undefined || createdRef.value === "undefined") {
        console.log("CREATED REF VAL IS UNDEFINED")

        if (getOrganizationFromUrl() == undefined || getOrganizationFromUrl() === "undefined") {

            const route = useRoute()
            console.log("AT: " + route + ", WANT TO NAVIGATE TO /")
            /*console.log("ORGANIZATION IN URL IS UNDEFINED, NAVIGATING TO SELECTOR")
            navigateTo("/")*/
        }
    }

    console.log("USING ORGANIZATION: ", createdRef.value)
    return createdRef
}
export const useCurrentOrganization = (): Ref<any | null> =>
    useState<Map<string, any> | null>("current-organization-data", () => {
        updateCurrentOrganizationData();
        return null;
    });

function updateCurrentOrganizationData() {
    getOrganizationDetails(useOrganization().value).then((result) => {
        useCurrentOrganization().value = result;
    });
}

export function selectOrganization(organization: string | null) {
    console.log("Switching to organization: ", organization);
    useOrganization().value = organization;
    updateCurrentOrganizationData();

    if (organization != null) {
        navigateTo(`/org/${organization}/dashboard`);
    }
}

//const { data: organizations } = useNuxtData('organizations')

export async function getOrganizationDetails(id: string | null) {
    if (id === null) return null;
    /*const { data } = await useFetch(`/r/organizations/org/${id}/view`, {
        key: `organization-${id}`,
    });
    return data.value;*/

    const { data } = await useFetch(`/r/organizations/org/${id}/view`);
    return data;
    //return await $fetch(`/r/organizations/org/${id}/view`);
}

type OrganizationEntry = { id: string; name: string; image: string; role: string };

export async function getOrganizations(): Promise<Ref<OrganizationEntry[] | null>> {
    const { data } = await useFetch<OrganizationEntry[]>(`/r/organizations`);
    return data;
    //return await $fetch<OrganizationEntry[]>("/r/organizations");
}
