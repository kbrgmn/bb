export type AccountEntry = {
    id: string;
    short?: string;
    name: string;
    currency: string;
};

export function getAccountEntry(organizationId: string, accountId: string): AccountEntry | null {
    console.log(`getAccountEntry: ${JSON.stringify(accountId)}: `, accounts.find((value) => value.id === accountId) ?? null);
    return accounts.find((value) => value.id === accountId) ?? null;
}

export const accounts: AccountEntry[] = [
    { id: "2700", short: "K", name: "Kassa", currency: "EUR" },
    { id: "2750", short: "KUSD", name: "Kassa US-Dollar", currency: "USD" },
    { id: "2751", short: "KHUF", name: "Kassa Ungarischer Forint", currency: "HUF" },
    { id: "2752", short: "KCZK", name: "Kassa Tschechische Kronen", currency: "CZK" },
    { id: "2800", short: "B", name: "Bank", currency: "EUR" },
    { id: "2801", short: "BCZK", name: "Bank", currency: "CZK" },
    { id: "0660", short: "BGA", name: "Betriebs- und Geschäftsausstattung", currency: "EUR" },
    { id: "7600", short: "Büro", name: "Büromaterial", currency: "EUR" },
    { id: "2500", short: "VSt", name: "Vorsteuer", currency: "EUR" },
    { id: "2740", name: "Postwertzeichen", currency: "EUR" },
    { id: "3510", short: "USt 10%", name: "Umsatzsteuer 10%", currency: "EUR" },
    { id: "3520", short: "USt 20%", name: "Umsatzsteuer 20%", currency: "EUR" },
    //{id: '4000', name: 'HW-Erlöse', currency: "EUR"},
    { id: "4010", name: "Erlös 10%", currency: "EUR" },
    { id: "4020", name: "Erlös 20%", currency: "EUR" },
    { id: "7381", name: "Telefon- und Internetgebühren", currency: "EUR" },
    { id: "20001", name: "Kunde XYZ", currency: "EUR" },
    { id: "33099", name: "Diverse Lieferanten", currency: "EUR" },
];
